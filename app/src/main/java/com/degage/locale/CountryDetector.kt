package com.degage.locale

import android.content.Context
import android.telephony.TelephonyManager
import java.util.Locale

/** Détecte le pays "local" de l'utilisateur (carte SIM, puis région système) pour savoir lequel des pays pris en charge est inclus gratuitement. */
object CountryDetector {
    private val SUPPORTED = setOf("FR", "CH", "UK", "DE", "IT", "ES")

    private fun toSupportedCode(iso: String?): String? {
        val code = when (iso?.uppercase(Locale.ROOT)) {
            "GB" -> "UK"
            else -> iso?.uppercase(Locale.ROOT)
        }
        return code?.takeIf { it in SUPPORTED }
    }

    fun detectHomeCountry(context: Context): String {
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        toSupportedCode(telephony?.simCountryIso)?.let { return it }
        toSupportedCode(Locale.getDefault().country)?.let { return it }
        return "FR"
    }
}
