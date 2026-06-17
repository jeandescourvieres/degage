package com.degage.spam

import android.content.Context
import android.util.Log
import com.degage.callscreen.fromE164
import com.degage.database.AppDatabase
import com.degage.database.entities.SpamEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class SyncResult(
    val source: String,
    val added: Int,
    val error: String? = null,
)

object SpamSyncManager {

    private val TAG = "SpamSyncManager"

    suspend fun loadBundledList(context: Context): SyncResult = withContext(Dispatchers.IO) {
        var added = 0
        try {
            val db = AppDatabase.getInstance(context)
            context.assets.open("spam_numbers.csv").use { stream ->
                BufferedReader(InputStreamReader(stream)).forEachLine { raw ->
                    val line = raw.trim()
                    if (line.isBlank() || line.startsWith("#")) return@forEachLine
                    val number = line.split(",").firstOrNull()?.normalizeNumber() ?: return@forEachLine
                    if (number.length >= 6) {
                        // Insert ignore si déjà présent
                        kotlinx.coroutines.runBlocking {
                            db.spamDao().insert(SpamEntry(number = number, source = "bundled_arcep"))
                        }
                        added++
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Bundled list error: ${e.message}")
            return@withContext SyncResult("bundled", 0, e.message)
        }
        SyncResult("bundled", added)
    }

    suspend fun syncFromSupabase(context: Context, country: String): SyncResult = withContext(Dispatchers.IO) {
        val callingCode = com.degage.callscreen.callingCodeFor(country) ?: return@withContext SyncResult("community", 0)
        val numbers = SupabaseClient.fetchSpamNumbers(callingCode)
        if (numbers.isEmpty()) return@withContext SyncResult("community", 0)
        val db = AppDatabase.getInstance(context)
        var added = 0
        numbers.forEach { number ->
            val normalized = number.fromE164(country) ?: return@forEach
            if (normalized.length >= 6) {
                db.spamDao().insert(SpamEntry(number = normalized, source = "community"))
                // Reconfirme la présence même si déjà connu localement (insert ignore ne
                // touche pas la ligne existante) : sans ça, un numéro toujours signalé par
                // la communauté finirait quand même par être purgé comme obsolète.
                db.spamDao().touchLastSeen(normalized)
                added++
            }
        }
        Log.i(TAG, "Supabase sync: +$added")
        SyncResult("community", added)
    }

    // Un numéro non reconfirmé (ni par un appel reçu, ni par la communauté) depuis ce délai
    // est retiré de la base locale : les numéros de téléphone sont recyclés par les
    // opérateurs, et un blocage permanent finirait par viser un nouvel abonné innocent
    // plutôt que le spammeur d'origine.
    private const val STALE_AFTER_MS = 18L * 30 * 24 * 60 * 60 * 1000

    suspend fun purgeStaleEntries(context: Context): Int = withContext(Dispatchers.IO) {
        val removed = AppDatabase.getInstance(context).spamDao()
            .purgeStale(System.currentTimeMillis() - STALE_AFTER_MS)
        if (removed > 0) Log.i(TAG, "Purge: -$removed numéros obsolètes")
        removed
    }

    /** Normalise un numéro : supprime espaces/tirets, convertit +33 → 0 */
    private fun String.normalizeNumber(): String {
        val clean = this.replace("[^0-9+]".toRegex(), "")
        return when {
            clean.startsWith("+33") -> "0" + clean.drop(3)
            clean.startsWith("0033") -> "0" + clean.drop(4)
            else -> clean
        }
    }
}
