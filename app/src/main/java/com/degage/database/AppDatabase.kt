package com.degage.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.degage.database.dao.BlockedCallDao
import com.degage.database.dao.CallAttemptDao
import com.degage.database.dao.CustomBlockDao
import com.degage.database.dao.ReplyDao
import com.degage.database.dao.SpamDao
import com.degage.database.dao.WhitelistDao
import com.degage.database.entities.BlockedCallEntity
import com.degage.database.entities.CallAttemptEntity
import com.degage.database.entities.CustomBlockEntity
import com.degage.database.entities.ReplyEntity
import com.degage.database.entities.SpamEntry
import com.degage.database.entities.WhitelistEntry
import com.degage.modes.AppMode
import com.degage.replies.MODE_GLOBAL
import com.degage.replies.MessagePart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [BlockedCallEntity::class, ReplyEntity::class, SpamEntry::class, CustomBlockEntity::class, WhitelistEntry::class, CallAttemptEntity::class],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedCallDao(): BlockedCallDao
    abstract fun replyDao(): ReplyDao
    abstract fun spamDao(): SpamDao
    abstract fun customBlockDao(): CustomBlockDao
    abstract fun whitelistDao(): WhitelistDao
    abstract fun callAttemptDao(): CallAttemptDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `spam_entries` (
                        `number` TEXT NOT NULL,
                        `source` TEXT NOT NULL DEFAULT 'auto_block',
                        `reportCount` INTEGER NOT NULL DEFAULT 1,
                        `firstSeen` INTEGER NOT NULL DEFAULT 0,
                        `lastSeen` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`number`)
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `custom_blocks` (
                        `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        `value` TEXT NOT NULL,
                        `isPrefix` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `whitelist` (
                        `number` TEXT NOT NULL PRIMARY KEY,
                        `createdAt` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `call_attempts` (
                        `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        `number` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE replies ADD COLUMN language TEXT NOT NULL DEFAULT 'FR'")
                (DEFAULT_REPLIES_DE + DEFAULT_REPLIES_IT).forEach { insertReply(database, it) }
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                DEFAULT_REPLIES_EN.forEach { insertReply(database, it) }
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                DEFAULT_REPLIES_ES.forEach { insertReply(database, it) }
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE replies ADD COLUMN isStandalone INTEGER NOT NULL DEFAULT 0")
                // Administratif disparait : ses anciennes reponses (orphelines) restent en base
                // mais ne sont plus jamais lues, aucun mode ne porte plus ce nom.
                // Les modeles de base complets remplacent les phrases courtes comme reponse
                // par defaut en francais ; on desactive donc les anciennes phrases composables
                // encore actives pour ne garder qu'un seul corps actif par mode.
                database.execSQL(
                    "UPDATE replies SET isEnabled = 0 WHERE partType = 'BODY' AND language = 'FR' AND isEnabled = 1 " +
                        "AND modeName IN ('POLI','SARCASTIQUE','TROLL')"
                )
                DEFAULT_REPLIES.filter { it.isStandalone }.forEach { insertReply(database, it) }
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Meme operation que la migration precedente, pour les 4 autres langues :
                // les modeles de base complets deviennent la reponse par defaut.
                listOf("DE", "IT", "EN", "ES").forEach { lang ->
                    database.execSQL(
                        "UPDATE replies SET isEnabled = 0 WHERE partType = 'BODY' AND language = ? AND isEnabled = 1 " +
                            "AND modeName IN ('POLI','SARCASTIQUE','TROLL')",
                        arrayOf(lang)
                    )
                }
                (DEFAULT_REPLIES_DE + DEFAULT_REPLIES_IT + DEFAULT_REPLIES_EN + DEFAULT_REPLIES_ES)
                    .filter { it.isStandalone }
                    .forEach { insertReply(database, it) }
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // La salutation et la formule de fin choisies par l'utilisateur s'appliquent
                // maintenant aussi aux modeles de base complets : on retire la salutation
                // integree au texte (sinon elle apparaissait en double avec celle choisie).
                (DEFAULT_REPLIES + DEFAULT_REPLIES_DE + DEFAULT_REPLIES_IT + DEFAULT_REPLIES_EN + DEFAULT_REPLIES_ES)
                    .filter { it.partType == MessagePart.BODY.name && it.modeName != MODE_GLOBAL && it.isEnabled }
                    .forEach { reply ->
                        database.execSQL(
                            "UPDATE replies SET text = ? WHERE modeName = ? AND language = ? AND partType = 'BODY' AND isStandalone = 1",
                            arrayOf(reply.text, reply.modeName, reply.language)
                        )
                    }
            }
        }

        private fun insertReply(database: SupportSQLiteDatabase, reply: ReplyEntity) {
            val values = ContentValues().apply {
                put("text", reply.text)
                put("modeName", reply.modeName)
                put("partType", reply.partType)
                put("isEnabled", if (reply.isEnabled) 1 else 0)
                put("isCustom", if (reply.isCustom) 1 else 0)
                put("language", reply.language)
                put("isStandalone", if (reply.isStandalone) 1 else 0)
            }
            database.insert("replies", SQLiteDatabase.CONFLICT_REPLACE, values)
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "degage.db")
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).replyDao().let { dao ->
                                    (DEFAULT_REPLIES + DEFAULT_REPLIES_DE + DEFAULT_REPLIES_IT + DEFAULT_REPLIES_EN + DEFAULT_REPLIES_ES).forEach { dao.insert(it) }
                                }
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private val DEFAULT_REPLIES = listOf(
            // ── Salutations globales ──────────────────────────────────────
            ReplyEntity(text = "Bonjour.", modeName = MODE_GLOBAL, partType = MessagePart.SALUTATION.name),
            ReplyEntity(text = "Bonjour et bienvenue sur DÉGAGE.", modeName = MODE_GLOBAL, partType = MessagePart.SALUTATION.name, isEnabled = false),
            ReplyEntity(text = "Bonjour. Cette ligne est protégée.", modeName = MODE_GLOBAL, partType = MessagePart.SALUTATION.name, isEnabled = false),
            ReplyEntity(text = "Bonjour. Votre appel est en cours d'analyse.", modeName = MODE_GLOBAL, partType = MessagePart.SALUTATION.name, isEnabled = false),

            // ── Corps — Poli (alternative composable, desactivee par defaut) ──
            ReplyEntity(text = "Cette ligne n'accepte pas les sollicitations commerciales.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, isEnabled = false),
            ReplyEntity(text = "Merci mais cette ligne ne souhaite pas être démarchée.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, isEnabled = false),
            ReplyEntity(text = "Merci de retirer ce numéro de vos listes.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, isEnabled = false),

            // ── Corps — Sarcastique (alternative composable, desactivee par defaut) ──
            ReplyEntity(text = "Cette ligne pratique activement le rejet du démarchage.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false),
            ReplyEntity(text = "Cette ligne soutient la disparition du démarchage téléphonique.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false),
            ReplyEntity(text = "Cette ligne est allergique au démarchage.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false),

            // ── Corps — Troll (alternative composable, desactivee par defaut) ──
            ReplyEntity(text = "Transfert vers le service concerné…", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, isEnabled = false),

            // ── Modeles de base complets (corps detaille par mode) ──
            ReplyEntity(
                text = "Merci pour votre message et pour l'intérêt que vous me portez.\n\nAprès lecture de votre proposition, je ne souhaite pas y donner suite pour le moment.\n\nJe vous remercie de votre compréhension et vous souhaite une excellente continuation.",
                modeName = AppMode.POLI.name, partType = MessagePart.BODY.name
            ),
            ReplyEntity(
                text = "Merci pour votre prise de contact.\n\nAprès examen de votre message, votre proposition ne correspond pas à mes besoins actuels. Je ne souhaite donc pas poursuivre cet échange.\n\nJe vous remercie pour votre démarche et vous souhaite une bonne journée.\n\nCordialement.",
                modeName = AppMode.PRO.name, partType = MessagePart.BODY.name
            ),
            ReplyEntity(
                text = "Merci d'avoir pensé à moi pour cette proposition.\n\nMême si elle semble intéressante, ce n'est pas quelque chose qui me convient actuellement. Je vais donc passer mon tour.\n\nJe vous souhaite malgré tout beaucoup de succès dans vos démarches.",
                modeName = AppMode.AMICAL.name, partType = MessagePart.BODY.name
            ),
            ReplyEntity(
                text = "Merci pour votre message.\n\nJe ne suis pas intéressé par cette proposition et ne souhaite pas être recontacté à ce sujet.\n\nBonne continuation.",
                modeName = AppMode.DIRECT.name, partType = MessagePart.BODY.name
            ),
            ReplyEntity(
                text = "Merci pour votre proposition.\n\nAprès une réunion stratégique intensive avec moi-même, un café et mon agenda, nous sommes arrivés à la conclusion que ce n'était pas le bon moment.\n\nJe vais donc décliner cette offre.\n\nExcellente journée à vous.",
                modeName = AppMode.HUMOUR.name, partType = MessagePart.BODY.name
            ),
            ReplyEntity(
                text = "Merci pour cette opportunité manifestement exceptionnelle.\n\nMalheureusement, après une analyse approfondie d'environ trois secondes, j'ai décidé de ne pas donner suite à votre proposition.\n\nJe vous souhaite néanmoins bonne chance dans votre quête de prospects plus enthousiastes.",
                modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name
            ),
            ReplyEntity(
                text = "Votre message a bien été reçu et transmis à mon comité de sélection.\n\nAprès plusieurs débats animés, deux votes contre, un vote blanc et l'abstention du président, la décision est tombée : votre proposition ne sera pas retenue.\n\nNous vous remercions pour votre participation et vous souhaitons une excellente continuation.",
                modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name
            ),
            ReplyEntity(
                text = "Votre message a été analysé par le système.\n\nRésultat : proposition détectée. Niveau d'intérêt estimé : 0,7 %.\n\nAction exécutée : refus poli.\n\nMerci de votre compréhension.\n\nFin de transmission.",
                modeName = AppMode.ROBOT.name, partType = MessagePart.BODY.name
            ),
            ReplyEntity(
                text = "Message reçu.\n\nJe ne suis pas intéressé par cette proposition.\n\nAucune suite ne sera donnée.\n\nCordialement.",
                modeName = AppMode.FROID.name, partType = MessagePart.BODY.name
            ),
            ReplyEntity(
                text = "Merci pour votre message.\n\nComme la majorité des sollicitations non demandées que je reçois, votre proposition ne présente aucun intérêt pour moi.\n\nJe vous invite donc à ne pas perdre davantage votre temps ni le mien en poursuivant cet échange.\n\nBonne continuation.",
                modeName = AppMode.CINGLANT.name, partType = MessagePart.BODY.name
            ),

            // ── Formules de fin globales ──────────────────────────────────
            ReplyEntity(text = "À pas bientôt.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name),
            ReplyEntity(text = "Merci de ne jamais rappeler.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, isEnabled = false),
            ReplyEntity(text = "Cordialement… enfin presque.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, isEnabled = false),
            ReplyEntity(text = "Veuillez oublier ce numéro.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, isEnabled = false),
            ReplyEntity(text = "Bonne continuation ailleurs.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, isEnabled = false),
        )

        private val DEFAULT_REPLIES_DE = listOf(
            // ── Salutations globales (allemand) ───────────────────────────
            ReplyEntity(text = "Guten Tag.", modeName = MODE_GLOBAL, partType = MessagePart.SALUTATION.name, language = "DE"),
            ReplyEntity(text = "Guten Tag und willkommen.", modeName = MODE_GLOBAL, partType = MessagePart.SALUTATION.name, isEnabled = false, language = "DE"),
            ReplyEntity(text = "Guten Tag. Diese Leitung ist geschützt.", modeName = MODE_GLOBAL, partType = MessagePart.SALUTATION.name, isEnabled = false, language = "DE"),

            // ── Corps — Poli (allemand, alternative composable) ────────────
            ReplyEntity(text = "Diese Leitung nimmt keine kommerziellen Werbeanrufe entgegen.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, isEnabled = false, language = "DE"),
            ReplyEntity(text = "Bitte entfernen Sie diese Nummer von Ihrer Liste.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, isEnabled = false, language = "DE"),

            // ── Corps — Sarcastique (allemand, alternative composable) ─────
            ReplyEntity(text = "Herzlichen Glückwunsch, Sie haben die sarkastischste Mailbox Deutschlands erreicht.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false, language = "DE"),
            ReplyEntity(text = "Diese Leitung ist allergisch gegen Werbeanrufe.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false, language = "DE"),

            // ── Corps — Troll (allemand, alternative composable) ───────────
            ReplyEntity(text = "Bitte warten Sie, Ihr Anruf ist uns sehr wichtig.", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, isEnabled = false, language = "DE"),
            ReplyEntity(text = "Einen Moment bitte, Sie werden verbunden…", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, isEnabled = false, language = "DE"),

            // ── Modeles de base complets (allemand) ─────────────────────────
            ReplyEntity(text = "Vielen Dank für Ihre Nachricht und Ihr Interesse an mir.\n\nNach Lektüre Ihres Angebots möchte ich vorerst nicht weiter darauf eingehen.\n\nVielen Dank für Ihr Verständnis und alles Gute für Ihre weiteren Vorhaben.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, language = "DE"),
            ReplyEntity(text = "Vielen Dank für Ihre Kontaktaufnahme.\n\nNach Prüfung Ihrer Nachricht entspricht Ihr Angebot nicht meinen aktuellen Bedürfnissen. Ich möchte diesen Austausch daher nicht fortsetzen.\n\nVielen Dank für Ihre Bemühungen und einen schönen Tag noch.\n\nMit freundlichen Grüßen.", modeName = AppMode.PRO.name, partType = MessagePart.BODY.name, language = "DE"),
            ReplyEntity(text = "Danke, dass Sie an mich gedacht haben.\n\nAuch wenn es interessant klingt, passt es momentan nicht zu mir. Ich werde es daher vorbeiziehen lassen.\n\nIch wünsche Ihnen trotzdem viel Erfolg bei Ihren weiteren Vorhaben.", modeName = AppMode.AMICAL.name, partType = MessagePart.BODY.name, language = "DE"),
            ReplyEntity(text = "Danke für Ihre Nachricht.\n\nIch bin an diesem Angebot nicht interessiert und möchte diesbezüglich nicht erneut kontaktiert werden.\n\nAlles Gute.", modeName = AppMode.DIRECT.name, partType = MessagePart.BODY.name, language = "DE"),
            ReplyEntity(text = "Danke für Ihr Angebot.\n\nNach einer intensiven Strategiesitzung mit mir selbst, einem Kaffee und meinem Kalender sind wir zu dem Schluss gekommen, dass dies nicht der richtige Moment ist.\n\nIch werde dieses Angebot daher ablehnen.\n\nEinen ausgezeichneten Tag noch.", modeName = AppMode.HUMOUR.name, partType = MessagePart.BODY.name, language = "DE"),
            ReplyEntity(text = "Danke für diese offensichtlich außergewöhnliche Gelegenheit.\n\nLeider habe ich nach einer gründlichen Analyse von etwa drei Sekunden beschlossen, Ihrem Angebot nicht weiter nachzugehen.\n\nIch wünsche Ihnen trotzdem viel Glück bei der Suche nach begeisterungsfähigeren Interessenten.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, language = "DE"),
            ReplyEntity(text = "Ihre Nachricht wurde empfangen und an meinen Auswahlausschuss weitergeleitet.\n\nNach mehreren lebhaften Debatten, zwei Gegenstimmen, einer Enthaltung und der Stimmenthaltung des Vorsitzenden fiel die Entscheidung: Ihr Angebot wird nicht berücksichtigt.\n\nWir danken Ihnen für Ihre Teilnahme und wünschen Ihnen alles Gute.", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, language = "DE"),
            ReplyEntity(text = "Ihre Nachricht wurde vom System analysiert.\n\nErgebnis: Angebot erkannt. Geschätztes Interessenniveau: 0,7 %.\n\nDurchgeführte Aktion: höfliche Ablehnung.\n\nVielen Dank für Ihr Verständnis.\n\nÜbertragung beendet.", modeName = AppMode.ROBOT.name, partType = MessagePart.BODY.name, language = "DE"),
            ReplyEntity(text = "Nachricht erhalten.\n\nIch bin an diesem Angebot nicht interessiert.\n\nEs wird keine weitere Reaktion erfolgen.\n\nMit freundlichen Grüßen.", modeName = AppMode.FROID.name, partType = MessagePart.BODY.name, language = "DE"),
            ReplyEntity(text = "Danke für Ihre Nachricht.\n\nWie die meisten unerwünschten Anfragen, die ich erhalte, ist Ihr Angebot für mich ohne jedes Interesse.\n\nIch lade Sie daher ein, weder Ihre noch meine Zeit mit der Fortsetzung dieses Austauschs zu verschwenden.\n\nAlles Gute.", modeName = AppMode.CINGLANT.name, partType = MessagePart.BODY.name, language = "DE"),

            // ── Formules de fin globales (allemand) ───────────────────────
            ReplyEntity(text = "Auf Wiederhören.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, language = "DE"),
            ReplyEntity(text = "Bitte rufen Sie nicht mehr an.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, isEnabled = false, language = "DE"),
            ReplyEntity(text = "Vergessen Sie diese Nummer.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, isEnabled = false, language = "DE"),
        )

        private val DEFAULT_REPLIES_IT = listOf(
            // ── Salutations globales (italien) ────────────────────────────
            ReplyEntity(text = "Buongiorno.", modeName = MODE_GLOBAL, partType = MessagePart.SALUTATION.name, language = "IT"),
            ReplyEntity(text = "Buongiorno e benvenuti.", modeName = MODE_GLOBAL, partType = MessagePart.SALUTATION.name, isEnabled = false, language = "IT"),
            ReplyEntity(text = "Buongiorno. Questa linea è protetta.", modeName = MODE_GLOBAL, partType = MessagePart.SALUTATION.name, isEnabled = false, language = "IT"),

            // ── Corps — Poli (italien, alternative composable) ─────────────
            ReplyEntity(text = "Questa linea non accetta sollecitazioni commerciali.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, isEnabled = false, language = "IT"),
            ReplyEntity(text = "La preghiamo di rimuovere questo numero dalle vostre liste.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, isEnabled = false, language = "IT"),

            // ── Corps — Sarcastique (italien, alternative composable) ──────
            ReplyEntity(text = "Congratulazioni, ha raggiunto la segreteria telefonica più sarcastica d'Italia.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false, language = "IT"),
            ReplyEntity(text = "Questa linea è allergica alle chiamate pubblicitarie.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false, language = "IT"),

            // ── Corps — Troll (italien, alternative composable) ────────────
            ReplyEntity(text = "Attenda in linea, la sua chiamata è molto importante per noi.", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, isEnabled = false, language = "IT"),
            ReplyEntity(text = "Un momento, la stiamo mettendo in contatto…", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, isEnabled = false, language = "IT"),

            // ── Modeles de base complets (italien) ──────────────────────────
            ReplyEntity(text = "Grazie per il suo messaggio e per l'interesse che mi ha dimostrato.\n\nDopo aver letto la sua proposta, per il momento non desidero darle seguito.\n\nLa ringrazio per la comprensione e le auguro un'ottima continuazione.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, language = "IT"),
            ReplyEntity(text = "Grazie per il suo contatto.\n\nDopo un esame del suo messaggio, la sua proposta non corrisponde alle mie attuali esigenze. Non desidero quindi proseguire questo scambio.\n\nLa ringrazio per il suo impegno e le auguro una buona giornata.\n\nCordiali saluti.", modeName = AppMode.PRO.name, partType = MessagePart.BODY.name, language = "IT"),
            ReplyEntity(text = "Grazie per aver pensato a me per questa proposta.\n\nAnche se sembra interessante, al momento non è qualcosa che mi convince. Passerò quindi il mio turno.\n\nLe auguro comunque molto successo nelle sue iniziative.", modeName = AppMode.AMICAL.name, partType = MessagePart.BODY.name, language = "IT"),
            ReplyEntity(text = "Grazie per il suo messaggio.\n\nNon sono interessato a questa proposta e non desidero essere ricontattato in merito.\n\nBuona continuazione.", modeName = AppMode.DIRECT.name, partType = MessagePart.BODY.name, language = "IT"),
            ReplyEntity(text = "Grazie per la sua proposta.\n\nDopo una riunione strategica intensiva con me stesso, un caffè e la mia agenda, siamo giunti alla conclusione che non è il momento giusto.\n\nDeclinerò quindi questa offerta.\n\nOttima giornata a lei.", modeName = AppMode.HUMOUR.name, partType = MessagePart.BODY.name, language = "IT"),
            ReplyEntity(text = "Grazie per questa opportunità a quanto pare eccezionale.\n\nPurtroppo, dopo un'analisi approfondita di circa tre secondi, ho deciso di non dare seguito alla sua proposta.\n\nLe auguro comunque buona fortuna nella ricerca di clienti più entusiasti.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, language = "IT"),
            ReplyEntity(text = "Il suo messaggio è stato ricevuto e trasmesso al mio comitato di selezione.\n\nDopo diversi dibattiti animati, due voti contrari, una scheda bianca e l'astensione del presidente, la decisione è stata presa: la sua proposta non sarà accolta.\n\nLa ringraziamo per la partecipazione e le auguriamo un'ottima continuazione.", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, language = "IT"),
            ReplyEntity(text = "Il suo messaggio è stato analizzato dal sistema.\n\nRisultato: proposta rilevata. Livello di interesse stimato: 0,7 %.\n\nAzione eseguita: rifiuto cortese.\n\nGrazie per la comprensione.\n\nFine della trasmissione.", modeName = AppMode.ROBOT.name, partType = MessagePart.BODY.name, language = "IT"),
            ReplyEntity(text = "Messaggio ricevuto.\n\nNon sono interessato a questa proposta.\n\nNon sarà data alcuna risposta ulteriore.\n\nCordiali saluti.", modeName = AppMode.FROID.name, partType = MessagePart.BODY.name, language = "IT"),
            ReplyEntity(text = "Grazie per il suo messaggio.\n\nCome la maggior parte delle richieste non sollecitate che ricevo, la sua proposta non presenta alcun interesse per me.\n\nLa invito quindi a non perdere ulteriormente il suo tempo né il mio continuando questo scambio.\n\nBuona continuazione.", modeName = AppMode.CINGLANT.name, partType = MessagePart.BODY.name, language = "IT"),

            // ── Formules de fin globales (italien) ────────────────────────
            ReplyEntity(text = "Arrivederci.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, language = "IT"),
            ReplyEntity(text = "La preghiamo di non richiamare più.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, isEnabled = false, language = "IT"),
            ReplyEntity(text = "Dimentichi questo numero.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, isEnabled = false, language = "IT"),
        )

        private val DEFAULT_REPLIES_EN = listOf(
            // ── Salutations globales (anglais) ────────────────────────────
            ReplyEntity(text = "Hello.", modeName = MODE_GLOBAL, partType = MessagePart.SALUTATION.name, language = "EN"),
            ReplyEntity(text = "Hello and welcome.", modeName = MODE_GLOBAL, partType = MessagePart.SALUTATION.name, isEnabled = false, language = "EN"),
            ReplyEntity(text = "Hello. This line is protected.", modeName = MODE_GLOBAL, partType = MessagePart.SALUTATION.name, isEnabled = false, language = "EN"),

            // ── Corps — Poli (anglais, alternative composable) ─────────────
            ReplyEntity(text = "This line does not accept commercial solicitations.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, isEnabled = false, language = "EN"),
            ReplyEntity(text = "Please remove this number from your lists.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, isEnabled = false, language = "EN"),

            // ── Corps — Sarcastique (anglais, alternative composable) ──────
            ReplyEntity(text = "Congratulations, you've reached the most sarcastic voicemail in the country.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false, language = "EN"),
            ReplyEntity(text = "This line is allergic to telemarketing.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false, language = "EN"),

            // ── Corps — Troll (anglais, alternative composable) ────────────
            ReplyEntity(text = "Please hold, your call is very important to us.", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, isEnabled = false, language = "EN"),
            ReplyEntity(text = "One moment please, connecting you now…", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, isEnabled = false, language = "EN"),

            // ── Modeles de base complets (anglais) ──────────────────────────
            ReplyEntity(text = "Thank you for your message and for your interest in me.\n\nHaving read your offer, I don't wish to pursue it for now.\n\nThank you for your understanding, and best wishes going forward.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, language = "EN"),
            ReplyEntity(text = "Thank you for reaching out.\n\nAfter reviewing your message, your offer doesn't match my current needs. I therefore don't wish to continue this exchange.\n\nThank you for your effort, and have a good day.\n\nKind regards.", modeName = AppMode.PRO.name, partType = MessagePart.BODY.name, language = "EN"),
            ReplyEntity(text = "Thank you for thinking of me for this offer.\n\nEven though it sounds interesting, it's not something that suits me right now, so I'll pass.\n\nI wish you every success with your endeavours anyway.", modeName = AppMode.AMICAL.name, partType = MessagePart.BODY.name, language = "EN"),
            ReplyEntity(text = "Thank you for your message.\n\nI'm not interested in this offer and don't wish to be contacted again about it.\n\nAll the best.", modeName = AppMode.DIRECT.name, partType = MessagePart.BODY.name, language = "EN"),
            ReplyEntity(text = "Thank you for your offer.\n\nAfter an intensive strategy meeting with myself, a coffee and my calendar, we concluded this isn't the right time.\n\nSo I'll decline this offer.\n\nHave an excellent day.", modeName = AppMode.HUMOUR.name, partType = MessagePart.BODY.name, language = "EN"),
            ReplyEntity(text = "Thank you for this apparently exceptional opportunity.\n\nUnfortunately, after a thorough analysis lasting about three seconds, I've decided not to pursue your offer.\n\nI wish you good luck finding more enthusiastic prospects nonetheless.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, language = "EN"),
            ReplyEntity(text = "Your message has been received and forwarded to my selection committee.\n\nAfter several heated debates, two votes against, one abstention and the chair's non-vote, a decision has been reached: your offer will not be accepted.\n\nThank you for taking part, and best wishes going forward.", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, language = "EN"),
            ReplyEntity(text = "Your message has been analysed by the system.\n\nResult: offer detected. Estimated interest level: 0.7%.\n\nAction taken: polite refusal.\n\nThank you for your understanding.\n\nEnd of transmission.", modeName = AppMode.ROBOT.name, partType = MessagePart.BODY.name, language = "EN"),
            ReplyEntity(text = "Message received.\n\nI'm not interested in this offer.\n\nNo further action will be taken.\n\nKind regards.", modeName = AppMode.FROID.name, partType = MessagePart.BODY.name, language = "EN"),
            ReplyEntity(text = "Thank you for your message.\n\nLike most unsolicited offers I receive, yours holds no interest for me whatsoever.\n\nI'd therefore suggest we both stop wasting time by continuing this exchange.\n\nAll the best.", modeName = AppMode.CINGLANT.name, partType = MessagePart.BODY.name, language = "EN"),

            // ── Formules de fin globales (anglais) ────────────────────────
            ReplyEntity(text = "Goodbye.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, language = "EN"),
            ReplyEntity(text = "Please don't call again.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, isEnabled = false, language = "EN"),
            ReplyEntity(text = "Please forget this number.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, isEnabled = false, language = "EN"),
        )

        private val DEFAULT_REPLIES_ES = listOf(
            // ── Salutations globales (espagnol) ───────────────────────────
            ReplyEntity(text = "Buenos días.", modeName = MODE_GLOBAL, partType = MessagePart.SALUTATION.name, language = "ES"),
            ReplyEntity(text = "Buenos días y bienvenido.", modeName = MODE_GLOBAL, partType = MessagePart.SALUTATION.name, isEnabled = false, language = "ES"),
            ReplyEntity(text = "Buenos días. Esta línea está protegida.", modeName = MODE_GLOBAL, partType = MessagePart.SALUTATION.name, isEnabled = false, language = "ES"),

            // ── Corps — Poli (espagnol, alternative composable) ────────────
            ReplyEntity(text = "Este número no desea recibir llamadas publicitarias.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, isEnabled = false, language = "ES"),
            ReplyEntity(text = "Por favor, elimine este número de sus listas.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, isEnabled = false, language = "ES"),

            // ── Corps — Sarcastique (espagnol, alternative composable) ─────
            ReplyEntity(text = "Por favor, cuelgue antes de que esto se vuelva incómodo.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false, language = "ES"),
            ReplyEntity(text = "Esta línea es alérgica a las llamadas publicitarias.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false, language = "ES"),

            // ── Corps — Troll (espagnol, alternative composable) ───────────
            ReplyEntity(text = "Espere por favor, su llamada está siendo transferida…", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, isEnabled = false, language = "ES"),
            ReplyEntity(text = "Un momento por favor, le estamos conectando…", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, isEnabled = false, language = "ES"),

            // ── Modeles de base complets (espagnol) ─────────────────────────
            ReplyEntity(text = "Gracias por su mensaje y por el interés que me ha mostrado.\n\nTras leer su propuesta, no deseo darle seguimiento por el momento.\n\nLe agradezco su comprensión y le deseo una excelente continuación.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, language = "ES"),
            ReplyEntity(text = "Gracias por su contacto.\n\nTras examinar su mensaje, su propuesta no corresponde a mis necesidades actuales. Por lo tanto, no deseo continuar este intercambio.\n\nLe agradezco su gestión y le deseo un buen día.\n\nAtentamente.", modeName = AppMode.PRO.name, partType = MessagePart.BODY.name, language = "ES"),
            ReplyEntity(text = "Gracias por haber pensado en mí para esta propuesta.\n\nAunque parece interesante, no es algo que me convenga actualmente, así que voy a dejarlo pasar.\n\nLe deseo de todos modos mucho éxito en sus gestiones.", modeName = AppMode.AMICAL.name, partType = MessagePart.BODY.name, language = "ES"),
            ReplyEntity(text = "Gracias por su mensaje.\n\nNo estoy interesado en esta propuesta y no deseo que se me vuelva a contactar al respecto.\n\nBuena continuación.", modeName = AppMode.DIRECT.name, partType = MessagePart.BODY.name, language = "ES"),
            ReplyEntity(text = "Gracias por su propuesta.\n\nTras una intensa reunión estratégica conmigo mismo, un café y mi agenda, hemos llegado a la conclusión de que no es el momento adecuado.\n\nPor lo tanto, voy a rechazar esta oferta.\n\nQue tenga un excelente día.", modeName = AppMode.HUMOUR.name, partType = MessagePart.BODY.name, language = "ES"),
            ReplyEntity(text = "Gracias por esta oportunidad manifiestamente excepcional.\n\nDesgraciadamente, tras un análisis exhaustivo de unos tres segundos, he decidido no dar seguimiento a su propuesta.\n\nLe deseo no obstante buena suerte en su búsqueda de clientes más entusiastas.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, language = "ES"),
            ReplyEntity(text = "Su mensaje ha sido recibido y transmitido a mi comité de selección.\n\nTras varios debates animados, dos votos en contra, un voto en blanco y la abstención del presidente, se ha tomado la decisión: su propuesta no será aceptada.\n\nLe agradecemos su participación y le deseamos una excelente continuación.", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, language = "ES"),
            ReplyEntity(text = "Su mensaje ha sido analizado por el sistema.\n\nResultado: propuesta detectada. Nivel de interés estimado: 0,7 %.\n\nAcción ejecutada: rechazo cortés.\n\nGracias por su comprensión.\n\nFin de la transmisión.", modeName = AppMode.ROBOT.name, partType = MessagePart.BODY.name, language = "ES"),
            ReplyEntity(text = "Mensaje recibido.\n\nNo estoy interesado en esta propuesta.\n\nNo se dará ninguna respuesta adicional.\n\nAtentamente.", modeName = AppMode.FROID.name, partType = MessagePart.BODY.name, language = "ES"),
            ReplyEntity(text = "Gracias por su mensaje.\n\nComo la mayoría de las propuestas no solicitadas que recibo, la suya no presenta ningún interés para mí.\n\nPor lo tanto, le invito a no perder más su tiempo ni el mío continuando este intercambio.\n\nBuena continuación.", modeName = AppMode.CINGLANT.name, partType = MessagePart.BODY.name, language = "ES"),

            // ── Formules de fin globales (espagnol) ───────────────────────
            ReplyEntity(text = "Adiós.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, language = "ES"),
            ReplyEntity(text = "Por favor, no vuelva a llamar.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, isEnabled = false, language = "ES"),
            ReplyEntity(text = "Por favor, olvide este número.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, isEnabled = false, language = "ES"),
        )
    }
}
