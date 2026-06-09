package com.degage.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NeonGreen = Color(0xFF39FF14)
val NeonGreenDim = Color(0xFF1A7A00)
val DarkBg = Color(0xFF0A0A0A)
val CardBg = Color(0xFF141414)
val CardBgAlt = Color(0xFF1C1C1C)
val RedAlert = Color(0xFFE53935)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF8A8A8A)

private val DegageColorScheme = darkColorScheme(
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

@Composable
fun DegageTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DegageColorScheme,
        typography = DegageTypography,
        content = content
    )
}
