package com.degage.modes

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.degage.R

enum class AppMode(val label: String, val emoji: String) {
    POLI("Poli", "😇"),
    PRO("Pro", "💼"),
    AMICAL("Amical", "😊"),
    DIRECT("Direct", "⚡"),
    HUMOUR("Humour", "😂"),
    SARCASTIQUE("Sarcastique", "😏"),
    TROLL("Troll", "😈"),
    ROBOT("Robot", "🤖"),
    FROID("Froid", "🥶"),
    CINGLANT("Cinglant", "🔥"),
}

/** Libellé du mode traduit dans la langue de l'interface. */
@Composable
fun AppMode.localizedLabel(): String = when (this) {
    AppMode.POLI -> stringResource(R.string.mode_poli)
    AppMode.PRO -> stringResource(R.string.mode_pro)
    AppMode.AMICAL -> stringResource(R.string.mode_amical)
    AppMode.DIRECT -> stringResource(R.string.mode_direct)
    AppMode.HUMOUR -> stringResource(R.string.mode_humour)
    AppMode.SARCASTIQUE -> stringResource(R.string.mode_sarcastique)
    AppMode.TROLL -> stringResource(R.string.mode_troll)
    AppMode.ROBOT -> stringResource(R.string.mode_robot)
    AppMode.FROID -> stringResource(R.string.mode_froid)
    AppMode.CINGLANT -> stringResource(R.string.mode_cinglant)
}
