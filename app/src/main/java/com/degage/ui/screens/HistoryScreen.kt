package com.degage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.degage.ui.components.InfoDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.R
import com.degage.callscreen.RecentCallEntry
import com.degage.database.entities.BlockedCallEntity
import com.degage.history.exportCallsToCsv
import com.degage.ui.components.PremiumBadge
import com.degage.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class HistoryFilter(val label: String) { TOUS("Tous"), BLOQUES("Bloqués"), REPONSES("Réponses"), MANUELS("Manuels") }

@Composable
fun HistoryFilter.localizedLabel(): String = when (this) {
    HistoryFilter.TOUS -> stringResource(R.string.history_filter_all)
    HistoryFilter.BLOQUES -> stringResource(R.string.history_filter_blocked)
    HistoryFilter.REPONSES -> stringResource(R.string.history_filter_replies)
    HistoryFilter.MANUELS -> stringResource(R.string.history_filter_manual)
}

@Composable
fun HistoryScreen(
    calls: List<BlockedCallEntity>,
    onDelete: (Long) -> Unit,
    onMarkNotSpam: (BlockedCallEntity) -> Unit = {},
    recentUnblockedCalls: List<RecentCallEntry> = emptyList(),
    onLoadRecentUnblocked: () -> Unit = {},
    onBlockRecentCall: (RecentCallEntry) -> Unit = {},
    onNavigateCustomBlocks: () -> Unit = {},
    isPremium: Boolean = true,
    onUpgrade: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    var filter by remember { mutableStateOf(HistoryFilter.TOUS) }
    var showInfo by remember { mutableStateOf(false) }
    if (showInfo) InfoDialog(
        title = stringResource(R.string.history_info_title),
        content = stringResource(R.string.history_info_content),
        onDismiss = { showInfo = false }
    )

    LaunchedEffect(Unit) { onLoadRecentUnblocked() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = Color.White)
            }
            Text(stringResource(R.string.history_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
            IconButton(
                onClick = { if (!isPremium) onUpgrade() else exportCallsToCsv(context, calls) },
                enabled = calls.isNotEmpty()
            ) {
                if (!isPremium) {
                    PremiumBadge()
                } else {
                    Icon(Icons.Default.FileDownload, contentDescription = stringResource(R.string.history_export_csv), tint = if (calls.isNotEmpty()) NeonGreen else TextSecondary, modifier = Modifier.size(26.dp))
                }
            }
            IconButton(onClick = { showInfo = true }) {
                Icon(Icons.Default.Info, contentDescription = stringResource(R.string.cd_help), tint = NeonGreen, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HistoryFilter.entries.forEach { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = { filter = f },
                    label = { Text(f.localizedLabel(), fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonGreen,
                        selectedLabelColor = Color.Black,
                        containerColor = CardBg,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filter == HistoryFilter.TOUS) {
            CustomBlocksLinkCard(locked = !isPremium, onClick = onNavigateCustomBlocks, onUpgrade = onUpgrade)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (filter == HistoryFilter.TOUS && recentUnblockedCalls.isNotEmpty()) {
            Text(
                stringResource(R.string.history_recent_title),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeonGreen
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recentUnblockedCalls.forEach { entry ->
                    RecentCallRow(entry = entry, onBlock = { onBlockRecentCall(entry) })
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        val filteredCalls = when (filter) {
            HistoryFilter.TOUS -> calls
            HistoryFilter.BLOQUES -> calls.filter { it.modeName == "Auto" }
            HistoryFilter.REPONSES -> calls.filter { it.modeName != "Auto" }
            HistoryFilter.MANUELS -> calls.filter { it.replyUsed.contains("règle personnalisée") }
        }

        if (filteredCalls.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.history_empty), color = TextSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredCalls, key = { it.id }) { call ->
                    HistoryRow(call = call, onDelete = { onDelete(call.id) }, onMarkNotSpam = { onMarkNotSpam(call) })
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun CustomBlocksLinkCard(locked: Boolean, onClick: () -> Unit, onUpgrade: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(14.dp))
            .clickable { if (locked) onUpgrade() else onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.history_custom_blocks_title), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.history_custom_blocks_desc), color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (locked) PremiumBadge() else Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
    }
}

@Composable
fun RecentCallRow(entry: RecentCallEntry, onBlock: () -> Unit) {
    val dateStr = remember(entry.timestamp) {
        val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.FRENCH)
        sdf.format(Date(entry.timestamp))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.PhoneMissed, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.number, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 15.sp)
            Text(dateStr, color = TextSecondary, fontSize = 12.sp)
        }
        Button(
            onClick = onBlock,
            colors = ButtonDefaults.buttonColors(containerColor = RedAlert.copy(alpha = 0.15f), contentColor = RedAlert),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(stringResource(R.string.history_recent_block_button), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun HistoryRow(call: BlockedCallEntity, onDelete: () -> Unit, onMarkNotSpam: () -> Unit = {}) {
    val dateStr = remember(call.timestamp) {
        val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.FRENCH)
        sdf.format(Date(call.timestamp))
    }
    val isRealNumber = call.phoneNumber.any { it.isDigit() }

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
            Text(call.modeName, color = NeonGreen, fontSize = 12.sp)
            if (call.replyUsed.isNotBlank()) {
                Text("« ${call.replyUsed} »", color = TextSecondary, fontSize = 11.sp, maxLines = 1)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(dateStr, color = TextSecondary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                if (isRealNumber) {
                    IconButton(onClick = onMarkNotSpam, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.history_not_spam), tint = NeonGreen, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete), tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun HistoryPreview() {
    DegageTheme { HistoryScreen(calls = emptyList(), onDelete = {}) }
}
