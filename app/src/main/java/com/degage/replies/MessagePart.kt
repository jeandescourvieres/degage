package com.degage.replies

enum class MessagePart(val label: String, val emoji: String) {
    SALUTATION("Salutation", "👋"),
    BODY("Corps du message", "💬"),
    ENDING("Formule de fin", "🚪")
}

// modeName utilisé pour les salutations et formules de fin (partagées entre les modes)
const val MODE_GLOBAL = "GLOBAL"
