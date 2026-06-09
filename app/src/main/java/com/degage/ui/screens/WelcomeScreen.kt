package com.degage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.ui.theme.*

@Composable
fun WelcomeScreen(
    onDismiss: () -> Unit,
    onBack: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
            Text(
                text = "Bienvenue sur Tu dégages",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                // Hero
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonGreenDim.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🤖", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Les spammeurs adorent\nvous appeler ?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )
                    Text(
                        text = "Votre IA va adorer\nleur répondre !",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGreen,
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Ne vous contentez plus de raccrocher. Faites répondre votre IA personnelle avec l'humour de votre choix.",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp
                    )
                }
            }

            item {
                WelcomeSection(
                    "🤖 Blocage intelligent avec réponse IA",
                    "Tu dégages filtre les appels indésirables et les redirige vers votre assistant vocal IA personnalisé — avant de mettre fin à la conversation. Le démarcheur parle à votre IA, pas à vous."
                )
            }
            item {
                WelcomeSection(
                    "🎭 Pas comme les autres applis",
                    "Les applications classiques bloquent les appels. La nôtre permet à votre IA de répondre avec le ton que vous choisissez — Poli, Administratif, Sarcastique ou Troll — avant de raccrocher."
                )
            }
            item {
                WelcomeSection(
                    "💬 Votre IA, votre voix",
                    "Composez vos propres formules, choisissez le ton, réglez la voix. Votre IA vous ressemble. Les spammeurs méritent une réponse à la hauteur."
                )
            }
            item {
                WelcomeSection(
                    "📊 Chaque bataille comptée",
                    "Historique complet, temps économisé, numéros mémorisés pour rejet immédiat. Vous saurez exactement combien de fois votre IA a gagné."
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonGreenDim.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                        .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "💡 Bon à savoir",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Vous pouvez revoir cette présentation à tout moment depuis Paramètres → Revoir la présentation.",
                        fontSize = 14.sp,
                        color = Color.White,
                        lineHeight = 21.sp
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // Bouton dismiss
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBg)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text(
                    text = "J'ai compris, ne plus afficher",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun WelcomeSection(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(6.dp))
        Text(body, fontSize = 13.sp, color = TextSecondary, lineHeight = 20.sp)
    }
}
