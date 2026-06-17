package com.degage.callscreen

// Préfixes français documentés comme sources fréquentes d'appels indésirables.
// Sources : rapports consommateurs ARCEP, signalements Signal-Conso, bases numeroinconnu.fr.
val SPAM_PREFIXES = listOf(

    // ── MVNO virtuels Île-de-France (0162/0163) ──────────────────────────
    // Plages MVNO sans ancrage géographique, massivement exploitées par les centres d'appels
    "0162", "0163",

    // ── VoIP Paris non géographiques (01 70–84) ──────────────────────────
    "0170", "0171", "0172", "0173", "0174",
    "0175", "0176", "0177", "0178", "0179",
    "0180", "0181", "0182", "0183", "0184",

    // ── VoIP régionaux non géographiques ─────────────────────────────────
    "0270", "0271",          // Ouest (Pays de la Loire, Bretagne)
    "0377", "0378",          // Est (Alsace, Lorraine, Bourgogne)
    "0424", "0425",          // Sud-Est (PACA, Rhône-Alpes)
    "0568", "0569",          // Sud-Ouest (Occitanie, Nouvelle-Aquitaine)
    "0375", "0376",          // Est — VoIP Bouygues/SFR

    // ── 09 non géographiques (souvent centres d'appels) ──────────────────
    "0948", "0949",
    "0960", "0961", "0962", "0963", "0964",
    "0970", "0971", "0972", "0973", "0974",
    "0975", "0976", "0977", "0978", "0979",
    "0980", "0981", "0982", "0983", "0984",

    // ── Numéros courts surtaxés réaffectés ───────────────────────────────
    "0890", "0891", "0892", "0893", "0895",
    "0897", "0898", "0899",

    // ── Indicatifs outre-mer utilisés pour contourner le filtrage ────────
    "0262",  // La Réunion — parfois usurpé
    "0590",  // Guadeloupe — parfois usurpé
    "0594",  // Guyane — parfois usurpé
    "0596",  // Martinique — parfois usurpé
)

// Préfixes suisses documentés comme sources fréquentes d'appels indésirables.
// Source : OFCOM/BAKOM (plages de numéros à valeur ajoutée et numéros d'entreprise nationaux).
val SPAM_PREFIXES_CH = listOf(

    // ── Numéros à valeur ajoutée (0900/0901/0906) ────────────────────────
    // Attribués individuellement par l'OFCOM pour le business/marketing/divertissement
    "0900", "0901", "0906",

    // ── Numéros d'entreprise nationaux à coût partagé (084x/0878) ────────
    // Plages effectivement allouées par l'OFCOM (0840, 0842, 0844, 0848, 0878),
    // utilisées comme numéros de redirection par des centres d'appels et hotlines
    "0840", "0842", "0844", "0848", "0878",
)

// Préfixes espagnols documentés comme sources fréquentes d'appels indésirables.
// Source : plan de numérotation CNMC — tranches à tarification additionnelle.
// Note : pas de 0 de tronc en Espagne, les numéros nationaux s'écrivent sans préfixe 0.
val SPAM_PREFIXES_ES = listOf(
    // ── Tarification additionnelle (803/806/807/907) ─────────────────────
    // Tranches étroites réservées aux services à valeur ajoutée/adultes, peu utilisées
    // par des entreprises légitimes — contrairement aux mobiles 6xx/7xx, trop larges pour être fiables.
    "803", "806", "807", "907",
)

// ── Mode strict : plages entières des opérateurs VoIP non géographiques ─────
// Mêmes plages que celles bloquées intégralement par certaines apps concurrentes
// (utilisées par Aircall, Vonage, Manifone, Tata Communications, CM Telecom…).
// Couvre TOUTE la plage, y compris les numéros d'entreprises légitimes qui les utilisent.
val SPAM_PREFIXES_STRICT_FR: List<String> = buildList {
    for (i in 70..89) add("01$i")          // 01 70–89 : VoIP non géographique national
    for (i in 70..79) {                    // X0 70–79 régionaux : VoIP non géographique
        add("02$i")
        add("03$i")
        add("04$i")
        add("05$i")
    }
    for (i in 40..99) add("09$i")          // 09 40–99 : VoIP/centres d'appels non géographiques
}

// ── Mode strict Suisse : plages à valeur ajoutée et 084x élargies ───────────
val SPAM_PREFIXES_STRICT_CH: List<String> = buildList {
    for (i in 0..9) add("084$i")
    add("0878")
    for (i in 0..9) add("090$i")
}

// ── Mode strict Espagne : tarification spéciale 901/902 ─────────────────────
// Coût partagé, légitimes pour du service client mais aussi très exploités par
// des centres d'appels — d'où le rattachement au mode strict plutôt qu'au défaut.
val SPAM_PREFIXES_STRICT_ES = listOf("901", "902")

private val UNKNOWN_MARKERS = listOf(
    "inconnu", "unknown", "privé", "private",
    "masqué", "hidden", "anonymous", "withheld"
)

fun String.isSpamNumber(country: String = "FR"): Boolean {
    val normalized = this.replace(" ", "").replace("-", "").replace(".", "")
    val prefixes = when (country) {
        "CH" -> SPAM_PREFIXES_CH
        "FR" -> SPAM_PREFIXES
        "ES" -> SPAM_PREFIXES_ES
        else -> emptyList() // UK/DE/IT : pas de préfixes fiables, on s'appuie sur la base communautaire
    }
    return prefixes.any { normalized.startsWith(it) }
}

fun String.isStrictVoipNumber(country: String = "FR"): Boolean {
    val normalized = this.replace(" ", "").replace("-", "").replace(".", "")
    val prefixes = when (country) {
        "CH" -> SPAM_PREFIXES_STRICT_CH
        "FR" -> SPAM_PREFIXES_STRICT_FR
        "ES" -> SPAM_PREFIXES_STRICT_ES
        else -> emptyList()
    }
    return prefixes.any { normalized.startsWith(it) }
}

fun String?.isUnknownNumber(): Boolean {
    if (isNullOrBlank()) return true
    val lower = this.lowercase().trim()
    return UNKNOWN_MARKERS.any { lower.contains(it) }
}

fun String?.displayNumber(): String =
    if (isNullOrBlank() || this.isUnknownNumber()) "📵 Numéro masqué" else this

fun String?.normalizeNumber(): String =
    this?.replace(" ", "")?.replace("-", "")?.replace(".", "") ?: ""
