package com.degage.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/** Applique la langue d'interface choisie par l'utilisateur (FR/DE/IT, ou "" pour suivre le système). */
object LocaleHelper {
    fun applyLanguage(code: String) {
        val locales = if (code.isBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(code.lowercase())
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
