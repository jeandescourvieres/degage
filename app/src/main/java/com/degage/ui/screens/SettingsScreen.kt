package com.degage.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.degage.ui.components.InfoDialog
import com.degage.ui.components.PremiumBadge
import com.degage.ui.components.highlightBrand
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.R
import com.degage.ui.theme.*

@Composable
fun SettingsScreen(
    isEnabled: Boolean,
    autoReject: Boolean,
    blockAfterReply: Boolean,
    notifications: Boolean,
    monitorLive: Boolean = false,
    contributeDb: Boolean = false,
    blockHiddenNumbers: Boolean = false,
    strictMode: Boolean = false,
    country: String = "FR",
    replyLanguage: String = "FR",
    appLanguage: String = "",
    isPremium: Boolean = true,
    onUpgrade: () -> Unit = {},
    onToggleEnabled: () -> Unit,
    onToggleAutoReject: () -> Unit,
    onToggleBlockAfterReply: () -> Unit,
    onToggleNotifications: () -> Unit,
    onToggleMonitorLive: () -> Unit = {},
    onToggleContributeDb: () -> Unit = {},
    onToggleBlockHiddenNumbers: () -> Unit = {},
    onToggleStrictMode: () -> Unit = {},
    onSetCountry: (String) -> Unit = {},
    onSetReplyLanguage: (String) -> Unit = {},
    onSetAppLanguage: (String) -> Unit = {},
    onNavigateAbout: () -> Unit,
    onNavigateMessageBuilder: () -> Unit,
    onNavigateVoiceSettings: () -> Unit,
    onNavigateManual: () -> Unit,
    onNavigateCustomBlocks: () -> Unit = {},
    onSyncSpamList: () -> Unit = {},
    isSyncing: Boolean = false,
    initialShowInfo: Boolean = false,
    onBack: () -> Unit = {},
) {
    var showInfo by remember { mutableStateOf(initialShowInfo) }
    if (showInfo) InfoDialog(
        title = stringResource(R.string.settings_info_title),
        content = stringResource(R.string.settings_info_content),
        onDismiss = { showInfo = false }
    )

    var searchQuery by remember { mutableStateOf("") }
    fun matches(vararg texts: String) =
        searchQuery.isBlank() || texts.any { it.contains(searchQuery, ignoreCase = true) }

    val labelAppLang = stringResource(R.string.settings_app_lang_label)
    val labelCountry = stringResource(R.string.settings_country_label)
    val labelReplyLang = stringResource(R.string.settings_reply_lang_label)
    val labelProtection = stringResource(R.string.settings_toggle_protection)
    val labelAutoReject = stringResource(R.string.settings_toggle_auto_reject)
    val labelBlockAfterReply = stringResource(R.string.settings_toggle_block_after_reply)
    val labelNotifications = stringResource(R.string.settings_toggle_notifications)
    val labelBlockHidden = stringResource(R.string.settings_block_hidden_label)
    val labelStrictMode = stringResource(R.string.settings_strict_mode_label)
    val labelMonitorLive = stringResource(R.string.settings_monitor_live_label)
    val labelContribute = stringResource(R.string.settings_contribute_label)
    val labelMessageBuilder = stringResource(R.string.settings_nav_message_builder)
    val labelVoice = stringResource(R.string.settings_nav_voice)
    val labelCustomBlocks = stringResource(R.string.settings_nav_custom_blocks)
    val labelPremium = stringResource(R.string.settings_nav_premium)
    val labelCommunityDb = stringResource(R.string.settings_community_db_label)
    val labelSync = stringResource(R.string.settings_sync_label)
    val labelManual = stringResource(R.string.settings_nav_manual)
    val labelAbout = stringResource(R.string.settings_nav_about)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = Color.White)
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .background(AccentYellow, RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                IconButton(onClick = { showInfo = true }) {
                    Icon(Icons.Default.Info, contentDescription = stringResource(R.string.cd_help), tint = NeonGreen, modifier = Modifier.size(26.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                placeholder = { Text(stringResource(R.string.settings_search_placeholder), color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonGreen) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = CardBgAlt,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = NeonGreen,
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg
                )
            )
        }

        if (searchQuery.isBlank()) item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    stringResource(R.string.settings_quick_access_title),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                QuickAccessRow(label = labelMessageBuilder, locked = !isPremium, onClick = onNavigateMessageBuilder, onUpgrade = onUpgrade)
                QuickAccessRow(label = labelVoice, locked = !isPremium, onClick = onNavigateVoiceSettings, onUpgrade = onUpgrade)
                QuickAccessRow(label = labelCustomBlocks, locked = !isPremium, onClick = onNavigateCustomBlocks, onUpgrade = onUpgrade)
            }
        }

        if (matches(labelAppLang)) item {
            AppLanguageSelectorRow(language = appLanguage, onSetLanguage = onSetAppLanguage)
        }
        if (matches(labelCountry)) item {
            CountrySelectorRow(country = country, isPremium = isPremium, onSetCountry = onSetCountry, onUpgrade = onUpgrade)
        }
        if (matches(labelReplyLang)) item {
            ReplyLanguageSelectorRow(language = replyLanguage, isPremium = isPremium, onSetLanguage = onSetReplyLanguage, onUpgrade = onUpgrade)
        }
        if (matches(labelProtection, labelAutoReject, labelBlockAfterReply, labelNotifications)) item {
            SettingsGroupHelpCard(
                title = stringResource(R.string.settings_help_protection_title),
                text = stringResource(R.string.settings_help_protection)
            )
        }
        if (matches(labelProtection)) item {
            SettingsToggleRow(label = labelProtection, checked = isEnabled, onToggle = onToggleEnabled)
        }
        if (matches(labelAutoReject)) item {
            SettingsToggleRow(label = labelAutoReject, checked = autoReject, onToggle = onToggleAutoReject)
        }
        if (matches(labelBlockAfterReply)) item {
            SettingsToggleRow(label = labelBlockAfterReply, checked = blockAfterReply, onToggle = onToggleBlockAfterReply)
        }
        if (matches(labelNotifications)) item {
            SettingsToggleRow(label = labelNotifications, checked = notifications, onToggle = onToggleNotifications)
        }
        if (matches(labelBlockHidden)) item {
            BlockHiddenNumbersRow(checked = blockHiddenNumbers, onToggle = onToggleBlockHiddenNumbers)
        }
        if (matches(labelStrictMode)) item {
            StrictModeRow(checked = strictMode, onToggle = onToggleStrictMode)
        }
        if (matches(labelMonitorLive)) item {
            MonitorLiveRow(checked = monitorLive, onToggle = onToggleMonitorLive)
        }
        if (matches(labelContribute)) item {
            ContributeDbRow(checked = contributeDb, isPremium = isPremium, onToggle = onToggleContributeDb, onUpgrade = onUpgrade)
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        if (matches(labelMessageBuilder)) item {
            SettingsNavRow(
                label = highlightBrand(labelMessageBuilder),
                locked = !isPremium,
                helpText = stringResource(R.string.settings_help_message_builder),
                onClick = onNavigateMessageBuilder,
                onUpgrade = onUpgrade
            )
        }
        if (matches(labelVoice)) item {
            SettingsNavRow(
                label = highlightBrand(labelVoice),
                locked = !isPremium,
                helpText = stringResource(R.string.settings_help_voice),
                onClick = onNavigateVoiceSettings,
                onUpgrade = onUpgrade
            )
        }
        if (matches(labelCustomBlocks)) item {
            SettingsNavRow(
                label = highlightBrand(labelCustomBlocks),
                locked = !isPremium,
                helpText = stringResource(R.string.settings_help_custom_blocks),
                onClick = onNavigateCustomBlocks,
                onUpgrade = onUpgrade
            )
        }
        if (matches(labelPremium)) item {
            SettingsNavRow(
                label = highlightBrand(labelPremium),
                helpText = stringResource(R.string.settings_help_premium),
                onClick = onUpgrade
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        if (matches(labelCommunityDb)) item {
            SettingsInfoRow(
                label = labelCommunityDb,
                value = stringResource(R.string.settings_community_db_value),
                helpText = stringResource(R.string.settings_help_community_db)
            )
        }
        if (matches(labelSync)) item {
            SpamSyncRow(isSyncing = isSyncing, onClick = onSyncSpamList)
        }
        if (matches(labelManual)) item {
            SettingsNavRow(label = highlightBrand(labelManual), onClick = onNavigateManual)
        }
        if (matches(labelAbout)) item {
            SettingsNavRow(label = highlightBrand(labelAbout), onClick = onNavigateAbout)
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun SettingsToggleRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 15.sp)
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = NeonGreen,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = CardBgAlt
            )
        )
    }
}

