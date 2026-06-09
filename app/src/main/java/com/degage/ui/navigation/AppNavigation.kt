package com.degage.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Modes : Screen("modes")
    data object Statistics : Screen("statistics")
    data object History : Screen("history")
    data object Settings : Screen("settings")
    data object Replies : Screen("replies/{modeName}") {
        fun withMode(mode: String) = "replies/$mode"
    }
    data object About : Screen("about")
    data object AddNumber : Screen("add_number")
    data object Notifications : Screen("notifications")
    data object MessageBuilder : Screen("message_builder")
    data object VoiceSettings : Screen("voice_settings")
    data object Manual : Screen("manual")
    data object Welcome : Screen("welcome")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Accueil", Icons.Default.Home),
    BottomNavItem(Screen.Modes, "Modes", Icons.Default.Tune),
    BottomNavItem(Screen.Statistics, "Statistiques", Icons.Default.BarChart),
    BottomNavItem(Screen.History, "Historique", Icons.Default.History),
    BottomNavItem(Screen.Settings, "Paramètres", Icons.Default.Settings),
)
