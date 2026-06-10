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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.degage.ui.components.InfoDialog
import com.degage.ui.components.PremiumBadge
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        title = "Paramètres",
        content = "Configurez le comportement de DÉGAGE :\n\n• Protection : active/désactive le filtrage global des appels.\n• Décroche automatique : répond à l'appel sans que le téléphone sonne (recommandé).\n• Bloquer après réponse : ajoute le numéro à la liste noire après chaque interaction.\n• Notifications : vous avertit à chaque appel bloqué.\n• Personnaliser les réponses : créez et gérez vos propres messages.\n• Paramètres vocaux : changez la voix, la vitesse et la hauteur.",
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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                }
                Text("Paramètres", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                IconButton(onClick = { showInfo = true }) {
                    Icon(Icons.Default.Info, contentDescription = "Aide", tint = NeonGreen, modifier = Modifier.size(26.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            CountrySelectorRow(country = country, isPremium = isPremium, onSetCountry = onSetCountry, onUpgrade = onUpgrade)
        }
        item {
            ReplyLanguageSelectorRow(language = replyLanguage, isPremium = isPremium, onSetLanguage = onSetReplyLanguage, onUpgrade = onUpgrade)
        }
        item {
            SettingsToggleRow(label = "Protection", checked = isEnabled, onToggle = onToggleEnabled)
        }
        item {
            SettingsToggleRow(label = "Décroche automatique", checked = autoReject, onToggle = onToggleAutoReject)
        }
        item {
            SettingsToggleRow(label = "Bloquer le numéro après réponse", checked = blockAfterReply, onToggle = onToggleBlockAfterReply)
        }
        item {
            SettingsToggleRow(label = "Notifications", checked = notifications, onToggle = onToggleNotifications)
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
            SettingsNavRow(label = "💬 Personnaliser les réponses", locked = !isPremium, onClick = onNavigateMessageBuilder, onUpgrade = onUpgrade)
        }
        item {
            SettingsNavRow(label = "🎙️ Paramètres vocaux", locked = !isPremium, onClick = onNavigateVoiceSettings, onUpgrade = onUpgrade)
        }
        item {
            SettingsNavRow(label = "🚫 Numéros bloqués manuellement", locked = !isPremium, onClick = onNavigateCustomBlocks, onUpgrade = onUpgrade)
        }
        item {
            SettingsNavRow(label = "⭐ Tu dégages Premium", onClick = onUpgrade)
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            SettingsInfoRow(label = "Base communautaire", value = "Mise à jour : aujourd'hui 08:15")
        }
        item {
            SettingsInfoRow(label = "Langue", value = "Français")
        }
        item {
            SpamSyncRow(isSyncing = isSyncing, onClick = onSyncSpamList)
        }
        item {
            SettingsNavRow(label = "📖 Mode d'emploi", onClick = onNavigateManual)
        }
        item {
            SettingsNavRow(label = "👋 Revoir la présentation", onClick = onNavigateWelcome)
        }
        item {
            SettingsNavRow(label = "À propos", onClick = onNavigateAbout)
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
            Text("🛡️ Mettre à jour la base spam", color = Color.White, fontSize = 15.sp)
            Text(
                "phoneblock.net • Signal-Spam France • ARCEP",
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
            Text("Sync", color = NeonGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
        Text("Pays", color = Color.White, fontSize = 15.sp)
        Text(
            "Adapte la base de numéros indésirables détectés à votre pays.",
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CountryChip("🇫🇷 France", selected = country == "FR", onClick = { onSetCountry("FR") })
            val chSelected = country == "CH"
            val chLocked = !isPremium
            CountryChip(
                "🇨🇭 Suisse",
                selected = chSelected && !chLocked,
                onClick = { if (chLocked) onUpgrade() else onSetCountry("CH") }
            )
            if (chLocked) PremiumBadge()
        }
    }
}

@Composable
fun ReplyLanguageSelectorRow(language: String, isPremium: Boolean = true, onSetLanguage: (String) -> Unit, onUpgrade: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text("Langue des messages vocaux", color = Color.White, fontSize = 15.sp)
        Text(
            "Langue dans laquelle Tu dégages répond aux démarcheurs (utile en Suisse alémanique ou italophone).",
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CountryChip("🇫🇷 Français", selected = language == "FR", onClick = { onSetLanguage("FR") })
            val locked = !isPremium
            CountryChip(
                "🇩🇪 Deutsch",
                selected = language == "DE" && !locked,
                onClick = { if (locked) onUpgrade() else onSetLanguage("DE") }
            )
            CountryChip(
                "🇮🇹 Italiano",
                selected = language == "IT" && !locked,
                onClick = { if (locked) onUpgrade() else onSetLanguage("IT") }
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
            Text("📵 Bloquer les appels masqués", color = Color.White, fontSize = 15.sp)
            Text(
                "Rejette automatiquement, sans message vocal, les appels sans numéro affiché (masqué, privé, inconnu).",
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
            Text("🔊 Écouter la réponse IA en direct", color = Color.White, fontSize = 15.sp)
            Text(
                "Entendez ce que votre IA dit au spammeur sur votre haut-parleur",
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
                    "Base communautaire",
                    color = NeonGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    buildAnnotatedString {
                        append("La clé de voûte de\n")
                        withStyle(SpanStyle(color = NeonGreen)) { append("Tu dégages") }
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
            "Chaque spammeur bloqué par un utilisateur est signalé à toute la communauté. " +
            "À chaque connexion, votre appli télécharge automatiquement les nouveaux numéros signalés par tous les utilisateurs — " +
            "et les ajoute à votre base locale. Plus on est nombreux, plus la base devient redoutable contre les spammeurs.",
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ContributeBadge("🤝 Partagé entre tous")
            ContributeBadge("🔒 Opt-in")
            ContributeBadge("🇫🇷 Serveur EU")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Seul le numéro de l'appelant spam est transmis — jamais vos données personnelles.",
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
fun SettingsNavRow(label: String, locked: Boolean = false, onClick: () -> Unit, onUpgrade: () -> Unit = {}) {
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