@Composable
fun SettingsInfoRow(label: String, value: String, helpText: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White, fontSize = 15.sp)
            Text(value, color = TextSecondary, fontSize = 13.sp)
        }
        if (helpText != null) {
            SettingsHelpToggle(helpText)
        }
    }
}

@Composable
fun SpamSyncRow(isSyncing: Boolean, onClick: () -> Unit) {
    var showSuccess by remember { mutableStateOf(false) }
    var wasSyncing by remember { mutableStateOf(false) }
    LaunchedEffect(isSyncing) {
        if (wasSyncing && !isSyncing) {
            showSuccess = true
            delay(2500)
            showSuccess = false
        }
        wasSyncing = isSyncing
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NeonGreenDim.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable(enabled = !isSyncing) { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.settings_sync_label), color = NeonGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(
                if (showSuccess) stringResource(R.string.settings_sync_success) else stringResource(R.string.settings_sync_sources),
                color = if (showSuccess) NeonGreen else TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (showSuccess) FontWeight.Bold else FontWeight.Normal
            )
        }
        if (isSyncing) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = NeonGreen,
                strokeWidth = 2.dp
            )
        } else {
            Text(stringResource(R.string.settings_sync_button), color = NeonGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CountrySelectorRow(country: String, isPremium: Boolean = true, onSetCountry: (String) -> Unit, onUpgrade: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(stringResource(R.string.settings_country_label), color = Color.White, fontSize = 15.sp)
        Text(
            stringResource(R.string.settings_country_desc),
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CountryChip(stringResource(R.string.country_fr), selected = country == "FR", onClick = { onSetCountry("FR") })
            val chSelected = country == "CH"
            val chLocked = !isPremium
            CountryChip(
                stringResource(R.string.country_ch),
                selected = chSelected && !chLocked,
                onClick = { if (chLocked) onUpgrade() else onSetCountry("CH") }
            )
            if (chLocked) PremiumBadge()
        }
        SettingsHelpToggle(stringResource(R.string.settings_help_country))
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AppLanguageSelectorRow(language: String, onSetLanguage: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(stringResource(R.string.settings_app_lang_label), color = Color.White, fontSize = 15.sp)
        Text(
            highlightBrand(stringResource(R.string.settings_app_lang_desc)),
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CountryChip(stringResource(R.string.lang_system), selected = language == "", onClick = { onSetLanguage("") })
            CountryChip(stringResource(R.string.lang_fr), selected = language == "FR", onClick = { onSetLanguage("FR") })
            CountryChip(stringResource(R.string.lang_de), selected = language == "DE", onClick = { onSetLanguage("DE") })
            CountryChip(stringResource(R.string.lang_it), selected = language == "IT", onClick = { onSetLanguage("IT") })
            CountryChip(stringResource(R.string.lang_en), selected = language == "EN", onClick = { onSetLanguage("EN") })
        }
        SettingsHelpToggle(stringResource(R.string.settings_help_app_lang))
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ReplyLanguageSelectorRow(language: String, isPremium: Boolean = true, onSetLanguage: (String) -> Unit, onUpgrade: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(stringResource(R.string.settings_reply_lang_label), color = Color.White, fontSize = 15.sp)
        Text(
            highlightBrand(stringResource(R.string.settings_reply_lang_desc)),
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CountryChip(stringResource(R.string.lang_fr), selected = language == "FR", onClick = { onSetLanguage("FR") })
            val locked = !isPremium
            CountryChip(
                stringResource(R.string.lang_de),
                selected = language == "DE" && !locked,
                onClick = { if (locked) onUpgrade() else onSetLanguage("DE") }
            )
            CountryChip(
                stringResource(R.string.lang_it),
                selected = language == "IT" && !locked,
                onClick = { if (locked) onUpgrade() else onSetLanguage("IT") }
            )
            CountryChip(
                stringResource(R.string.lang_en),
                selected = language == "EN" && !locked,
                onClick = { if (locked) onUpgrade() else onSetLanguage("EN") }
            )
            if (locked) PremiumBadge()
        }
        SettingsHelpToggle(stringResource(R.string.settings_help_reply_lang))
    }
}

// Petit accordéon "Pourquoi ce réglage ?" repliable, à placer dans une section de paramètres.
@Composable
fun SettingsHelpToggle(text: String) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.padding(top = 6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { expanded = !expanded }
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                stringResource(if (expanded) R.string.settings_help_hide else R.string.settings_help_show),
                color = NeonGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        AnimatedVisibility(visible = expanded) {
            Text(
                highlightBrand(text),
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// Carte d'introduction repliable pour un groupe de réglages (ex. section Protection).
@Composable
fun SettingsGroupHelpCard(title: String, text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        SettingsHelpToggle(text)
    }
}

@Composable
private fun CountryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        color = if (selected) Color.Black else Color.White,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(if (selected) NeonGreen else CardBgAlt, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun BlockHiddenNumbersRow(checked: Boolean, onToggle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (checked) NeonGreenDim.copy(alpha = 0.15f) else CardBg, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.settings_block_hidden_label), color = Color.White, fontSize = 15.sp)
                Text(
                    stringResource(R.string.settings_block_hidden_desc),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = checked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = NeonGreen,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = CardBgAlt
                )
            )
        }
        SettingsHelpToggle(stringResource(R.string.settings_help_block_hidden))
    }
}

@Composable
fun StrictModeRow(checked: Boolean, onToggle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (checked) RedAlert.copy(alpha = 0.15f) else CardBg, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.settings_strict_mode_label), color = Color.White, fontSize = 15.sp)
                Text(
                    stringResource(R.string.settings_strict_mode_desc),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = checked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = RedAlert,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = CardBgAlt
                )
            )
        }
        SettingsHelpToggle(stringResource(R.string.settings_help_strict_mode))
    }
}

