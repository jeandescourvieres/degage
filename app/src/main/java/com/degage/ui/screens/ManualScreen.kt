package com.degage.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.ui.theme.*

@Composable
fun ManualScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
            }
            Text("Mode d'emploi", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ManualSection(
                    title = "🛡️ À quoi sert Tu dégages ?",
                    body = "Tu dégages intercepte automatiquement les appels provenant de numéros de démarcheurs connus, leur joue un message vocal, puis raccroche — sans que votre téléphone ait sonné.\n\nVous économisez du temps et de l'énergie en n'ayant plus jamais à décrocher pour entendre un robot vous proposer une cuisine équipée."
                )
            }
            item {
                ManualSection(
                    title = "🎯 Pourquoi on ne bloque pas « tout un opérateur » ?",
                    body = "Certaines applis concurrentes annoncent bloquer en bloc des numéros appartenant à des opérateurs VoIP (Aircall, Vonage, Manifone, Tata Communications, CM Telecom, etc.).\n\nTu dégages a fait le choix inverse, et voici pourquoi :\n\nCes opérateurs sont des fournisseurs VoIP généralistes, pas des « opérateurs spam ». Ils sont utilisés :\n❌ par des centres d'appels de démarchage,\n✅ mais aussi par des entreprises légitimes : support client, livraisons, rendez-vous médicaux, artisans, etc.\n\nSi on bloquait « tout Aircall » ou « tout Vonage », on bloquerait aussi ces appels légitimes — gros risque de faux positifs et de plaintes (« pourquoi mon livreur n'arrive pas à m'appeler ?! »).\n\nLa bonne approche, c'est celle qu'on a construite : la base communautaire (voir plus bas) capture les numéros réellement signalés comme spam par les utilisateurs, peu importe l'opérateur derrière — sans jamais bloquer les usages légitimes du même opérateur.\n\nTu dégages préfère un blocage précis et fiable plutôt qu'un rejet à l'arrache fait pour impressionner."
                )
            }
            item {
                ManualSection(
                    title = "⚡ Démarrage rapide",
                    body = "1. Ouvrez Tu dégages.\n2. Assurez-vous que le bouton de protection est sur ON (vert) sur l'écran d'accueil.\n3. C'est tout. Tu dégages travaille en arrière-plan."
                )
            }
            item {
                ManualSection(
                    title = "🎭 Les 4 modes de réponse",
                    body = "Accédez à l'onglet « Modes » pour choisir le ton de la réponse :\n\n🤝 Poli — réponse courtoise et ferme. Idéal si vous préférez rester discret.\n\n📋 Administratif — ton froid et officiel, comme un service juridique. Déstabilise les opérateurs de centres d'appels.\n\n😏 Sarcastique — réponse humoristique. Pour les démarcheurs qui méritent un peu d'ironie.\n\n🎭 Troll — fait patienter le spammeur pendant 10 secondes de musique d'attente avant de raccrocher. Leur fait perdre du temps et de l'argent."
                )
            }
            item {
                ManualSection(
                    title = "💬 Personnaliser les messages",
                    body = "Dans Paramètres → Personnaliser les réponses, vous pouvez :\n\n• Activer ou désactiver chaque phrase existante.\n• Ajouter vos propres phrases personnalisées.\n• Utiliser le Constructeur de messages pour composer un message en 3 parties : salutation + corps + conclusion."
                )
            }
            item {
                ManualSection(
                    title = "🎙️ Paramètres vocaux",
                    body = "Dans Paramètres → Paramètres vocaux :\n\n• Choisissez la voix (selon les voix installées sur votre téléphone).\n• Ajustez la vitesse de lecture (de lent à rapide).\n• Modifiez la hauteur de la voix (grave ou aiguë).\n• Testez le rendu avec le bouton de prévisualisation."
                )
            }
            item {
                ManualSection(
                    title = "📋 Historique",
                    body = "L'onglet Historique liste tous les appels bloqués :\n\n• Numéro de téléphone détecté.\n• Date et heure du blocage.\n• Mode utilisé pour répondre.\n• Message exact joué à l'appelant.\n\nUtilisez les filtres (Tous / Bloqués / Réponses / Manuels) pour affiner l'affichage. Supprimez une entrée avec l'icône 🗑️."
                )
            }
            item {
                ManualSection(
                    title = "📊 Statistiques",
                    body = "L'onglet Statistiques affiche :\n\n• Le temps total économisé (graphique circulaire).\n• Le nombre total de spammeurs bloqués depuis l'installation.\n• La moyenne d'appels évités par jour.\n• La durée moyenne d'un appel de démarchage évitée."
                )
            }
            item {
                ManualSection(
                    title = "⚙️ Options avancées",
                    body = "Dans Paramètres :\n\n• Décroche automatique : Tu dégages répond à l'appel sans faire sonner votre téléphone (recommandé).\n• Bloquer après réponse : ajoute le numéro à votre liste noire personnelle après chaque interaction.\n• Notifications : reçois une notification à chaque appel bloqué."
                )
            }
            item {
                ManualSection(
                    title = "❓ Pourquoi le blocage ne fonctionne pas ?",
                    body = "Si Tu dégages ne bloque pas les appels, vérifiez :\n\n1. La protection est bien sur ON (écran d'accueil).\n2. Tu dégages est bien défini comme service de filtrage d'appels : Paramètres Android → Applications → Applications par défaut → Service d'identification de l'appelant et anti-spam → Sélectionner Tu dégages.\n3. Les permissions téléphone sont accordées à Tu dégages."
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ManualSection(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(10.dp))
        Text(body, fontSize = 14.sp, color = TextSecondary, lineHeight = 22.sp)
    }
}
