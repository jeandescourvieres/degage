package com.degage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.degage.modes.AppMode
import com.degage.replies.MessagePart
import com.degage.ui.navigation.Screen
import com.degage.ui.navigation.bottomNavItems
import com.degage.ui.screens.*
import com.degage.ui.screens.ManualScreen
import com.degage.ui.theme.CardBg
import com.degage.ui.theme.DarkBg
import com.degage.ui.theme.NeonGreen
import com.degage.ui.theme.TextSecondary
import com.degage.ui.viewmodel.MainViewModel
import com.degage.ui.viewmodel.VoiceSettingsViewModel

@Composable
fun DegageApp(
    viewModel: MainViewModel = viewModel(),
    onboardingDone: Boolean,
    welcomeShown: Boolean,
    onOnboardingComplete: () -> Unit,
    onWelcomeDismiss: () -> Unit,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isEnabled by viewModel.isEnabled.collectAsStateWithLifecycle()
    val activeMode by viewModel.activeMode.collectAsStateWithLifecycle()
    val totalBlocked by viewModel.totalBlocked.collectAsStateWithLifecycle()
    val todayCount by viewModel.todayCount.collectAsStateWithLifecycle()
    val timeSaved by viewModel.timeSavedMinutes.collectAsStateWithLifecycle()
    val allCalls by viewModel.allCalls.collectAsStateWithLifecycle()
    val autoReject by viewModel.autoReject.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val blockAfterReply by viewModel.blockAfterReply.collectAsStateWithLifecycle()
    val spamDbCount by viewModel.spamDbCount.collectAsStateWithLifecycle()
    val lastSpamSync by viewModel.lastSpamSync.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val monitorLive by viewModel.monitorLive.collectAsStateWithLifecycle()
    val contributeDb by viewModel.contributeDb.collectAsStateWithLifecycle()
    val blockHiddenNumbers by viewModel.blockHiddenNumbers.collectAsStateWithLifecycle()
    val country by viewModel.country.collectAsStateWithLifecycle()
    val customBlocks by viewModel.customBlocks.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()

    val startDestination = when {
        !onboardingDone -> Screen.Onboarding.route
        !welcomeShown -> Screen.Welcome.route
        else -> Screen.Home.route
    }
    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.screen.route }

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = CardBg, tonalElevation = 0.dp) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NeonGreen,
                                selectedTextColor = NeonGreen,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(onStart = {
                        onOnboardingComplete()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    })
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        isEnabled = isEnabled,
                        totalBlocked = totalBlocked,
                        todayCount = todayCount,
                        timeSavedMinutes = timeSaved,
                        activeMode = activeMode.label,
                        recentCalls = allCalls,
                        onToggle = viewModel::toggleEnabled,
                        onNavigateSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateHistory = { navController.navigate(Screen.History.route) }
                    )
                }

                composable(Screen.Modes.route) {
                    ModesScreen(
                        activeMode = activeMode,
                        onSelectMode = viewModel::setMode,
                        onPreviewMode = { mode ->
                            viewModel.previewMode(mode)
                        },
                        isPremium = isPremium,
                        onUpgrade = { navController.navigate(Screen.Premium.route) },
                        onBack = { navController.navigateUp() }
                    )
                }

                composable(Screen.Statistics.route) {
                    StatisticsScreen(
                        totalBlocked = totalBlocked,
                        todayCount = todayCount,
                        timeSavedMinutes = timeSaved,
                        spamDbCount = spamDbCount,
                        lastSpamSync = lastSpamSync,
                        onBack = { navController.navigateUp() }
                    )
                }

                composable(Screen.History.route) {
                    HistoryScreen(
                        calls = allCalls,
                        onDelete = viewModel::deleteHistoryEntry,
                        onMarkNotSpam = viewModel::markNotSpam,
                        isPremium = isPremium,
                        onUpgrade = { navController.navigate(Screen.Premium.route) },
                        onBack = { navController.navigateUp() }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        isEnabled = isEnabled,
                        autoReject = autoReject,
                        blockAfterReply = blockAfterReply,
                        notifications = notifications,
                        monitorLive = monitorLive,
                        contributeDb = contributeDb,
                        blockHiddenNumbers = blockHiddenNumbers,
                        country = country,
                        isPremium = isPremium,
                        onUpgrade = { navController.navigate(Screen.Premium.route) },
                        onToggleEnabled = viewModel::toggleEnabled,
                        onToggleAutoReject = { viewModel.setAutoReject(!autoReject) },
                        onToggleBlockAfterReply = { viewModel.setBlockAfterReply(!blockAfterReply) },
                        onToggleNotifications = { viewModel.setNotifications(!notifications) },
                        onToggleMonitorLive = { viewModel.setMonitorLive(!monitorLive) },
                        onToggleContributeDb = { viewModel.setContributeDb(!contributeDb) },
                        onToggleBlockHiddenNumbers = { viewModel.setBlockHiddenNumbers(!blockHiddenNumbers) },
                        onSetCountry = { viewModel.setCountry(it) },
                        onNavigateAbout = { navController.navigate(Screen.About.route) },
                        onNavigateMessageBuilder = { navController.navigate(Screen.MessageBuilder.route) },
                        onNavigateVoiceSettings = { navController.navigate(Screen.VoiceSettings.route) },
                        onNavigateManual = { navController.navigate(Screen.Manual.route) },
                        onNavigateCustomBlocks = { navController.navigate(Screen.CustomBlocks.route) },
                        onNavigateWelcome = { navController.navigate(Screen.Welcome.route) },
                        onSyncSpamList = viewModel::syncSpamList,
                        isSyncing = isSyncing,
                        onBack = { navController.navigateUp() }
                    )
                }

                composable(Screen.Replies.route) { backStackEntry ->
                    val modeName = backStackEntry.arguments?.getString("modeName") ?: AppMode.POLI.name
                    val mode = runCatching { AppMode.valueOf(modeName) }.getOrDefault(AppMode.POLI)
                    val replies by viewModel.getRepliesForMode(mode).collectAsStateWithLifecycle(emptyList())
                    RepliesScreen(
                        mode = mode,
                        replies = replies,
                        onBack = { navController.popBackStack() },
                        onToggle = viewModel::toggleReply,
                        onAdd = { text -> viewModel.addCustomReply(text, mode) },
                        onDelete = viewModel::deleteReply
                    )
                }

                composable(Screen.MessageBuilder.route) {
                    val salutations by viewModel.getSalutations().collectAsStateWithLifecycle(emptyList())
                    val bodies by viewModel.getBodiesForMode(activeMode).collectAsStateWithLifecycle(emptyList())
                    val endings by viewModel.getEndings().collectAsStateWithLifecycle(emptyList())
                    MessageBuilderScreen(
                        activeMode = activeMode,
                        salutations = salutations,
                        bodies = bodies,
                        endings = endings,
                        onBack = { navController.popBackStack() },
                        onSelect = viewModel::selectReply,
                        onAdd = { text, part -> viewModel.addPartItem(text, part, activeMode) },
                        onDelete = viewModel::deleteReply
                    )
                }

                composable(Screen.VoiceSettings.route) {
                    val voiceVm: VoiceSettingsViewModel = viewModel()
                    val voices by voiceVm.voices.collectAsStateWithLifecycle()
                    val rate by voiceVm.speechRate.collectAsStateWithLifecycle()
                    val pitch by voiceVm.pitch.collectAsStateWithLifecycle()
                    val voiceName by voiceVm.selectedVoiceName.collectAsStateWithLifecycle()
                    VoiceSettingsScreen(
                        voices = voices,
                        selectedVoiceName = voiceName,
                        speechRate = rate,
                        pitch = pitch,
                        onBack = { navController.popBackStack() },
                        onSelectVoice = voiceVm::setVoice,
                        onRateChange = voiceVm::setRate,
                        onPitchChange = voiceVm::setPitch,
                        onPreview = voiceVm::previewVoice
                    )
                }

                composable(Screen.Welcome.route) {
                    WelcomeScreen(
                        onDismiss = {
                            onWelcomeDismiss()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        },
                        onBack = if (welcomeShown) ({ navController.popBackStack() }) else null
                    )
                }

                composable(Screen.Manual.route) {
                    ManualScreen(onBack = { navController.popBackStack() })
                }

                composable(Screen.About.route) {
                    AboutScreen(onBack = { navController.popBackStack() })
                }

                composable(Screen.CustomBlocks.route) {
                    CustomBlockScreen(
                        blocks = customBlocks,
                        onAdd = viewModel::addCustomBlock,
                        onDelete = viewModel::deleteCustomBlock,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Premium.route) {
                    PremiumScreen(
                        isPremium = isPremium,
                        onBack = { navController.popBackStack() },
                        onToggleDevPremium = viewModel::setPremium
                    )
                }
            }
        }
    }
}
