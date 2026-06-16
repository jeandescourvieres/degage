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
import com.degage.ui.components.highlightBrand
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.R
import com.degage.database.entities.ReplyEntity
import com.degage.modes.AppMode
import com.degage.modes.localizedLabel
import com.degage.replies.MessagePart
import com.degage.replies.localizedLabel
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
            modifier = Modifier
                .fillMaxWidth()
                .background(AccentCyan)
                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = Color.Black)
            }
            Text(
                stringResource(R.string.mb_header_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
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
                Text(stringResource(R.string.mb_active_mode_label), fontSize = 11.sp, color = NeonGreen, fontWeight = FontWeight.SemiBold)
                Text(
                    activeMode.localizedLabel(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                stringResource(R.string.mb_active_mode_hint),
                fontSize = 10.sp,
                color = TextSecondary,
                textAlign = TextAlign.End,
                lineHeight = 14.sp
            )
        }

        if (replyLanguage != "FR") {
            val langLabel = when (replyLanguage) {
                "DE" -> stringResource(R.string.reply_lang_de)
                "IT" -> stringResource(R.string.reply_lang_it)
                "EN" -> stringResource(R.string.reply_lang_en)
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
                    stringResource(R.string.mb_lang_banner, langLabel),
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
                        stringResource(R.string.mb_intro_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        highlightBrand(stringResource(R.string.mb_intro_desc)),
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                    MessagePartExplainRow("👋", stringResource(R.string.mb_explain_salutation_title), stringResource(R.string.mb_explain_salutation_desc))
                    MessagePartExplainRow("💬", stringResource(R.string.mb_explain_body_title), stringResource(R.string.mb_explain_body_desc, activeMode.localizedLabel()))
                    MessagePartExplainRow("🔚", stringResource(R.string.mb_explain_ending_title), stringResource(R.string.mb_explain_ending_desc))
                    Text(
                        stringResource(R.string.mb_intro_footer),
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
                    Text(stringResource(R.string.mb_preview_label), fontSize = 12.sp, color = NeonGreen, fontWeight = FontWeight.SemiBold)
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
                    description = stringResource(R.string.mb_section_salutation_desc),
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
                    description = stringResource(R.string.mb_section_body_desc, activeMode.emoji, activeMode.localizedLabel()),
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
                    description = stringResource(R.string.mb_section_ending_desc),
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
                Text(part.localizedLabel(), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
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
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete), tint = TextSecondary, modifier = Modifier.size(14.dp))
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
            placeholder = { Text(stringResource(R.string.mb_add_part_placeholder, part.localizedLabel().lowercase()), color = TextSecondary, fontSize = 12.sp) },
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
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add), tint = Color.Black)
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
