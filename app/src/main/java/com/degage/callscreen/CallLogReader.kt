package com.degage.callscreen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import androidx.core.content.ContextCompat

data class RecentCallEntry(
    val number: String,
    val normalized: String,
    val timestamp: Long,
)

/** Lit les derniers appels entrants/manqués du journal d'appels du téléphone. */
fun Context.getRecentIncomingCalls(limit: Int = 20): List<RecentCallEntry> {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
        return emptyList()
    }
    val result = mutableListOf<RecentCallEntry>()
    val projection = arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE)
    contentResolver.query(
        CallLog.Calls.CONTENT_URI,
        projection,
        null,
        null,
        "${CallLog.Calls.DATE} DESC"
    )?.use { cursor ->
        val numberIdx = cursor.getColumnIndex(CallLog.Calls.NUMBER)
        val dateIdx = cursor.getColumnIndex(CallLog.Calls.DATE)
        val typeIdx = cursor.getColumnIndex(CallLog.Calls.TYPE)
        while (cursor.moveToNext() && result.size < limit) {
            val type = cursor.getInt(typeIdx)
            if (type != CallLog.Calls.INCOMING_TYPE && type != CallLog.Calls.MISSED_TYPE) continue
            val number = cursor.getString(numberIdx)
            if (number.isUnknownNumber()) continue
            val normalized = number.normalizeNumber()
            if (normalized.length < 6) continue
            result.add(RecentCallEntry(number = number!!, normalized = normalized, timestamp = cursor.getLong(dateIdx)))
        }
    }
    return result
}
