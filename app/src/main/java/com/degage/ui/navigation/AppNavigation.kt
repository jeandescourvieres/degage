package com.degage.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import com.degage.R

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Modes : Screen("modes")
    data object ReadyMadeModes : Screen("ready_made_modes")
    data object Statistics : Screen("statistics")
    data object History : Screen("history")
    data object Settings : Screen("settings") {
        const val routeWithArgs = "settings?openFaq={openFaq}"
        fun withFaq() = "settings?openFaq=true"
    }
    data object Replies : Screen("replies/{modeName}") {
        fun withMode(mode: String) = "replies/$mode"
    }
    data object About : Screen("about")
    data object CustomBlocks : Screen("custom_blocks")
    data object MessageBuilder : Screen("message_builder")
    data object VoiceSettings : Screen("voice_settings")
    data object Manual : Screen("manual")
    data object UserGuide : Screen("user_guide")
    data object WelcomeDetails : Screen("welcome_details")
    data object Dashboard : Screen("dashboard")
    data object Premium : Screen("premium")
}

data class BottomNavItem(
    val screen: Screen,
    @StringRes val labelRes: Int,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, R.string.nav_home, Icons.Default.Home),
    BottomNavItem(Screen.Modes, R.string.nav_modes, Icons.Default.Tune),
    BottomNavItem(Screen.Statistics, R.string.nav_statistics, Icons.Default.BarChart),
    BottomNavItem(Screen.History, R.string.nav_history, Icons.Default.History),
    BottomNavItem(Screen.UserGuide, R.string.nav_guide, Icons.Default.MenuBook),
    BottomNavItem(Screen.Settings, R.string.nav_settings, Icons.Default.Settings),
)
