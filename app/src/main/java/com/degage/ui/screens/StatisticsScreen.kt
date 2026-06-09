package com.degage.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.degage.ui.components.InfoDialog
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.ui.theme.*

@Composable
fun StatisticsScreen(
    totalBlocked: Int,
    todayCount: Int,
    timeSavedMinutes: Int,
    spamDbCount: Int = 0,
    lastSpamSync: Long = 0L,
    onBack: () -> Unit = {},
) {
    var showInfo by remember { mutableStateOf(false) }
    if (showInfo) InfoDialog(
        title = "Statistiques",
        content = "Votre bilan anti-spam :\n\n• Le graphique circulaire représente le temps économisé sur 24h.\n• Spammeurs bloqués : nombre total d'appels interceptés depuis l'installation.\n• Appels évités/jour : moyenne quotidienne sur les 7 derniers jours.\n• Durée moyenne évitée : temps moyen d'un appel de démarchage.",
        onDismiss = { showInfo = false }
    )

    val timeSavedLabel = run {
        val h = timeSavedMinutes / 60
        val m = timeSavedMinutes % 60
        if (h > 0) "${h}h ${m.toString().padStart(2, '0')}" else "${m}min"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
            }
            Text("Statistiques", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
            Text("7 derniers jours", color = TextSecondary, fontSize = 13.sp)
            IconButton(onClick = { showInfo = true }) {
                Icon(Icons.Default.Info, contentDescription = "Aide", tint = NeonGreen, modifier = Modifier.size(26.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Graphique circulaire
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
            val sweepAngle = (timeSavedMinutes.toFloat() / (24 * 60).toFloat()).coerceIn(0f, 1f) * 360f
            Canvas(modifier = Modifier.size(220.dp)) {
                // Track gris
                drawArc(
                    color = CardBgAlt,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round),
                    topLeft = Offset(12.dp.toPx(), 12.dp.toPx()),
                    size = Size(size.width - 24.dp.toPx(), size.height - 24.dp.toPx())
                )
                // Arc vert néon
                drawArc(
                    color = NeonGreen,
                    startAngle = -90f,
                    sweepAngle = sweepAngle.coerceAtLeast(10f),
                    useCenter = false,
                    style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round),
                    topLeft = Offset(12.dp.toPx(), 12.dp.toPx()),
                    size = Size(size.width - 24.dp.toPx(), size.height - 24.dp.toPx())
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(timeSavedLabel, fontSize = 36.sp, fontWeight = FontWeight.Black, color = NeonGreen)
                Text("Temps économisé", fontSize = 13.sp, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Stat rows
        StatRow(emoji = "🚫", value = totalBlocked.toString(), label = "Spammeurs bloqués")
        Spacer(modifier = Modifier.height(12.dp))
        StatRow(emoji = "😊", value = todayCount.toString(), label = "Appels évités / jour (moy.)")
        Spacer(modifier = Modifier.height(12.dp))
        StatRow(emoji = "👤", value = "4,8 min", label = "Durée moyenne évitée")
        Spacer(modifier = Modifier.height(12.dp))
        StatRow(emoji = "🛡️", value = spamDbCount.toString(), label = "Numéros mémorisés (rejet auto)")
        Spacer(modifier = Modifier.height(12.dp))
        SpamDbInfoRow(lastSpamSync = lastSpamSync)
    }
}

@Composable
fun StatRow(emoji: String, value: String, label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = NeonGreen, modifier = Modifier.width(80.dp))
        Text(label, fontSize = 14.sp, color = TextSecondary)
    }
}

@Composable
private fun SpamDbInfoRow(lastSpamSync: Long) {
    val syncLabel = if (lastSpamSync == 0L) "Jamais synchronisée" else {
        val diff = System.currentTimeMillis() - lastSpamSync
        when {
            diff < 60_000 -> "À l'instant"
            diff < 3_600_000 -> "Il y a ${diff / 60_000} min"
            diff < 86_400_000 -> "Il y a ${diff / 3_600_000} h"
            else -> "Il y a ${diff / 86_400_000} j"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NeonGreenDim.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
            .border(1.dp, NeonGreen.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Sources intégrées", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeonGreen)

        SpamSourceRow("📋", "Plages ARCEP documentées", "52 préfixes FR actifs en permanence", alwaysActive = true)
        SpamSourceRow("🌍", "phoneblock.net", "Base communautaire européenne open source")
        SpamSourceRow("🇫🇷", "Signal-Spam France", "Association certifiée anti-spam française")
        SpamSourceRow("👤", "Vos signalements", "Chaque numéro bloqué par vous est mémorisé")

        HorizontalDivider(color = NeonGreen.copy(alpha = 0.15f), thickness = 1.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dernière synchronisation", fontSize = 12.sp, color = TextSecondary)
            Text(syncLabel, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Text(
            "Mise à jour disponible dans Paramètres → Mettre à jour la base spam",
            fontSize = 11.sp,
            color = TextSecondary.copy(alpha = 0.7f),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun SpamSourceRow(emoji: String, name: String, desc: String, alwaysActive: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(emoji, fontSize = 16.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(desc, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
        }
        if (alwaysActive) {
            Text("ACTIF", fontSize = 9.sp, color = NeonGreen, fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun StatisticsPreview() {
    DegageTheme { StatisticsScreen(totalBlocked = 128, todayCount = 23, timeSavedMinutes = 456, spamDbCount = 47) }
}