@Composable
fun MonitorLiveRow(checked: Boolean, onToggle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (checked) NeonGreenDim.copy(alpha = 0.15f) else CardBg, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.settings_monitor_live_label), color = Color.White, fontSize = 15.sp)
                Text(
                    stringResource(R.string.settings_monitor_live_desc),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = checked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = NeonGreen,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = CardBgAlt
                )
            )
        }
        SettingsHelpToggle(stringResource(R.string.settings_help_monitor_live))
    }
}

@Composable
fun ContributeDbRow(checked: Boolean, isPremium: Boolean = true, onToggle: () -> Unit, onUpgrade: () -> Unit = {}) {
    val locked = !isPremium
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (checked) NeonGreenDim.copy(alpha = 0.18f) else CardBg,
                RoundedCornerShape(18.dp)
            )
            .border(
                width = if (checked) 1.5.dp else 1.dp,
                color = if (checked) NeonGreen.copy(alpha = 0.6f) else NeonGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(18.dp)
            )
            .then(if (locked) Modifier.clickable { onUpgrade() } else Modifier)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🌍", fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.settings_contribute_label),
                    color = NeonGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    buildAnnotatedString {
                        append(stringResource(R.string.settings_contribute_title_prefix))
                        withStyle(SpanStyle(color = NeonGreen)) { append(stringResource(R.string.app_name)) }
                    },
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 22.sp
                )
            }
            if (locked) {
                PremiumBadge()
            } else {
                Switch(
                    checked = checked,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = NeonGreen,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = CardBgAlt
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            stringResource(R.string.settings_contribute_desc),
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ContributeBadge(stringResource(R.string.contribute_badge_shared))
            ContributeBadge(stringResource(R.string.contribute_badge_optin))
            ContributeBadge(stringResource(R.string.contribute_badge_server))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.settings_contribute_footer),
            color = TextSecondary.copy(alpha = 0.7f),
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun ContributeBadge(label: String) {
    Text(
        label,
        color = NeonGreen,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(NeonGreen.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
fun QuickAccessRow(label: String, locked: Boolean, onClick: () -> Unit, onUpgrade: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AccentCyan, RoundedCornerShape(10.dp))
            .clickable { if (locked) onUpgrade() else onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        if (locked) PremiumBadge() else Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Black)
    }
}

@Composable
fun SettingsNavRow(label: AnnotatedString, locked: Boolean = false, helpText: String? = null, onClick: () -> Unit, onUpgrade: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { if (locked) onUpgrade() else onClick() }
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = if (helpText != null) 0.dp else 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White, fontSize = 15.sp)
            if (locked) PremiumBadge() else Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
        if (helpText != null) {
            Box(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 12.dp)) {
                SettingsHelpToggle(helpText)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun SettingsPreview() {
    DegageTheme {
        SettingsScreen(
            isEnabled = true, autoReject = true, blockAfterReply = true, notifications = true,
            onToggleEnabled = {}, onToggleAutoReject = {}, onToggleBlockAfterReply = {}, onToggleNotifications = {},
            onNavigateAbout = {}, onNavigateMessageBuilder = {}, onNavigateVoiceSettings = {}, onNavigateManual = {}
        )
    }
}
