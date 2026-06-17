package com.degage.spam

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object SupabaseClient {

    private const val BASE = "https://omnbalnsrvvxnoqskxza.supabase.co/rest/v1"
    private const val FUNCTIONS_BASE = "https://omnbalnsrvvxnoqskxza.supabase.co/functions/v1"
    private const val KEY = "sb_publishable_tqaA4jrnGVPVFEWNVE4rnQ_PJAQlfBK"
    private const val TAG = "SupabaseClient"

    // Ne récupère que les numéros de l'indicatif pays demandé (ex. "33" pour la France), pour
    // éviter de télécharger et de faire correspondre des numéros d'autres pays sans rapport.
    // report_count>=2 : un signalement isolé ne suffit plus à propager un numéro à toute la
    // communauté (l'Edge Function report-number ne compte qu'une fois par IP et par numéro).
    suspend fun fetchSpamNumbers(callingCode: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val conn = (URL("$BASE/spam_numbers?select=number&number=like.%2B$callingCode*&report_count=gte.2").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 20_000
                setRequestProperty("apikey", KEY)
                setRequestProperty("Authorization", "Bearer $KEY")
            }
            val code = conn.responseCode
            if (code != 200) {
                Log.w(TAG, "fetch HTTP $code")
                conn.disconnect()
                return@withContext emptyList()
            }
            val body = BufferedReader(InputStreamReader(conn.inputStream)).readText()
            conn.disconnect()
            val arr = JSONArray(body)
            List(arr.length()) { arr.getJSONObject(it).getString("number") }
        } catch (e: Exception) {
            Log.w(TAG, "fetch failed: ${e.message}")
            emptyList()
        }
    }

    // Passe par l'Edge Function plutôt qu'un insert REST direct : la table n'accepte plus
    // d'écriture anon (RLS), la validation et la dédup anti-abus par IP se font côté serveur
    // avec la clé service_role, jamais exposée au client.
    suspend fun reportNumber(number: String): Unit = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("number", number)
            }.toString().toByteArray(Charsets.UTF_8)

            val conn = (URL("$FUNCTIONS_BASE/report-number").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("apikey", KEY)
                setRequestProperty("Authorization", "Bearer $KEY")
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                outputStream.write(json)
            }
            Log.d(TAG, "report $number: HTTP ${conn.responseCode}")
            conn.disconnect()
        } catch (e: Exception) {
            Log.w(TAG, "report failed: ${e.message}")
        }
    }
}
