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
                    title = "👤 Mes contacts peuvent-ils être bloqués par erreur ?",
                    body = "Non. Si un numéro qui vous appelle correspond à un contact enregistré sur votre téléphone, Tu dégages laisse toujours passer l'appel — même si ce numéro figure par ailleurs dans la base spam ou dans vos règles personnalisées.\n\nCette vérification nécessite l'autorisation d'accès aux contacts, demandée au premier lancement de l'application."
                )
            }
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

            item { CategoryHeader("📋 Base de numéros bloqués") }
            item {
                ManualSection(
                    title = "🇫🇷🇨🇭 Tu dégages fonctionne-t-il en Suisse ?",
                    body = "Oui ! Dans Paramètres, choisissez votre pays (🇫🇷 France ou 🇨🇭 Suisse) tout en haut de la liste.\n\nEn Suisse, Tu dégages détecte notamment :\n\n• Les numéros à valeur ajoutée (0900, 0901, 0906) — business, marketing et divertissement, attribués individuellement par l'OFCOM.\n• Les numéros d'entreprise nationaux à coût partagé (0840, 0842, 0844, 0848, 0878) — souvent utilisés comme numéros de redirection par des centres d'appels et hotlines commerciales.\n\nLa base communautaire et votre historique personnel fonctionnent exactement de la même façon, quel que soit le pays choisi."
                )
            }
            item {
                ManualSection(
                    title = "Quels numéros sont bloqués ?",
                    body = "Tu dégages combine plusieurs sources :\n\n• Plages ARCEP documentées : 52 préfixes français officiellement attribués au démarchage téléphonique.\n• phoneblock.net : base communautaire européenne open source.\n• Signal-Spam France : association française de lutte contre le spam.\n• Base communautaire Tu dégages : numéros signalés par les utilisateurs (opt-in).\n• Votre historique personnel : tout numéro qui vous a déjà appelé et a été traité comme spam."
                )
            }
            item {
                ManualSection(
                    title = "À quoi sert le signalement d'un numéro ?",
                    body = "Quand vous bloquez un numéro et que vous avez activé « Base communautaire » dans les Paramètres, ce numéro est transmis — anonymement, sans aucune de vos données — à un serveur partagé.\n\nTous les autres utilisateurs de Tu dégages téléchargent ensuite ce numéro et le rejettent automatiquement, sans même jouer le message vocal. Plus il y a d'utilisateurs, plus la base est efficace pour tout le monde."
                )
            }
            item {
                ManualSection(
                    title = "Comment sont choisis les numéros bloqués ?",
                    body = "Un numéro est bloqué s'il appartient à une plage ARCEP connue pour le démarchage, s'il figure dans phoneblock.net ou Signal-Spam, s'il vous a déjà appelé et a été traité comme spam, ou s'il a été signalé par la communauté.\n\nTu dégages ne bloque jamais un numéro simplement parce qu'il appartient à tel ou tel opérateur télécom — voir « Pourquoi on ne bloque pas tout un opérateur » plus haut."
                )
            }
            item {
                ManualSection(
                    title = "🚫 Puis-je bloquer moi-même un numéro ou un préfixe ?",
                    body = "Oui ! Dans Paramètres → Numéros bloqués manuellement, vous pouvez ajouter vos propres règles :\n\n• Numéro exact : bloque uniquement ce numéro précis (ex. 0612345678).\n• Préfixe : bloque tous les numéros commençant par cette suite de chiffres (ex. 0033612 bloque tous les numéros commençant ainsi).\n\nCes règles s'ajoutent à la base spam automatique : tout appel correspondant est rejeté immédiatement, sans message vocal. Vous pouvez supprimer une règle à tout moment depuis le même écran."
                )
            }

            item { CategoryHeader("📱 Utilisation") }
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
                    body = "Dans Paramètres :\n\n• Décroche automatique : Tu dégages répond à l'appel sans faire sonner votre téléphone (recommandé).\n• Bloquer après réponse : ajoute le numéro à votre liste noire personnelle après chaque interaction.\n• Notifications : si activé, vous recevez une notification Android à chaque appel bloqué (numéro et raison du blocage)."
                )
            }

            item { CategoryHeader("🔧 Problèmes") }
            item {
                ManualSection(
                    title = "Que faire si un numéro est bloqué à tort ?",
                    body = "Tu dégages n'a pas encore de bouton « débloquer » intégré. En attendant :\n\n• Si vous attendez un appel important d'un numéro inconnu, désactivez temporairement la protection (écran d'accueil) le temps de l'appel.\n• Si un numéro légitime a été ajouté par erreur à la base communautaire, contactez-nous (voir À propos) pour le faire retirer du serveur partagé.\n\nUne fonction de liste blanche est à l'étude pour une prochaine version."
                )
            }
            item {
                ManualSection(
                    title = "Que faire si je suis victime de spoofing ou d'usurpation de numéro ?",
                    body = "Le spoofing (un spammeur qui affiche un faux numéro, parfois le vôtre ou celui d'un proche) est un problème du réseau téléphonique que Tu dégages ne peut pas corriger : l'appli ne voit que le numéro affiché par l'opérateur.\n\nSi vous recevez des appels avec votre propre numéro ou un numéro usurpé, le signalement à votre opérateur ou au 33700 (service anti-arnaque SMS/appels) reste la solution la plus efficace."
                )
            }
            item {
                ManualSection(
                    title = "Un numéro spam n'est pas bloqué, que faire ?",
                    body = "1. Vérifiez que la protection est activée (écran d'accueil).\n2. Vérifiez que Tu dégages est bien défini comme service d'identification d'appel par défaut (Paramètres Android → Applications → Application de filtrage des appels).\n3. Mettez à jour la base spam manuellement : Paramètres → Mettre à jour la base spam.\n4. Si le numéro continue d'appeler après ça, il sera mémorisé dès le premier blocage et rejeté automatiquement la fois suivante."
                )
            }
            item {
                ManualSection(
                    title = "Les appelants peuvent-ils laisser un message vocal ?",
                    body = "Non. Tu dégages joue son propre message à l'appelant puis raccroche la ligne — l'appelant n'a pas accès à votre messagerie vocale habituelle pour cet appel.\n\nC'est volontaire : un démarcheur qui ne peut ni vous parler ni laisser de message a beaucoup moins de raisons de rappeler."
                )
            }
            item {
                ManualSection(
                    title = "❓ Pourquoi le blocage ne fonctionne pas ?",
                    body = "Si Tu dégages ne bloque pas les appels, vérifiez :\n\n1. La protection est bien sur ON (écran d'accueil).\n2. Tu dégages est bien défini comme service de filtrage d'appels : Paramètres Android → Applications → Applications par défaut → Service d'identification de l'appelant et anti-spam → Sélectionner Tu dégages.\n3. Les permissions téléphone sont accordées à Tu dégages."
                )
            }

            item { CategoryHeader("⚙️ Fonctionnalités & technique") }
            item {
                ManualSection(
                    title = "Tu dégages consomme-t-il beaucoup de batterie ?",
                    body = "Non. Tu dégages ne tourne pas en permanence en arrière-plan : Android ne réveille l'appli qu'au moment précis où un appel arrive, via le service de filtrage d'appels du système.\n\nEn dehors des appels, l'appli est totalement inactive. La seule activité réseau a lieu lors de la mise à jour de la base spam (manuelle ou à l'ouverture de l'appli)."
                )
            }

            item { CategoryHeader("❓ Autres") }
            item {
                ManualSection(
                    title = "Comment est protégée ma vie privée ?",
                    body = "Par défaut, Tu dégages ne collecte et ne transmet aucune donnée : tout reste sur votre téléphone.\n\nSi vous activez la base communautaire (opt-in), seul le numéro de l'appelant spam est transmis à un serveur hébergé en Europe — jamais votre numéro ni vos informations personnelles. Le détail complet est dans À propos → Politique de confidentialité."
                )
            }
            item {
                ManualSection(
                    title = "Comment signaler un bug ou donner un avis ?",
                    body = "Vous pouvez nous écrire directement à l'adresse de contact indiquée dans À propos. Décrivez le problème rencontré (modèle de téléphone, version d'Android, ce qui s'est passé) — ça nous aide énormément à améliorer Tu dégages."
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun CategoryHeader(title: String) {
    Text(
        title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = NeonGreen,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
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
