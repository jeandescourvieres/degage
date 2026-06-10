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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        title = "Numéros bloqués manuellement",
        content = "Ajoutez vos propres règles de blocage, en plus de la base spam automatique :\n\n" +
            "• Numéro exact : bloque uniquement ce numéro précis (ex. 0612345678).\n" +
            "• Préfixe : bloque tous les numéros commençant par cette suite de chiffres (ex. 0033612 bloque tous les numéros commençant ainsi).\n\n" +
            "Tout appel correspondant à une règle est rejeté immédiatement, sans message vocal.",
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
            Text("Numéros bloqués manuellement", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
            IconButton(onClick = { showInfo = true }) {
                Icon(Icons.Default.Info, contentDescription = "Aide", tint = NeonGreen, modifier = Modifier.size(26.dp))
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
            Text("Ajouter une règle", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text(if (isPrefix) "Ex. 0033612" else "Ex. 0612345678") },
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
                Text("Bloquer comme préfixe", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
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
                Text("Ajouter", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (blocks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucune règle personnalisée", color = TextSecondary)
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
                if (block.isPrefix) "Préfixe — bloque tous les numéros commençant ainsi" else "Numéro exact",
                color = NeonGreen,
                fontSize = 12.sp
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = TextSecondary, modifier = Modifier.size(18.dp))
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
