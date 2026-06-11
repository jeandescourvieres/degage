package com.degage.ui.screens

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onSetCountry: (String) -> Unit = {},
    onSetReplyLanguage: (String) -> Unit = {},
    onSetAppLanguage: (String) -> Unit = {},
    onNavigateAbout: () -> Unit,
    onNavigateMessageBuilder: () -> Unit,
    onNavigateVoiceSettings: () -> Unit,
    onNavigateManual: () -> Unit,
    onNavigateCustomBlocks: () -> Unit = {},
    onNavigateWelcome: () -> Unit = {},
    onSyncSpamList: () -> Unit = {},
    isSyncing: Boolean = false,
    onBack: () -> Unit = {},
) {
    var showInfo by remember { mutableStateOf(false) }
    if (showInfo) InfoDialog(
        title = stringResource(R.string.settings_info_title),
        content = stringResource(R.string.settings_info_content),
        onDismiss = { showInfo = false }
    )
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
                Text(stringResource(R.string.settings_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                IconButton(onClick = { showInfo = true }) {
                    Icon(Icons.Default.Info, contentDescription = stringResource(R.string.cd_help), tint = NeonGreen, modifier = Modifier.size(26.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            AppLanguageSelectorRow(language = appLanguage, onSetLanguage = onSetAppLanguage)
        }
        item {
            CountrySelectorRow(country = country, isPremium = isPremium, onSetCountry = onSetCountry, onUpgrade = onUpgrade)
        }
        item {
            ReplyLanguageSelectorRow(language = replyLanguage, isPremium = isPremium, onSetLanguage = onSetReplyLanguage, onUpgrade = onUpgrade)
        }
        item {
            SettingsToggleRow(label = stringResource(R.string.settings_toggle_protection), checked = isEnabled, onToggle = onToggleEnabled)
        }
        item {
            SettingsToggleRow(label = stringResource(R.string.settings_toggle_auto_reject), checked = autoReject, onToggle = onToggleAutoReject)
        }
        item {
            SettingsToggleRow(label = stringResource(R.string.settings_toggle_block_after_reply), checked = blockAfterReply, onToggle = onToggleBlockAfterReply)
        }
        item {
            SettingsToggleRow(label = stringResource(R.string.settings_toggle_notifications), checked = notifications, onToggle = onToggleNotifications)
        }
        item {
            BlockHiddenNumbersRow(checked = blockHiddenNumbers, onToggle = onToggleBlockHiddenNumbers)
        }
        item {
            MonitorLiveRow(checked = monitorLive, onToggle = onToggleMonitorLive)
        }
        item {
            ContributeDbRow(checked = contributeDb, isPremium = isPremium, onToggle = onToggleContributeDb, onUpgrade = onUpgrade)
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            SettingsNavRow(label = highlightBrand(stringResource(R.string.settings_nav_message_builder)), locked = !isPremium, onClick = onNavigateMessageBuilder, onUpgrade = onUpgrade)
        }
        item {
            SettingsNavRow(label = highlightBrand(stringResource(R.string.settings_nav_voice)), locked = !isPremium, onClick = onNavigateVoiceSettings, onUpgrade = onUpgrade)
        }
        item {
            SettingsNavRow(label = highlightBrand(stringResource(R.string.settings_nav_custom_blocks)), locked = !isPremium, onClick = onNavigateCustomBlocks, onUpgrade = onUpgrade)
        }
        item {
            SettingsNavRow(label = highlightBrand(stringResource(R.string.settings_nav_premium)), onClick = onUpgrade)
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            SettingsInfoRow(label = stringResource(R.string.settings_community_db_label), value = stringResource(R.string.settings_community_db_value))
        }
        item {
            SpamSyncRow(isSyncing = isSyncing, onClick = onSyncSpamList)
        }
        item {
            SettingsNavRow(label = highlightBrand(stringResource(R.string.settings_nav_manual)), onClick = onNavigateManual)
        }
        item {
            SettingsNavRow(label = highlightBrand(stringResource(R.string.settings_nav_welcome)), onClick = onNavigateWelcome)
        }
        item {
            SettingsNavRow(label = highlightBrand(stringResource(R.string.settings_nav_about)), onClick = onNavigateAbout)
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
fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 15.sp)
        Text(value, color = TextSecondary, fontSize = 13.sp)
    }
}

@Composable
fun SpamSyncRow(isSyncing: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(14.dp))
            .clickable(enabled = !isSyncing) { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.settings_sync_label), color = Color.White, fontSize = 15.sp)
            Text(
                stringResource(R.string.settings_sync_sources),
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
        if (isSyncing) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = NeonGreen,
                strokeWidth = 2.dp
            )
        } else {
            Text(stringResource(R.string.settings_sync_button), color = NeonGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (checked) NeonGreenDim.copy(alpha = 0.15f) else CardBg, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
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
}

@Composable
fun MonitorLiveRow(checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (checked) NeonGreenDim.copy(alpha = 0.15f) else CardBg, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
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
fun SettingsNavRow(label: AnnotatedString, locked: Boolean = false, onClick: () -> Unit, onUpgrade: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(14.dp))
            .clickable { if (locked) onUpgrade() else onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 15.sp)
        if (locked) PremiumBadge() else Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
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
