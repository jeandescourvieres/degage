package com.degage.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.R
import com.degage.database.entities.BlockedCallEntity
import com.degage.ui.components.InfoDialog
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
    val h = timeSavedMinutes / 60
    val m = timeSavedMinutes % 60
    val timeSavedLabel = if (h > 0) stringResource(R.string.stats_time_saved_hours, h, m) else stringResource(R.string.stats_time_saved_minutes, m)
    var showInfo by remember { mutableStateOf(false) }
    if (showInfo) InfoDialog(
        title = stringResource(R.string.home_info_title),
        content = stringResource(R.string.home_info_content),
        onDismiss = { showInfo = false }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ── HEADER ────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.home_title),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(R.string.home_subtitle),
                        fontSize = 11.sp,
                        color = NeonGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row {
                    IconButton(onClick = { showInfo = true }) {
                        Icon(Icons.Default.Info, contentDescription = stringResource(R.string.cd_help), tint = NeonGreen, modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = onNavigateSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title), tint = NeonGreen, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }

        // ── INTRO HERO ────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(NeonGreen.copy(alpha = 0.12f), NeonGreen.copy(alpha = 0.04f))
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .border(1.dp, NeonGreen.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(R.drawable.robot),
                        contentDescription = null,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        buildAnnotatedString {
                            append("TU ")
                            withStyle(SpanStyle(color = NeonGreen)) {
                                append("DÉGAGES")
                            }
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.home_hero_badge),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonGreen,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        buildAnnotatedString {
                            append(stringResource(R.string.home_hero_line1))
                            withStyle(SpanStyle(color = NeonGreen, fontWeight = FontWeight.Black)) {
                                append(stringResource(R.string.home_hero_line2))
                            }
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IntroBadge(stringResource(R.string.home_badge_others_block))
                        Spacer(modifier = Modifier.height(6.dp))
                        IntroBadge(stringResource(R.string.home_badge_we_reply))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    IntroBadge(stringResource(R.string.home_badge_offline))
                    Spacer(modifier = Modifier.height(6.dp))
                    IntroBadge(stringResource(R.string.home_badge_countries))
                }
            }
        }

        // ── STATUT CENTRAL ────────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isEnabled) NeonGreenDim.copy(alpha = 0.22f) else CardBgAlt,
                        RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = if (isEnabled) 1.5.dp else 1.dp,
                        color = if (isEnabled) NeonGreen.copy(alpha = 0.5f) else TextSecondary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(if (isEnabled) "🛡️" else "😴", fontSize = 52.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isEnabled) stringResource(R.string.home_status_active) else stringResource(R.string.home_status_inactive),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isEnabled) NeonGreen else TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isEnabled)
                        stringResource(R.string.home_status_active_desc)
                    else
                        stringResource(R.string.home_status_inactive_desc),
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
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
                if (isEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .background(NeonGreen.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🎭", fontSize = 13.sp)
                        Text(
                            stringResource(R.string.home_mode_label, activeMode),
                            color = NeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ── STATS ─────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BigStatCard(
                    value = totalBlocked.toString(),
                    label = stringResource(R.string.home_stat_blocked),
                    emoji = "🛡️",
                    modifier = Modifier.weight(1f)
                )
                BigStatCard(
                    value = timeSavedLabel,
                    label = stringResource(R.string.home_stat_time_saved),
                    emoji = "⏰",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── DERNIERS APPELS ───────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.home_recent_calls_title), fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp)
                Text(
                    text = stringResource(R.string.home_recent_calls_see_all),
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
                    Text(stringResource(R.string.home_recent_calls_empty),
                        color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
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
private fun IntroBadge(label: String) {
    Text(
        label,
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(CardBgAlt, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun BigStatCard(value: String, label: String, emoji: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(CardBg, RoundedCornerShape(18.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(value, fontSize = 32.sp, fontWeight = FontWeight.Black, color = NeonGreen)
        Text(label, fontSize = 11.sp, color = TextSecondary, textAlign = TextAlign.Center, lineHeight = 16.sp)
    }
}

@Composable
fun BlockedCallRow(call: BlockedCallEntity) {
    val todayPrefix = stringResource(R.string.home_call_today_prefix)
    val yesterdayPrefix = stringResource(R.string.home_call_yesterday_prefix)
    val dateStr = remember(call.timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.FRENCH)
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH).format(Date())
        val callDay = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH).format(Date(call.timestamp))
        val prefix = if (today == callDay) todayPrefix else yesterdayPrefix
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
