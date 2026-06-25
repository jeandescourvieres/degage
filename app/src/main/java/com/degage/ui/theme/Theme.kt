package com.degage.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

val NeonGreen = Color(0xFF39FF14)
val NeonGreenDim = Color(0xFF1A7A00)
// Couleur de fond de l'app, personnalisable depuis Parametres (synchronisee avec
// AppPreferences.backgroundColor) — declaree mutable pour que tous les ecrans qui la
// referencent directement se recomposent automatiquement quand l'utilisateur la change.
var DarkBg by mutableStateOf(Color(0xFF0A0A0A))
val CardBg = Color(0xFF141414)
val CardBgAlt = Color(0xFF1C1C1C)
val RedAlert = Color(0xFFE53935)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFBBBBBB)

// Couleurs d'accentuation pour égayer les sections de l'écran de bienvenue
val AccentBlue = Color(0xFF3B9DFF)
val AccentPurple = Color(0xFFB084FF)
val AccentOrange = Color(0xFFFFA53B)
val AccentPink = Color(0xFFFF6FB0)
val AccentCyan = Color(0xFF00E5FF)
val AccentYellow = Color(0xFFFFD60A)

@Composable
fun DegageTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = NeonGreen,
        onPrimary = Color.Black,
        primaryContainer = NeonGreenDim,
        onPrimaryContainer = NeonGreen,
        secondary = NeonGreenDim,
        background = DarkBg,
        surface = CardBg,
        surfaceVariant = CardBgAlt,
        onBackground = TextPrimary,
        onSurface = TextPrimary,
        onSurfaceVariant = TextSecondary,
        error = RedAlert,
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = DegageTypography,
        content = content
    )
}
