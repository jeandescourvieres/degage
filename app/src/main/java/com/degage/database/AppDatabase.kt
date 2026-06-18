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
    version = 10,
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
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
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

            // ── Modeles de base complets (standalone : salutation/fin deja incluses) ──
            ReplyEntity(
                text = "Bonjour,\n\nMerci pour votre message et pour l'intérêt que vous me portez.\n\nAprès lecture de votre proposition, je ne souhaite pas y donner suite pour le moment.\n\nJe vous remercie de votre compréhension et vous souhaite une excellente continuation.",
                modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, isStandalone = true
            ),
            ReplyEntity(
                text = "Bonjour,\n\nMerci pour votre prise de contact.\n\nAprès examen de votre message, votre proposition ne correspond pas à mes besoins actuels. Je ne souhaite donc pas poursuivre cet échange.\n\nJe vous remercie pour votre démarche et vous souhaite une bonne journée.\n\nCordialement.",
                modeName = AppMode.PRO.name, partType = MessagePart.BODY.name, isStandalone = true
            ),
            ReplyEntity(
                text = "Bonjour,\n\nMerci d'avoir pensé à moi pour cette proposition.\n\nMême si elle semble intéressante, ce n'est pas quelque chose qui me convient actuellement. Je vais donc passer mon tour.\n\nJe vous souhaite malgré tout beaucoup de succès dans vos démarches.",
                modeName = AppMode.AMICAL.name, partType = MessagePart.BODY.name, isStandalone = true
            ),
            ReplyEntity(
                text = "Bonjour,\n\nMerci pour votre message.\n\nJe ne suis pas intéressé par cette proposition et ne souhaite pas être recontacté à ce sujet.\n\nBonne continuation.",
                modeName = AppMode.DIRECT.name, partType = MessagePart.BODY.name, isStandalone = true
            ),
            ReplyEntity(
                text = "Bonjour,\n\nMerci pour votre proposition.\n\nAprès une réunion stratégique intensive avec moi-même, un café et mon agenda, nous sommes arrivés à la conclusion que ce n'était pas le bon moment.\n\nJe vais donc décliner cette offre.\n\nExcellente journée à vous.",
                modeName = AppMode.HUMOUR.name, partType = MessagePart.BODY.name, isStandalone = true
            ),
            ReplyEntity(
                text = "Bonjour,\n\nMerci pour cette opportunité manifestement exceptionnelle.\n\nMalheureusement, après une analyse approfondie d'environ trois secondes, j'ai décidé de ne pas donner suite à votre proposition.\n\nJe vous souhaite néanmoins bonne chance dans votre quête de prospects plus enthousiastes.",
                modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isStandalone = true
            ),
            ReplyEntity(
                text = "Bonjour,\n\nVotre message a bien été reçu et transmis à mon comité de sélection.\n\nAprès plusieurs débats animés, deux votes contre, un vote blanc et l'abstention du président, la décision est tombée : votre proposition ne sera pas retenue.\n\nNous vous remercions pour votre participation et vous souhaitons une excellente continuation.",
                modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, isStandalone = true
            ),
            ReplyEntity(
                text = "Bonjour.\n\nVotre message a été analysé par le système.\n\nRésultat : proposition détectée. Niveau d'intérêt estimé : 0,7 %.\n\nAction exécutée : refus poli.\n\nMerci de votre compréhension.\n\nFin de transmission.",
                modeName = AppMode.ROBOT.name, partType = MessagePart.BODY.name, isStandalone = true
            ),
            ReplyEntity(
                text = "Bonjour,\n\nMessage reçu.\n\nJe ne suis pas intéressé par cette proposition.\n\nAucune suite ne sera donnée.\n\nCordialement.",
                modeName = AppMode.FROID.name, partType = MessagePart.BODY.name, isStandalone = true
            ),
            ReplyEntity(
                text = "Bonjour,\n\nMerci pour votre message.\n\nComme la majorité des sollicitations non demandées que je reçois, votre proposition ne présente aucun intérêt pour moi.\n\nJe vous invite donc à ne pas perdre davantage votre temps ni le mien en poursuivant cet échange.\n\nBonne continuation.",
                modeName = AppMode.CINGLANT.name, partType = MessagePart.BODY.name, isStandalone = true
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

            // ── Corps — Poli (allemand) ────────────────────────────────────
            ReplyEntity(text = "Diese Leitung nimmt keine kommerziellen Werbeanrufe entgegen.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, language = "DE"),
            ReplyEntity(text = "Bitte entfernen Sie diese Nummer von Ihrer Liste.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, isEnabled = false, language = "DE"),

            // ── Corps — Sarcastique (allemand) ────────────────────────────
            ReplyEntity(text = "Herzlichen Glückwunsch, Sie haben die sarkastischste Mailbox Deutschlands erreicht.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, language = "DE"),
            ReplyEntity(text = "Diese Leitung ist allergisch gegen Werbeanrufe.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false, language = "DE"),

            // ── Corps — Troll (allemand) ──────────────────────────────────
            ReplyEntity(text = "Bitte warten Sie, Ihr Anruf ist uns sehr wichtig.", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, language = "DE"),
            ReplyEntity(text = "Einen Moment bitte, Sie werden verbunden…", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, isEnabled = false, language = "DE"),

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

            // ── Corps — Poli (italien) ─────────────────────────────────────
            ReplyEntity(text = "Questa linea non accetta sollecitazioni commerciali.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, language = "IT"),
            ReplyEntity(text = "La preghiamo di rimuovere questo numero dalle vostre liste.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, isEnabled = false, language = "IT"),

            // ── Corps — Sarcastique (italien) ─────────────────────────────
            ReplyEntity(text = "Congratulazioni, ha raggiunto la segreteria telefonica più sarcastica d'Italia.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, language = "IT"),
            ReplyEntity(text = "Questa linea è allergica alle chiamate pubblicitarie.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false, language = "IT"),

            // ── Corps — Troll (italien) ────────────────────────────────────
            ReplyEntity(text = "Attenda in linea, la sua chiamata è molto importante per noi.", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, language = "IT"),
            ReplyEntity(text = "Un momento, la stiamo mettendo in contatto…", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, isEnabled = false, language = "IT"),

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

            // ── Corps — Poli (anglais) ─────────────────────────────────────
            ReplyEntity(text = "This line does not accept commercial solicitations.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, language = "EN"),
            ReplyEntity(text = "Please remove this number from your lists.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, isEnabled = false, language = "EN"),

            // ── Corps — Sarcastique (anglais) ─────────────────────────────
            ReplyEntity(text = "Congratulations, you've reached the most sarcastic voicemail in the country.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, language = "EN"),
            ReplyEntity(text = "This line is allergic to telemarketing.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false, language = "EN"),

            // ── Corps — Troll (anglais) ────────────────────────────────────
            ReplyEntity(text = "Please hold, your call is very important to us.", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, language = "EN"),
            ReplyEntity(text = "One moment please, connecting you now…", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, isEnabled = false, language = "EN"),

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

            // ── Corps — Poli (espagnol) ────────────────────────────────────
            ReplyEntity(text = "Este número no desea recibir llamadas publicitarias.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, language = "ES"),
            ReplyEntity(text = "Por favor, elimine este número de sus listas.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, isEnabled = false, language = "ES"),

            // ── Corps — Sarcastique (espagnol) ─────────────────────────────
            ReplyEntity(text = "Por favor, cuelgue antes de que esto se vuelva incómodo.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, language = "ES"),
            ReplyEntity(text = "Esta línea es alérgica a las llamadas publicitarias.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false, language = "ES"),

            // ── Corps — Troll (espagnol) ───────────────────────────────────
            ReplyEntity(text = "Espere por favor, su llamada está siendo transferida…", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, language = "ES"),
            ReplyEntity(text = "Un momento por favor, le estamos conectando…", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, isEnabled = false, language = "ES"),

            // ── Formules de fin globales (espagnol) ───────────────────────
            ReplyEntity(text = "Adiós.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, language = "ES"),
            ReplyEntity(text = "Por favor, no vuelva a llamar.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, isEnabled = false, language = "ES"),
            ReplyEntity(text = "Por favor, olvide este número.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, isEnabled = false, language = "ES"),
        )
    }
}
