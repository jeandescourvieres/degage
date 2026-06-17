package com.degage.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.degage.ui.components.InfoDialog
import com.degage.ui.components.highlightBrand
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.R
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
        title = stringResource(R.string.stats_info_title),
        content = stringResource(R.string.stats_info_content),
        onDismiss = { showInfo = false }
    )

    val timeSavedLabel = run {
        val h = timeSavedMinutes / 60
        val m = timeSavedMinutes % 60
        if (h > 0) stringResource(R.string.stats_time_saved_hours, h, m) else stringResource(R.string.stats_time_saved_minutes, m)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = Color.White)
            }
            Text(stringResource(R.string.stats_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
            Text(stringResource(R.string.stats_period), color = TextSecondary, fontSize = 13.sp)
            IconButton(onClick = { showInfo = true }) {
                Icon(Icons.Default.Info, contentDescription = stringResource(R.string.cd_help), tint = NeonGreen, modifier = Modifier.size(26.dp))
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
                Text(stringResource(R.string.stats_time_saved), fontSize = 13.sp, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Stat rows
        StatRow(emoji = "🚫", value = totalBlocked.toString(), label = stringResource(R.string.stats_blocked_label))
        Spacer(modifier = Modifier.height(12.dp))
        StatRow(emoji = "😊", value = todayCount.toString(), label = stringResource(R.string.stats_avg_avoided_label))
        Spacer(modifier = Modifier.height(12.dp))
        StatRow(emoji = "👤", value = stringResource(R.string.stats_avg_duration_value), label = stringResource(R.string.stats_avg_duration_label))
        Spacer(modifier = Modifier.height(12.dp))
        StatRow(emoji = "🛡️", value = spamDbCount.toString(), label = stringResource(R.string.stats_memorized_label))
        Spacer(modifier = Modifier.height(12.dp))
        SpamDbInfoRow(lastSpamSync = lastSpamSync)
        Spacer(modifier = Modifier.height(12.dp))
        LegalNoteCard()
        Spacer(modifier = Modifier.height(80.dp))
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
    val syncLabel = if (lastSpamSync == 0L) stringResource(R.string.stats_sync_never) else {
        val diff = System.currentTimeMillis() - lastSpamSync
        when {
            diff < 60_000 -> stringResource(R.string.stats_sync_now)
            diff < 3_600_000 -> stringResource(R.string.stats_sync_minutes, diff / 60_000)
            diff < 86_400_000 -> stringResource(R.string.stats_sync_hours, diff / 3_600_000)
            else -> stringResource(R.string.stats_sync_days, diff / 86_400_000)
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
        Text(stringResource(R.string.stats_sources_title), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeonGreen)

        SpamSourceRow("📋", stringResource(R.string.stats_source_arcep_name), stringResource(R.string.stats_source_arcep_desc), alwaysActive = true)
        SpamSourceRow("🌍", stringResource(R.string.stats_source_community_name), stringResource(R.string.stats_source_community_desc), alwaysActive = true)
        SpamSourceRow("👤", stringResource(R.string.stats_source_user_name), stringResource(R.string.stats_source_user_desc))

        HorizontalDivider(color = NeonGreen.copy(alpha = 0.15f), thickness = 1.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.stats_last_sync_label), fontSize = 12.sp, color = TextSecondary)
            Text(syncLabel, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Text(
            stringResource(R.string.stats_sync_update_hint),
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
            Text(stringResource(R.string.stats_source_active), fontSize = 9.sp, color = NeonGreen, fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp))
        }
    }
}

@Composable
private fun LegalNoteCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBgAlt, RoundedCornerShape(14.dp))
            .border(1.dp, NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("⚖️", fontSize = 16.sp)
            Text(stringResource(R.string.stats_legal_title), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Text(
            stringResource(R.string.stats_legal_p1),
            fontSize = 12.sp, color = TextSecondary, lineHeight = 18.sp
        )
        Text(
            highlightBrand(stringResource(R.string.stats_legal_p2)),
            fontSize = 12.sp, color = TextSecondary, lineHeight = 18.sp
        )
        Row(
            modifier = Modifier
                .background(NeonGreen.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("💡", fontSize = 12.sp)
            Text(
                stringResource(R.string.stats_legal_tip),
                fontSize = 11.sp, color = NeonGreen, lineHeight = 16.sp
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun StatisticsPreview() {
    DegageTheme { StatisticsScreen(totalBlocked = 128, todayCount = 23, timeSavedMinutes = 456, spamDbCount = 47) }
}
