package com.degage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.database.entities.ReplyEntity
import com.degage.modes.AppMode
import com.degage.ui.theme.*

@Composable
fun RepliesScreen(
    mode: AppMode,
    replies: List<ReplyEntity>,
    onBack: () -> Unit,
    onToggle: (ReplyEntity) -> Unit,
    onAdd: (String) -> Unit,
    onDelete: (ReplyEntity) -> Unit,
) {
    var newText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
            }
            Column {
                Text("${mode.emoji} ${mode.label}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Phrases de réponse", fontSize = 13.sp, color = TextSecondary)
            }
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(replies, key = { it.id }) { reply ->
                ReplyRow(reply = reply, onToggle = { onToggle(reply) }, onDelete = { onDelete(reply) })
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Ajouter une phrase custom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newText,
                        onValueChange = { newText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ajouter une phrase personnalisée…", color = TextSecondary, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = CardBgAlt,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = NeonGreen
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (newText.isNotBlank()) { onAdd(newText.trim()); newText = "" }
                        })
                    )
                    IconButton(
                        onClick = {
                            if (newText.isNotBlank()) { onAdd(newText.trim()); newText = "" }
                        },
                        modifier = Modifier
                            .background(NeonGreen, RoundedCornerShape(12.dp))
                            .size(52.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = Color.Black)
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun ReplyRow(reply: ReplyEntity, onToggle: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = reply.text,
            modifier = Modifier.weight(1f),
            color = if (reply.isEnabled) Color.White else TextSecondary,
            fontSize = 14.sp
        )
        if (reply.isCustom) {
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
        }
        Switch(
            checked = reply.isEnabled,
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

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun RepliesPreview() {
    DegageTheme {
        RepliesScreen(
            mode = AppMode.SARCASTIQUE,
            replies = listOf(
                ReplyEntity(id = 1, text = "Cette ligne est allergique au démarchage.", modeName = "SARCASTIQUE", isEnabled = true),
                ReplyEntity(id = 2, text = "Cette ligne pratique activement le rejet.", modeName = "SARCASTIQUE", isEnabled = false, isCustom = true),
            ),
            onBack = {}, onToggle = {}, onAdd = {}, onDelete = {}
        )
    }
}
