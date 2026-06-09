package com.degage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VolumeUp
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
import com.degage.modes.AppMode
import com.degage.ui.theme.*

data class ModeInfo(
    val mode: AppMode,
    val description: String,
    val examplePhrase: String
)

val modeInfoList = listOf(
    ModeInfo(AppMode.POLI, "Courtois mais ferme.\nIdéal pour rester clean.", "\"Cette ligne n'accepte pas les sollicitations commerciales.\""),
    ModeInfo(AppMode.ADMINISTRATIF, "Froid, officiel, dissuasif.", "\"Votre appel a été classé comme démarchage non sollicité.\""),
    ModeInfo(AppMode.SARCASTIQUE, "Pour les relous assumés.", "\"Félicitations, vous avez atteint la boîte vocale la plus sarcastique de France.\""),
    ModeInfo(AppMode.TROLL, "Faites perdre du temps… beaucoup de temps.", "\"Merci de patienter, votre appel est très important pour nous.\""),
)

@Composable
fun ModesScreen(
    activeMode: AppMode,
    onSelectMode: (AppMode) -> Unit,
    onPreviewMode: (AppMode) -> Unit,
    onBack: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
    ) {
        var showInfo by remember { mutableStateOf(false) }
        if (showInfo) InfoDialog(
            title = "Modes de réponse",
            content = "Les modes définissent le ton de la réponse donnée aux spammeurs avant de raccrocher.\n\n🤝 Poli — réponse courtoise et ferme. Idéal pour rester discret.\n\n📋 Administratif — ton froid et officiel, comme un service juridique.\n\n😏 Sarcastique — réponse humoristique pour les démarcheurs qui le méritent.\n\n🎭 Troll — fait patienter le spammeur 10 secondes avant de raccrocher. Leur fait perdre du temps.\n\nTouchez un mode pour le sélectionner, puis tapez \"Écouter un aperçu\" pour l'entendre.",
            onDismiss = { showInfo = false }
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Modes de réponse", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Ne raccrochez plus. Faites répondre votre IA.", color = TextSecondary, fontSize = 13.sp)
            }
            IconButton(onClick = { showInfo = true }) {
                Icon(Icons.Default.Info, contentDescription = "Aide", tint = NeonGreen, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(modeInfoList) { info ->
                ModeCard(
                    info = info,
                    isSelected = activeMode == info.mode,
                    onClick = { onSelectMode(info.mode) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onPreviewMode(activeMode) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Écouter un aperçu", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun ModeCard(info: ModeInfo, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) NeonGreen else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(16.dp))
            .border(if (isSelected) 2.dp else 0.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = info.mode.emoji, fontSize = 32.sp)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(info.mode.label, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            Text(info.description, color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(info.examplePhrase, color = Color(0xFF6A6A6A), fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        }
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = NeonGreen, unselectedColor = TextSecondary)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun ModesScreenPreview() {
    DegageTheme {
        ModesScreen(
            activeMode = AppMode.SARCASTIQUE,
            onSelectMode = {},
            onPreviewMode = {}
        )
    }
}
