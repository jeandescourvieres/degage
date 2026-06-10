package com.degage.replies

import com.degage.modes.AppMode

/**
 * Messages vocaux en allemand, pour la Suisse alémanique.
 * Contrairement aux réponses françaises (modifiables via la base Room),
 * ces phrases sont fixes pour la version 1 de la langue allemande.
 */
object GermanReplies {

    private const val SALUTATION = "Guten Tag."
    private const val ENDING = "Auf Wiederhören."

    private val BODIES = mapOf(
        AppMode.POLI to "Diese Nummer wünscht keine Werbeanrufe.",
        AppMode.ADMINISTRATIF to "Dieser Anruf wurde als Werbung eingestuft und automatisch abgelehnt.",
        AppMode.SARCASTIQUE to "Bitte legen Sie auf, bevor es unangenehm wird.",
        AppMode.TROLL to "Bitte warten Sie, Ihr Anruf wird weitergeleitet…",
    )

    fun fullMessage(mode: AppMode): String {
        val body = BODIES[mode] ?: BODIES.getValue(AppMode.POLI)
        return "$SALUTATION $body $ENDING"
    }
}
