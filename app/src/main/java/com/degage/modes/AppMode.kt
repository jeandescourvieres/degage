package com.degage.modes

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.degage.R

enum class AppMode(val label: String, val emoji: String) {
    POLI("Poli", "😐"),
    ADMINISTRATIF("Administratif", "🤖"),
    SARCASTIQUE("Sarcastique", "😎"),
    TROLL("Troll", "☠️")
}

/** Libellé du mode traduit dans la langue de l'interface. */
@Composable
fun AppMode.localizedLabel(): String = when (this) {
    AppMode.POLI -> stringResource(R.string.mode_poli)
    AppMode.ADMINISTRATIF -> stringResource(R.string.mode_administratif)
    AppMode.SARCASTIQUE -> stringResource(R.string.mode_sarcastique)
    AppMode.TROLL -> stringResource(R.string.mode_troll)
}
