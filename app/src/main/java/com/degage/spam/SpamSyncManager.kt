package com.degage.spam

import android.content.Context
import android.util.Log
import com.degage.database.AppDatabase
import com.degage.database.entities.SpamEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class SyncResult(
    val source: String,
    val added: Int,
    val error: String? = null,
)

object SpamSyncManager {

    private val TAG = "SpamSyncManager"

    // Sources publiques de numéros spam
    private val REMOTE_SOURCES = listOf(
        // phoneblock.net — projet communautaire européen anti-spam (open source)
        RemoteSource(
            id = "phoneblock",
            label = "phoneblock.net",
            url = "https://phoneblock.net/phoneblock/api/blocklist",
            format = LineFormat.ONE_PER_LINE,
        ),
        // signal-spam.fr — association française certifiée de lutte anti-spam
        RemoteSource(
            id = "signal_spam",
            label = "Signal-Spam France",
            url = "https://www.signal-spam.fr/api/numbers/spam.txt",
            format = LineFormat.ONE_PER_LINE,
        ),
    )

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

    suspend fun syncAll(context: Context): List<SyncResult> = withContext(Dispatchers.IO) {
        REMOTE_SOURCES.map { source -> syncSource(context, source) }
    }

    private suspend fun syncSource(context: Context, source: RemoteSource): SyncResult =
        withContext(Dispatchers.IO) {
            var added = 0
            try {
                val db = AppDatabase.getInstance(context)
                val connection = (URL(source.url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10_000
                    readTimeout = 20_000
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "DEGAGE-App/1.0 (Android spam filter)")
                }
                val code = connection.responseCode
                if (code != HttpURLConnection.HTTP_OK) {
                    return@withContext SyncResult(source.label, 0, "HTTP $code")
                }
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.forEachLine { raw ->
                        val line = raw.trim()
                        if (line.isBlank() || line.startsWith("#") || line.startsWith(";")) return@forEachLine
                        val number = when (source.format) {
                            LineFormat.ONE_PER_LINE -> line.split(",", ";", "\t").firstOrNull()
                            LineFormat.CSV_FIRST_COL -> line.split(",").firstOrNull()
                        }?.normalizeNumber() ?: return@forEachLine
                        if (number.length >= 6) {
                            kotlinx.coroutines.runBlocking {
                                db.spamDao().insert(SpamEntry(number = number, source = source.id))
                            }
                            added++
                        }
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.w(TAG, "Sync ${source.label} failed: ${e.message}")
                return@withContext SyncResult(source.label, 0, e.message)
            }
            Log.i(TAG, "Sync ${source.label}: +$added")
            SyncResult(source.label, added)
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

    private data class RemoteSource(
        val id: String,
        val label: String,
        val url: String,
        val format: LineFormat,
    )

    private enum class LineFormat { ONE_PER_LINE, CSV_FIRST_COL }
}
