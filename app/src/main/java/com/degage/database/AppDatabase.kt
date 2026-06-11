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
    version = 8,
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

        private fun insertReply(database: SupportSQLiteDatabase, reply: ReplyEntity) {
            val values = ContentValues().apply {
                put("text", reply.text)
                put("modeName", reply.modeName)
                put("partType", reply.partType)
                put("isEnabled", if (reply.isEnabled) 1 else 0)
                put("isCustom", if (reply.isCustom) 1 else 0)
                put("language", reply.language)
            }
            database.insert("replies", SQLiteDatabase.CONFLICT_REPLACE, values)
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "degage.db")
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).replyDao().let { dao ->
                                    (DEFAULT_REPLIES + DEFAULT_REPLIES_DE + DEFAULT_REPLIES_IT + DEFAULT_REPLIES_EN).forEach { dao.insert(it) }
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

            // ── Corps — Poli ──────────────────────────────────────────────
            ReplyEntity(text = "Cette ligne refuse les sollicitations commerciales.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name),
            ReplyEntity(text = "Merci mais cette ligne ne souhaite pas être démarchée.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, isEnabled = false),
            ReplyEntity(text = "Merci de retirer ce numéro de vos listes.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, isEnabled = false),

            // ── Corps — Administratif ─────────────────────────────────────
            ReplyEntity(text = "Votre appel a été identifié comme démarchage.", modeName = AppMode.ADMINISTRATIF.name, partType = MessagePart.BODY.name),
            ReplyEntity(text = "Cette ligne applique une politique anti-sollicitation.", modeName = AppMode.ADMINISTRATIF.name, partType = MessagePart.BODY.name, isEnabled = false),
            ReplyEntity(text = "Le démarchage téléphonique n'est pas accepté sur cette ligne.", modeName = AppMode.ADMINISTRATIF.name, partType = MessagePart.BODY.name, isEnabled = false),

            // ── Corps — Sarcastique ───────────────────────────────────────
            ReplyEntity(text = "Merci de raccrocher avant que ça devienne gênant.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name),
            ReplyEntity(text = "Cette ligne pratique activement le rejet du démarchage.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false),
            ReplyEntity(text = "Cette ligne soutient la disparition du démarchage téléphonique.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false),
            ReplyEntity(text = "Cette ligne est allergique au démarchage.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false),

            // ── Corps — Troll ─────────────────────────────────────────────
            ReplyEntity(text = "Veuillez patienter…", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name),
            ReplyEntity(text = "Transfert vers le service concerné…", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, isEnabled = false),

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
            ReplyEntity(text = "Diese Nummer wünscht keine Werbeanrufe.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, language = "DE"),
            ReplyEntity(text = "Bitte entfernen Sie diese Nummer von Ihrer Liste.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, isEnabled = false, language = "DE"),

            // ── Corps — Administratif (allemand) ──────────────────────────
            ReplyEntity(text = "Dieser Anruf wurde als Werbung eingestuft und automatisch abgelehnt.", modeName = AppMode.ADMINISTRATIF.name, partType = MessagePart.BODY.name, language = "DE"),
            ReplyEntity(text = "Werbeanrufe sind auf dieser Leitung nicht erwünscht.", modeName = AppMode.ADMINISTRATIF.name, partType = MessagePart.BODY.name, isEnabled = false, language = "DE"),

            // ── Corps — Sarcastique (allemand) ────────────────────────────
            ReplyEntity(text = "Bitte legen Sie auf, bevor es unangenehm wird.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, language = "DE"),
            ReplyEntity(text = "Diese Leitung ist allergisch gegen Werbeanrufe.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false, language = "DE"),

            // ── Corps — Troll (allemand) ──────────────────────────────────
            ReplyEntity(text = "Bitte warten Sie, Ihr Anruf wird weitergeleitet…", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, language = "DE"),
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
            ReplyEntity(text = "Questo numero non desidera ricevere chiamate pubblicitarie.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, language = "IT"),
            ReplyEntity(text = "La preghiamo di rimuovere questo numero dalle vostre liste.", modeName = AppMode.POLI.name, partType = MessagePart.BODY.name, isEnabled = false, language = "IT"),

            // ── Corps — Administratif (italien) ───────────────────────────
            ReplyEntity(text = "Questa chiamata è stata classificata come pubblicità e rifiutata automaticamente.", modeName = AppMode.ADMINISTRATIF.name, partType = MessagePart.BODY.name, language = "IT"),
            ReplyEntity(text = "Le chiamate commerciali non sono gradite su questa linea.", modeName = AppMode.ADMINISTRATIF.name, partType = MessagePart.BODY.name, isEnabled = false, language = "IT"),

            // ── Corps — Sarcastique (italien) ─────────────────────────────
            ReplyEntity(text = "La preghiamo di riagganciare prima che diventi imbarazzante.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, language = "IT"),
            ReplyEntity(text = "Questa linea è allergica alle chiamate pubblicitarie.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false, language = "IT"),

            // ── Corps — Troll (italien) ────────────────────────────────────
            ReplyEntity(text = "Attenda in linea, la sua chiamata verrà inoltrata…", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, language = "IT"),
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

            // ── Corps — Administratif (anglais) ───────────────────────────
            ReplyEntity(text = "This call has been classified as advertising and automatically declined.", modeName = AppMode.ADMINISTRATIF.name, partType = MessagePart.BODY.name, language = "EN"),
            ReplyEntity(text = "Commercial calls are not accepted on this line.", modeName = AppMode.ADMINISTRATIF.name, partType = MessagePart.BODY.name, isEnabled = false, language = "EN"),

            // ── Corps — Sarcastique (anglais) ─────────────────────────────
            ReplyEntity(text = "Please hang up before this gets awkward.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, language = "EN"),
            ReplyEntity(text = "This line is allergic to telemarketing.", modeName = AppMode.SARCASTIQUE.name, partType = MessagePart.BODY.name, isEnabled = false, language = "EN"),

            // ── Corps — Troll (anglais) ────────────────────────────────────
            ReplyEntity(text = "Please hold, your call is being transferred…", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, language = "EN"),
            ReplyEntity(text = "One moment please, connecting you now…", modeName = AppMode.TROLL.name, partType = MessagePart.BODY.name, isEnabled = false, language = "EN"),

            // ── Formules de fin globales (anglais) ────────────────────────
            ReplyEntity(text = "Goodbye.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, language = "EN"),
            ReplyEntity(text = "Please don't call again.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, isEnabled = false, language = "EN"),
            ReplyEntity(text = "Please forget this number.", modeName = MODE_GLOBAL, partType = MessagePart.ENDING.name, isEnabled = false, language = "EN"),
        )
    }
}
