package com.degage.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.database.entities.ReplyEntity
import com.degage.modes.AppMode
import com.degage.replies.MessagePart
import com.degage.ui.theme.*

@Composable
fun MessageBuilderScreen(
    activeMode: AppMode,
    salutations: List<ReplyEntity>,
    bodies: List<ReplyEntity>,
    endings: List<ReplyEntity>,
    replyLanguage: String = "FR",
    onBack: () -> Unit,
    onSelect: (ReplyEntity) -> Unit,
    onAdd: (String, MessagePart) -> Unit,
    onDelete: (ReplyEntity) -> Unit,
    @Suppress("UNUSED_PARAMETER") onToggle: (ReplyEntity) -> Unit = {},
) {
    val previewText = remember(salutations, bodies, endings) {
        val s = salutations.firstOrNull { it.isEnabled }?.text ?: ""
        val b = bodies.firstOrNull { it.isEnabled }?.text ?: "…"
        val e = endings.firstOrNull { it.isEnabled }?.text ?: ""
        listOf(s, b, e).filter { it.isNotBlank() }.joinToString(" ")
    }

    var expandedSection by remember { mutableStateOf<MessagePart?>(MessagePart.SALUTATION) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // ── Header ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
            }
            Text(
                "Personnaliser les réponses",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // ── Mode actif — badge proéminent ────────────────────────────────
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .border(1.dp, NeonGreen.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(activeMode.emoji, fontSize = 28.sp)
            Column {
                Text("Mode actif", fontSize = 11.sp, color = NeonGreen, fontWeight = FontWeight.SemiBold)
                Text(
                    activeMode.label,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "Corps adapté\nà ce mode →",
                fontSize = 10.sp,
                color = TextSecondary,
                textAlign = TextAlign.End,
                lineHeight = 14.sp
            )
        }

        if (replyLanguage != "FR") {
            val langLabel = when (replyLanguage) {
                "DE" -> "allemand"
                "IT" -> "italien"
                else -> replyLanguage
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .background(NeonGreenDim.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("🌐", fontSize = 18.sp)
                Text(
                    "Les phrases ci-dessous sont en $langLabel, la langue choisie pour vos messages vocaux. Sélectionnez celles qui vous correspondent.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Intro / mode d'emploi ────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBg, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Comment composer votre message ?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Tu dégages assemble automatiquement un message en 3 parties avant de raccrocher. Vous choisissez librement une option par section :",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                    MessagePartExplainRow("👋", "Salutation", "La phrase d'ouverture. Commune à tous les modes.")
                    MessagePartExplainRow("💬", "Corps", "La réponse principale. Varie selon le mode actif (${activeMode.label}).")
                    MessagePartExplainRow("🔚", "Formule de fin", "La phrase de clôture. Commune à tous les modes.")
                    Text(
                        "Vous pouvez aussi créer vos propres formules en bas de chaque section.",
                        fontSize = 12.sp,
                        color = NeonGreen.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )
                }
            }

            // ── Aperçu du message final ──────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonGreenDim.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text("Aperçu du message assemblé", fontSize = 12.sp, color = NeonGreen, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"$previewText\"",
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            // ── Salutations ──────────────────────────────────────────────
            item {
                PartSection(
                    part = MessagePart.SALUTATION,
                    description = "La phrase d'ouverture lue en premier par Tu dégages. Sélectionnez celle qui correspond à votre style — une seule à la fois.",
                    isExpanded = expandedSection == MessagePart.SALUTATION,
                    onToggleExpand = {
                        expandedSection = if (expandedSection == MessagePart.SALUTATION) null else MessagePart.SALUTATION
                    }
                )
            }
            if (expandedSection == MessagePart.SALUTATION) {
                items(salutations, key = { "s_${it.id}" }) { item ->
                    PartItemRow(item = item, onSelect = { onSelect(item) }, onDelete = { onDelete(item) })
                }
                item {
                    AddPartField(part = MessagePart.SALUTATION, onAdd = { onAdd(it, MessagePart.SALUTATION) })
                }
            }

            // ── Corps du message ─────────────────────────────────────────
            item {
                PartSection(
                    part = MessagePart.BODY,
                    description = "Le cœur de la réponse, adapté au mode ${activeMode.emoji} ${activeMode.label}. C'est la phrase principale entendue par le démarcheur. Une seule à la fois.",
                    isExpanded = expandedSection == MessagePart.BODY,
                    onToggleExpand = {
                        expandedSection = if (expandedSection == MessagePart.BODY) null else MessagePart.BODY
                    }
                )
            }
            if (expandedSection == MessagePart.BODY) {
                items(bodies, key = { "b_${it.id}" }) { item ->
                    PartItemRow(item = item, onSelect = { onSelect(item) }, onDelete = { onDelete(item) })
                }
                item {
                    AddPartField(part = MessagePart.BODY, onAdd = { onAdd(it, MessagePart.BODY) })
                }
            }

            // ── Formules de fin ──────────────────────────────────────────
            item {
                PartSection(
                    part = MessagePart.ENDING,
                    description = "La formule de clôture prononcée juste avant de raccrocher. Une seule à la fois.",
                    isExpanded = expandedSection == MessagePart.ENDING,
                    onToggleExpand = {
                        expandedSection = if (expandedSection == MessagePart.ENDING) null else MessagePart.ENDING
                    }
                )
            }
            if (expandedSection == MessagePart.ENDING) {
                items(endings, key = { "e_${it.id}" }) { item ->
                    PartItemRow(item = item, onSelect = { onSelect(item) }, onDelete = { onDelete(item) })
                }
                item {
                    AddPartField(part = MessagePart.ENDING, onAdd = { onAdd(it, MessagePart.ENDING) })
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun MessagePartExplainRow(emoji: String, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBg, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(emoji, fontSize = 20.sp)
        Column {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(desc, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun PartSection(
    part: MessagePart,
    description: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(14.dp))
            .clickable { onToggleExpand() }
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(part.emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(part.label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = NeonGreen
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun PartItemRow(item: ReplyEntity, onSelect: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBgAlt, RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = item.isEnabled,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = NeonGreen,
                unselectedColor = TextSecondary
            )
        )
        Text(
            text = item.text,
            modifier = Modifier.weight(1f),
            color = if (item.isEnabled) Color.White else TextSecondary,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
        if (item.isCustom) {
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = TextSecondary, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun AddPartField(part: MessagePart, onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ajouter une ${part.label.lowercase()}…", color = TextSecondary, fontSize = 12.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = CardBgAlt,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = NeonGreen
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (text.isNotBlank()) { onAdd(text.trim()); text = "" }
            })
        )
        IconButton(
            onClick = { if (text.isNotBlank()) { onAdd(text.trim()); text = "" } },
            modifier = Modifier.background(NeonGreen, RoundedCornerShape(12.dp)).size(52.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = Color.Black)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun MessageBuilderPreview() {
    DegageTheme {
        MessageBuilderScreen(
            activeMode = AppMode.SARCASTIQUE,
            salutations = listOf(
                ReplyEntity(id = 1, text = "Bonjour.", modeName = "GLOBAL", partType = "SALUTATION", isEnabled = true),
                ReplyEntity(id = 2, text = "Bonjour et bienvenue sur Tu dégages.", modeName = "GLOBAL", partType = "SALUTATION", isEnabled = false),
            ),
            bodies = listOf(
                ReplyEntity(id = 3, text = "Cette ligne est allergique au démarchage.", modeName = "SARCASTIQUE", partType = "BODY", isEnabled = true),
                ReplyEntity(id = 4, text = "Vous avez atteint la boîte vocale la plus sarcastique de France.", modeName = "SARCASTIQUE", partType = "BODY", isEnabled = false),
            ),
            endings = listOf(
                ReplyEntity(id = 4, text = "À pas bientôt.", modeName = "GLOBAL", partType = "ENDING", isEnabled = true),
                ReplyEntity(id = 5, text = "Cordialement… enfin presque.", modeName = "GLOBAL", partType = "ENDING", isEnabled = false, isCustom = true),
            ),
            onBack = {}, onSelect = {}, onAdd = { _, _ -> }, onDelete = {}
        )
    }
}
