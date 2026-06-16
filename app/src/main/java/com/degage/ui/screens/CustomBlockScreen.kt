package com.degage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.R
import com.degage.database.entities.CustomBlockEntity
import com.degage.ui.components.InfoDialog
import com.degage.ui.theme.*

@Composable
fun CustomBlockScreen(
    blocks: List<CustomBlockEntity>,
    onAdd: (String, Boolean) -> Unit,
    onDelete: (CustomBlockEntity) -> Unit,
    onBack: () -> Unit = {},
) {
    var showInfo by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var isPrefix by remember { mutableStateOf(false) }

    if (showInfo) InfoDialog(
        title = stringResource(R.string.custom_block_info_title),
        content = stringResource(R.string.custom_block_info_content),
        onDismiss = { showInfo = false }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(AccentCyan).padding(top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = Color.Black)
            }
            Text(stringResource(R.string.custom_block_title), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f))
            IconButton(onClick = { showInfo = true }) {
                Icon(Icons.Default.Info, contentDescription = stringResource(R.string.cd_help), tint = Color.Black, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // ── Formulaire d'ajout ───────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBg, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.custom_block_add_rule), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text(if (isPrefix) stringResource(R.string.custom_block_placeholder_prefix) else stringResource(R.string.custom_block_placeholder_exact)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = TextSecondary,
                    cursorColor = NeonGreen,
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.custom_block_toggle_prefix), color = TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Switch(
                    checked = isPrefix,
                    onCheckedChange = { isPrefix = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = NeonGreen,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = CardBgAlt
                    )
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    if (input.isNotBlank()) {
                        onAdd(input, isPrefix)
                        input = ""
                        isPrefix = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
            ) {
                Text(stringResource(R.string.custom_block_add_button), fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (blocks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.custom_block_empty), color = TextSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(blocks, key = { it.id }) { block ->
                    CustomBlockRow(block = block, onDelete = { onDelete(block) })
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun CustomBlockRow(block: CustomBlockEntity, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(block.value, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 15.sp)
            Text(
                if (block.isPrefix) stringResource(R.string.custom_block_prefix_label) else stringResource(R.string.custom_block_exact_label),
                color = NeonGreen,
                fontSize = 12.sp
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete), tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun CustomBlockPreview() {
    DegageTheme {
        CustomBlockScreen(
            blocks = listOf(
                CustomBlockEntity(id = 1, value = "0612345678", isPrefix = false),
                CustomBlockEntity(id = 2, value = "0033612", isPrefix = true),
            ),
            onAdd = { _, _ -> },
            onDelete = {}
        )
    }
}
