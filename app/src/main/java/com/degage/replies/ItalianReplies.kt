package com.degage.replies

import com.degage.modes.AppMode

/**
 * Messages vocaux en italien, pour la Suisse italophone (Tessin).
 * Comme pour l'allemand, ces phrases sont fixes pour la version 1.
 */
object ItalianReplies {

    private const val SALUTATION = "Buongiorno."
    private const val ENDING = "Arrivederci."

    private val BODIES = mapOf(
        AppMode.POLI to "Questo numero non desidera ricevere chiamate pubblicitarie.",
        AppMode.ADMINISTRATIF to "Questa chiamata è stata classificata come pubblicità e rifiutata automaticamente.",
        AppMode.SARCASTIQUE to "La preghiamo di riagganciare prima che diventi imbarazzante.",
        AppMode.TROLL to "Attenda in linea, la sua chiamata verrà inoltrata…",
    )

    fun fullMessage(mode: AppMode): String {
        val body = BODIES[mode] ?: BODIES.getValue(AppMode.POLI)
        return "$SALUTATION $body $ENDING"
    }
}
