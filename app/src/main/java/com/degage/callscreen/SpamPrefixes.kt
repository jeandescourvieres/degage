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

private val UNKNOWN_MARKERS = listOf(
    "inconnu", "unknown", "privé", "private",
    "masqué", "hidden", "anonymous", "withheld"
)

fun String.isSpamNumber(country: String = "FR"): Boolean {
    val normalized = this.replace(" ", "").replace("-", "").replace(".", "")
    val prefixes = if (country == "CH") SPAM_PREFIXES_CH else SPAM_PREFIXES
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
