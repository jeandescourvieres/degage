package com.degage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.degage.R
import com.degage.modes.AppMode
import com.degage.modes.localizedLabel
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
    onOnboardingComplete: () -> Unit,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var homeRefreshKey by remember { mutableStateOf(0) }
    var welcomeChimePlayed by remember { mutableStateOf(false) }

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
    val strictMode by viewModel.strictMode.collectAsStateWithLifecycle()
    val country by viewModel.country.collectAsStateWithLifecycle()
    val homeCountry by viewModel.homeCountry.collectAsStateWithLifecycle()
    val welcomeMusic by viewModel.welcomeMusic.collectAsStateWithLifecycle()
    val modeFullTexts by viewModel.modeFullTexts.collectAsStateWithLifecycle()
    val customBlocks by viewModel.customBlocks.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val isPremiumUnlocked by viewModel.isPremiumUnlocked.collectAsStateWithLifecycle()
    val trialDaysRemaining by viewModel.trialDaysRemaining.collectAsStateWithLifecycle()
    val replyLanguage by viewModel.replyLanguage.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val backgroundColorArgb by viewModel.backgroundColor.collectAsStateWithLifecycle()
    LaunchedEffect(backgroundColorArgb) { DarkBg = Color(backgroundColorArgb) }
    val bgColorTipSeen by viewModel.bgColorTipSeen.collectAsStateWithLifecycle()

    val startDestination = when {
        !onboardingDone -> Screen.Onboarding.route
        else -> Screen.Home.route
    }
    val showBottomBar = currentDestination?.route != Screen.Onboarding.route

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            if (showBottomBar) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBg)
                        .navigationBarsPadding()
                        .height(52.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route?.substringBefore("?") == item.screen.route } == true
                        val tint = if (selected) NeonGreen else TextSecondary
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                                .clickable {
                                    if (item.screen.route == Screen.Home.route && selected) {
                                        homeRefreshKey++
                                    } else {
                                        navController.navigate(item.screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                item.icon,
                                contentDescription = stringResource(item.labelRes),
                                tint = tint,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                stringResource(item.labelRes),
                                color = tint,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
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
                        refreshKey = homeRefreshKey,
                        isEnabled = isEnabled,
                        activeMode = activeMode.localizedLabel(),
                        onToggle = viewModel::toggleEnabled,
                        onNavigateDetails = { navController.navigate(Screen.WelcomeDetails.route) },
                        onNavigateSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateFaq = { navController.navigate(Screen.Manual.route) },
                        onNavigateDashboard = { navController.navigate(Screen.Dashboard.route) },
                        onNavigateModes = { navController.navigate(Screen.Modes.route) },
                        onNavigateVoiceSettings = { navController.navigate(Screen.VoiceSettings.route) },
                        appLanguage = appLanguage,
                        onSetAppLanguage = { viewModel.setAppLanguage(it) },
                        welcomeMusicEnabled = welcomeMusic,
                        shouldPlayWelcomeChime = !welcomeChimePlayed,
                        onWelcomeChimePlayed = { welcomeChimePlayed = true },
                        showBackgroundColorTip = !bgColorTipSeen,
                        onBackgroundColorTipDismissed = { viewModel.markBgColorTipSeen() }
                    )
                }

                composable(Screen.Modes.route) {
                    val salutations by viewModel.getSalutations().collectAsStateWithLifecycle(emptyList())
                    val bodies by viewModel.getBodiesForMode(activeMode).collectAsStateWithLifecycle(emptyList())
                    val endings by viewModel.getEndings().collectAsStateWithLifecycle(emptyList())
                    val previewText = remember(salutations, bodies, endings) {
                        val s = salutations.firstOrNull { it.isEnabled }?.text ?: ""
                        val b = bodies.firstOrNull { it.isEnabled }?.text ?: "…"
                        val e = endings.firstOrNull { it.isEnabled }?.text ?: ""
                        listOf(s, b, e).filter { it.isNotBlank() }.joinToString("\n\n")
                    }
                    ModesScreen(
                        onBack = { navController.navigateUp() },
                        onNavigateReadyMadeModes = { navController.navigate(Screen.ReadyMadeModes.route) },
                        onNavigateMessageBuilder = { navController.navigate(Screen.MessageBuilder.route) },
                        previewText = previewText,
                        onListenPreview = { text -> viewModel.speakPreview(text) }
                    )
                }

                composable(Screen.ReadyMadeModes.route) {
                    ReadyMadeModesScreen(
                        activeMode = activeMode,
                        onSelectMode = viewModel::setMode,
                        onPreviewMode = { mode ->
                            viewModel.previewMode(mode)
                        },
                        isPremium = isPremiumUnlocked,
                        onUpgrade = { navController.navigate(Screen.Premium.route) },
                        onBack = { navController.navigateUp() },
                        appLanguage = appLanguage,
                        onSetAppLanguage = { viewModel.setAppLanguage(it) },
                        onNavigateMessageBuilder = { navController.navigate(Screen.MessageBuilder.route) },
                        modeFullTexts = modeFullTexts
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
                    val recentUnblockedCalls by viewModel.recentUnblockedCalls.collectAsStateWithLifecycle()
                    HistoryScreen(
                        calls = allCalls,
                        onDelete = viewModel::deleteHistoryEntry,
                        onMarkNotSpam = viewModel::markNotSpam,
                        recentUnblockedCalls = recentUnblockedCalls,
                        onLoadRecentUnblocked = viewModel::loadRecentUnblockedCalls,
                        onBlockRecentCall = viewModel::blockRecentCall,
                        onNavigateCustomBlocks = { navController.navigate(Screen.CustomBlocks.route) },
                        isPremium = isPremiumUnlocked,
                        onUpgrade = { navController.navigate(Screen.Premium.route) },
                        onBack = { navController.navigateUp() }
                    )
                }

                composable(
                    Screen.Settings.routeWithArgs,
                    arguments = listOf(navArgument("openFaq") { type = NavType.BoolType; defaultValue = false })
                ) { backStackEntry ->
                    val openFaq = backStackEntry.arguments?.getBoolean("openFaq") ?: false
                    SettingsScreen(
                        initialShowInfo = openFaq,
                        isEnabled = isEnabled,
                        autoReject = autoReject,
                        blockAfterReply = blockAfterReply,
                        notifications = notifications,
                        monitorLive = monitorLive,
                        contributeDb = contributeDb,
                        blockHiddenNumbers = blockHiddenNumbers,
                        strictMode = strictMode,
                        welcomeMusic = welcomeMusic,
                        country = country,
                        homeCountry = homeCountry,
                        replyLanguage = replyLanguage,
                        appLanguage = appLanguage,
                        backgroundColor = backgroundColorArgb,
                        onSetBackgroundColor = { viewModel.setBackgroundColor(it) },
                        onResetBgColorTip = { viewModel.resetBgColorTipSeen() },
                        isPremium = isPremiumUnlocked,
                        onUpgrade = { navController.navigate(Screen.Premium.route) },
                        onToggleEnabled = viewModel::toggleEnabled,
                        onToggleAutoReject = { viewModel.setAutoReject(!autoReject) },
                        onToggleBlockAfterReply = { viewModel.setBlockAfterReply(!blockAfterReply) },
                        onToggleNotifications = { viewModel.setNotifications(!notifications) },
                        onToggleMonitorLive = { viewModel.setMonitorLive(!monitorLive) },
                        onToggleContributeDb = { viewModel.setContributeDb(!contributeDb) },
                        onToggleBlockHiddenNumbers = { viewModel.setBlockHiddenNumbers(!blockHiddenNumbers) },
                        onToggleStrictMode = { viewModel.setStrictMode(!strictMode) },
                        onToggleWelcomeMusic = { viewModel.setWelcomeMusic(!welcomeMusic) },
                        onSetCountry = { viewModel.setCountry(it) },
                        onSetReplyLanguage = { viewModel.setReplyLanguage(it) },
                        onSetAppLanguage = { viewModel.setAppLanguage(it) },
                        onNavigateAbout = { navController.navigate(Screen.About.route) },
                        onNavigateMessageBuilder = { navController.navigate(Screen.MessageBuilder.route) },
                        onNavigateVoiceSettings = { navController.navigate(Screen.VoiceSettings.route) },
                        onNavigateManual = { navController.navigate(Screen.Manual.route) },
                        onNavigateCustomBlocks = { navController.navigate(Screen.CustomBlocks.route) },
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
                        replyLanguage = replyLanguage,
                        onBack = { navController.popBackStack() },
                        onSelect = viewModel::selectReply,
                        onAdd = { text, part -> viewModel.addPartItem(text, part, activeMode) },
                        onDelete = viewModel::deleteReply,
                        onNavigateReadyMadeModes = { navController.navigate(Screen.ReadyMadeModes.route) },
                        onListenPreview = { text -> viewModel.speakPreview(text) }
                    )
                }

                composable(Screen.VoiceSettings.route) {
                    val voiceVm: VoiceSettingsViewModel = viewModel()
                    val voices by voiceVm.voices.collectAsStateWithLifecycle()
                    val rate by voiceVm.speechRate.collectAsStateWithLifecycle()
                    val pitch by voiceVm.pitch.collectAsStateWithLifecycle()
                    val voiceName by voiceVm.selectedVoiceName.collectAsStateWithLifecycle()
                    val previewingVoiceName by voiceVm.previewingVoiceName.collectAsStateWithLifecycle()
                    val replyLanguage by voiceVm.replyLanguage.collectAsStateWithLifecycle()
                    val replyLanguageLabel = when (replyLanguage) {
                        "DE" -> stringResource(R.string.reply_lang_de)
                        "IT" -> stringResource(R.string.reply_lang_it)
                        "EN" -> stringResource(R.string.reply_lang_en)
                        "ES" -> stringResource(R.string.reply_lang_es)
                        else -> stringResource(R.string.reply_lang_fr)
                    }
                    VoiceSettingsScreen(
                        voices = voices,
                        selectedVoiceName = voiceName,
                        previewingVoiceName = previewingVoiceName,
                        speechRate = rate,
                        pitch = pitch,
                        replyLanguageLabel = replyLanguageLabel,
                        onBack = { navController.popBackStack() },
                        onSelectVoice = voiceVm::setVoice,
                        onRateChange = voiceVm::setRate,
                        onPitchChange = voiceVm::setPitch,
                        onPreview = voiceVm::previewVoice
                    )
                }

                composable(Screen.WelcomeDetails.route) {
                    WelcomeDetailsScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateFaq = { navController.navigate(Screen.Manual.route) }
                    )
                }

                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        isEnabled = isEnabled,
                        totalBlocked = totalBlocked,
                        timeSavedLabel = if (timeSaved / 60 > 0) stringResource(R.string.stats_time_saved_hours, timeSaved / 60, timeSaved % 60) else stringResource(R.string.stats_time_saved_minutes, timeSaved % 60),
                        activeMode = activeMode.localizedLabel(),
                        recentCalls = allCalls,
                        onToggle = viewModel::toggleEnabled,
                        onNavigateHistory = { navController.navigate(Screen.History.route) },
                        onBack = { navController.popBackStack() }
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
                        trialDaysRemaining = trialDaysRemaining,
                        onBack = { navController.popBackStack() },
                        onToggleDevPremium = viewModel::setPremium
                    )
                }
            }
        }
    }
}
