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
    private const val KEY = "sb_publishable_tqaA4jrnGVPVFEWNVE4rnQ_PJAQlfBK"
    private const val TAG = "SupabaseClient"

    // Ne récupère que les numéros de l'indicatif pays demandé (ex. "33" pour la France), pour
    // éviter de télécharger et de faire correspondre des numéros d'autres pays sans rapport.
    suspend fun fetchSpamNumbers(callingCode: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val conn = (URL("$BASE/spam_numbers?select=number&number=like.%2B$callingCode*").openConnection() as HttpURLConnection).apply {
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

    suspend fun reportNumber(number: String): Unit = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            val json = JSONObject().apply {
                put("number", number)
                put("report_count", 1)
                put("first_seen", now)
                put("last_seen", now)
            }.toString().toByteArray(Charsets.UTF_8)

            val conn = (URL("$BASE/spam_numbers").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("apikey", KEY)
                setRequestProperty("Authorization", "Bearer $KEY")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Prefer", "resolution=ignore-duplicates")
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
