package com.degage.replies

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.degage.R

enum class MessagePart(val label: String, val emoji: String) {
    SALUTATION("Salutation", "👋"),
    BODY("Corps du message", "💬"),
    ENDING("Formule de fin", "🚪")
}

/** Libellé de la partie de message traduit dans la langue de l'interface. */
@Composable
fun MessagePart.localizedLabel(): String = when (this) {
    MessagePart.SALUTATION -> stringResource(R.string.msgpart_salutation)
    MessagePart.BODY -> stringResource(R.string.msgpart_body)
    MessagePart.ENDING -> stringResource(R.string.msgpart_ending)
}

// modeName utilisé pour les salutations et formules de fin (partagées entre les modes)
const val MODE_GLOBAL = "GLOBAL"
