package com.degage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
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
import com.degage.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class HistoryFilter(val label: String) { TOUS("Tous"), BLOQUES("Bloqués"), REPONSES("Réponses"), MANUELS("Manuels") }

@Composable
fun HistoryScreen(
    calls: List<BlockedCallEntity>,
    onDelete: (Long) -> Unit,
    onMarkNotSpam: (BlockedCallEntity) -> Unit = {},
    onBack: () -> Unit = {},
) {
    var filter by remember { mutableStateOf(HistoryFilter.TOUS) }
    var showInfo by remember { mutableStateOf(false) }
    if (showInfo) InfoDialog(
        title = "Historique",
        content = "Journal de tous les appels bloqués par Tu dégages.\n\n• Chaque ligne affiche le numéro détecté, la date/heure, le mode utilisé et le message joué.\n• Utilisez les filtres en haut (Tous / Bloqués / Réponses / Manuels) pour trier l'affichage.\n• Appuyez sur l'icône ✅ si ce numéro a été bloqué à tort : il ne sera plus jamais bloqué.\n• Appuyez sur l'icône 🗑️ pour supprimer une entrée de l'historique.",
        onDismiss = { showInfo = false }
    )

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
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
            }
            Text("Historique", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
            IconButton(onClick = { showInfo = true }) {
                Icon(Icons.Default.Info, contentDescription = "Aide", tint = NeonGreen, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HistoryFilter.entries.forEach { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = { filter = f },
                    label = { Text(f.label, fontSize = 13.sp) },
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

        if (calls.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucun appel bloqué", color = TextSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(calls, key = { it.id }) { call ->
                    HistoryRow(call = call, onDelete = { onDelete(call.id) }, onMarkNotSpam = { onMarkNotSpam(call) })
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
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
                        Icon(Icons.Default.CheckCircle, contentDescription = "Pas un spam", tint = NeonGreen, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = TextSecondary, modifier = Modifier.size(16.dp))
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
