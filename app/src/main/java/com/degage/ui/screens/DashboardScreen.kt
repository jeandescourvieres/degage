package com.degage.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.R
import com.degage.database.entities.BlockedCallEntity
import com.degage.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    isEnabled: Boolean,
    totalBlocked: Int,
    timeSavedLabel: String,
    activeMode: String,
    recentCalls: List<BlockedCallEntity>,
    onToggle: () -> Unit,
    onNavigateHistory: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = Color.White)
            }
            Text(
                text = stringResource(R.string.dashboard_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(AccentCyan, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                DashboardCard(
                    isEnabled = isEnabled,
                    totalBlocked = totalBlocked,
                    timeSavedLabel = timeSavedLabel,
                    activeMode = activeMode,
                    recentCalls = recentCalls,
                    onToggle = onToggle,
                    onNavigateHistory = onNavigateHistory
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun DashboardCard(
    isEnabled: Boolean,
    totalBlocked: Int,
    timeSavedLabel: String,
    activeMode: String,
    recentCalls: List<BlockedCallEntity>,
    onToggle: () -> Unit,
    onNavigateHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(24.dp))
            .border(1.dp, NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.home_dashboard_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.home_dashboard_intro),
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )
        }

        // Statut central
        ProtectionStatusCard(
            isEnabled = isEnabled,
            activeMode = activeMode,
            onToggle = onToggle
        )

        // Stats
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

        // Derniers appels
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

            if (recentCalls.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBgAlt, RoundedCornerShape(16.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.home_recent_calls_empty),
                        color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
                }
            } else {
                recentCalls.take(5).forEach { call ->
                    BlockedCallRow(call)
                }
            }
        }
    }
}

@Composable
fun ProtectionStatusCard(
    isEnabled: Boolean,
    activeMode: String,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isEnabled) NeonGreenDim.copy(alpha = 0.22f) else CardBgAlt,
                RoundedCornerShape(24.dp)
            )
            .border(
                width = if (isEnabled) 1.5.dp else 1.dp,
                color = if (isEnabled) NeonGreen.copy(alpha = 0.5f) else TextSecondary.copy(alpha = 0.4f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val statusPulse by rememberInfiniteTransition(label = "statusPulse").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(900),
                repeatMode = RepeatMode.Reverse
            ),
            label = "statusPulse"
        )
        Text(
            text = if (isEnabled) "🛡️" else "😴",
            fontSize = 40.sp,
            modifier = if (isEnabled) Modifier.alpha(0.4f + 0.6f * statusPulse) else Modifier
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isEnabled) stringResource(R.string.home_status_active) else stringResource(R.string.home_status_inactive),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = if (isEnabled) NeonGreen.copy(alpha = 0.5f + 0.5f * statusPulse) else TextSecondary,
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
        Spacer(modifier = Modifier.height(10.dp))
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
            Spacer(modifier = Modifier.height(8.dp))
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

@Composable
private fun BigStatCard(value: String, label: String, emoji: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(CardBgAlt, RoundedCornerShape(18.dp))
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
            .background(CardBgAlt, RoundedCornerShape(14.dp))
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
