package com.degage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.degage.ui.components.InfoDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.database.entities.BlockedCallEntity
import com.degage.ui.components.StatCard
import com.degage.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    isEnabled: Boolean,
    totalBlocked: Int,
    todayCount: Int,
    timeSavedMinutes: Int,
    activeMode: String,
    recentCalls: List<BlockedCallEntity>,
    onToggle: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateHistory: () -> Unit,
) {
    val timeSavedLabel = remember(timeSavedMinutes) {
        val h = timeSavedMinutes / 60
        val m = timeSavedMinutes % 60
        if (h > 0) "${h}h ${m.toString().padStart(2, '0')}" else "${m}min"
    }
    var showInfo by remember { mutableStateOf(false) }
    if (showInfo) InfoDialog(
        title = "Écran d'accueil",
        content = "Tu dégages protège votre téléphone contre les appels de démarchage.\n\n• Le bouton ON/OFF active ou désactive la protection en temps réel.\n• Les compteurs affichent vos statistiques de blocage.\n• La liste en bas montre les derniers appels bloqués.\n• Appuyez sur \"Voir tout\" pour consulter l'historique complet.",
        onDismiss = { showInfo = false }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tu dégages",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "L'IA qui répond à vos spammeurs",
                        fontSize = 12.sp,
                        color = NeonGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row {
                    IconButton(onClick = { showInfo = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Aide", tint = NeonGreen, modifier = Modifier.size(26.dp))
                    }
                    IconButton(onClick = onNavigateSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Paramètres", tint = TextSecondary)
                    }
                }
            }
        }

        item {
            // Carte protection active
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isEnabled) NeonGreenDim.copy(alpha = 0.25f) else CardBgAlt,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isEnabled) "PROTECTION ACTIVE" else "PROTECTION INACTIVE",
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) NeonGreen else TextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (isEnabled) "Votre IA répond à votre place.\nLes spammeurs ne passent plus." else "Activez votre IA anti-spam.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
                Switch(
                    checked = isEnabled,
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

        item {
            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    value = totalBlocked.toString(),
                    label = "Spammeurs\nbloqués",
                    emoji = "🛡️",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = timeSavedLabel,
                    label = "Temps\néconomisé",
                    emoji = "⏰",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = todayCount.toString(),
                    label = "Appels évités\naujourd'hui",
                    emoji = "😊",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            // Carte différenciation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg, RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💡", fontSize = 16.sp)
                    Text("Ce qui nous différencie", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                }
                Text(
                    text = "Les applis classiques bloquent les appels.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Text(
                    text = "La nôtre laisse votre IA répondre — avec le ton que vous choisissez.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 20.sp
                )
            }
        }

        item {
            // Derniers appels bloqués header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Derniers appels bloqués", fontWeight = FontWeight.SemiBold, color = Color.White)
                Text(
                    text = "Voir tout",
                    color = NeonGreen,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onNavigateHistory() }
                )
            }
        }

        if (recentCalls.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBg, RoundedCornerShape(16.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucun appel bloqué pour l'instant", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            items(recentCalls.take(5)) { call ->
                BlockedCallRow(call)
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun BlockedCallRow(call: BlockedCallEntity) {
    val dateStr = remember(call.timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.FRENCH)
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH).format(Date())
        val callDay = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH).format(Date(call.timestamp))
        val prefix = if (today == callDay) "Aujourd'hui" else "Hier"
        "$prefix ${sdf.format(Date(call.timestamp))}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Block, contentDescription = null, tint = RedAlert, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(call.phoneNumber, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 15.sp)
            Text(call.modeName, color = TextSecondary, fontSize = 12.sp)
        }
        Text(dateStr, color = TextSecondary, fontSize = 12.sp)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun HomeScreenPreview() {
    DegageTheme {
        HomeScreen(
            isEnabled = true,
            totalBlocked = 128,
            todayCount = 23,
            timeSavedMinutes = 456,
            activeMode = "Sarcastique",
            recentCalls = emptyList(),
            onToggle = {},
            onNavigateSettings = {},
            onNavigateHistory = {}
        )
    }
}
