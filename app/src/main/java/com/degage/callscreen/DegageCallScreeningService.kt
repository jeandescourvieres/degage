package com.degage.callscreen

import android.content.Context
import android.media.AudioManager
import android.telecom.Call
import android.telecom.CallScreeningService
import com.degage.database.AppDatabase
import com.degage.database.entities.BlockedCallEntity
import com.degage.database.entities.CallAttemptEntity
import com.degage.database.entities.SpamEntry
import com.degage.modes.AppMode
import com.degage.notifications.NotificationHelper
import com.degage.spam.SupabaseClient
import com.degage.prefs.AppPreferences
import com.degage.replies.MessagePart
import com.degage.tts.HoldMusicPlayer
import com.degage.tts.TtsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DegageCallScreeningService : CallScreeningService() {

    companion object {
        private const val BURST_WINDOW_MS = 30 * 60 * 1000L // 30 minutes
        private const val BURST_THRESHOLD = 3 // appels avant blocage automatique
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var ttsManager: TtsManager
    private lateinit var prefs: AppPreferences
    private val holdMusicPlayer = HoldMusicPlayer()

    override fun onCreate() {
        super.onCreate()
        ttsManager = TtsManager(applicationContext)
        prefs = AppPreferences(applicationContext)
    }

    override fun onScreenCall(callDetails: Call.Details) {
        val rawNumber = callDetails.handle?.schemeSpecificPart
        val normalized = rawNumber.normalizeNumber()
        val isUnknown = rawNumber.isUnknownNumber()

        scope.launch {
            val isEnabled = prefs.isEnabled.first()
            if (!isEnabled) {
                respondToCall(callDetails, CallResponse.Builder().build())
                return@launch
            }

            // ── Numéro enregistré dans les contacts → jamais bloqué ──────────
            if (!isUnknown && rawNumber != null && applicationContext.isNumberInContacts(rawNumber)) {
                respondToCall(callDetails, CallResponse.Builder().build())
                return@launch
            }

            val db = AppDatabase.getInstance(applicationContext)

            // ── Numéro marqué « pas un spam » → jamais bloqué ────────────────
            if (normalized.isNotBlank() && db.whitelistDao().isWhitelisted(normalized)) {
                respondToCall(callDetails, CallResponse.Builder().build())
                return@launch
            }

            val contributeDb = prefs.contributeDb.first()
            val country = prefs.country.first()
            val notificationsEnabled = prefs.notifications.first()
            val isSpamPrefix = !isUnknown && normalized.isNotBlank() && rawNumber!!.isSpamNumber(country)

            // ── Appel masqué/inconnu + option activée → rejet immédiat sans TTS ──
            if (isUnknown && prefs.blockHiddenNumbers.first()) {
                silentReject(callDetails)
                db.blockedCallDao().insert(
                    BlockedCallEntity(
                        phoneNumber = rawNumber.displayNumber(),
                        timestamp = System.currentTimeMillis(),
                        modeName = "Auto",
                        replyUsed = "Rejet automatique — numéro masqué"
                    )
                )
                if (notificationsEnabled) {
                    NotificationHelper.notifyBlockedCall(applicationContext, rawNumber.displayNumber(), "numéro masqué")
                }
                return@launch
            }

            // ── Mode strict → rejet de toute la plage des opérateurs VoIP ────
            if (!isUnknown && normalized.isNotBlank() && prefs.strictMode.first() && rawNumber!!.isStrictVoipNumber(country)) {
                silentReject(callDetails)
                db.blockedCallDao().insert(
                    BlockedCallEntity(
                        phoneNumber = rawNumber.displayNumber(),
                        timestamp = System.currentTimeMillis(),
                        modeName = "Auto",
                        replyUsed = "Rejet automatique — mode strict (plage opérateur VoIP)"
                    )
                )
                if (notificationsEnabled) {
                    NotificationHelper.notifyBlockedCall(applicationContext, rawNumber.displayNumber(), "mode strict")
                }
                if (contributeDb) SupabaseClient.reportNumber(normalized.toE164(country))
                return@launch
            }

            // ── Indicatif international à risque (arnaque Wangiri) → rejet immédiat, sans
            // sonnerie ni message : l'arnaque repose sur l'appel manqué qui incite à
            // rappeler, donc le seul fait de ne jamais le montrer comme manqué la neutralise.
            if (!isUnknown && normalized.isNotBlank() && rawNumber!!.isWangiriRiskNumber()) {
                silentReject(callDetails)
                db.blockedCallDao().insert(
                    BlockedCallEntity(
                        phoneNumber = rawNumber.displayNumber(),
                        timestamp = System.currentTimeMillis(),
                        modeName = "Auto",
                        replyUsed = "Rejet automatique — indicatif international à risque (Wangiri)"
                    )
                )
                if (notificationsEnabled) {
                    NotificationHelper.notifyBlockedCall(applicationContext, rawNumber.displayNumber(), "indicatif à risque")
                }
                return@launch
            }

            // ── Numéro correspondant à une règle personnalisée → rejet immédiat ──
            if (!isUnknown && normalized.isNotBlank()) {
                val isCustomExact = db.customBlockDao().isExactBlocked(normalized)
                val isCustomPrefix = db.customBlockDao().getPrefixes().any { normalized.startsWith(it) }
                if (isCustomExact || isCustomPrefix) {
                    silentReject(callDetails)
                    db.blockedCallDao().insert(
                        BlockedCallEntity(
                            phoneNumber = rawNumber.displayNumber(),
                            timestamp = System.currentTimeMillis(),
                            modeName = "Auto",
                            replyUsed = "Rejet automatique — règle personnalisée"
                        )
                    )
                    if (notificationsEnabled) {
                        NotificationHelper.notifyBlockedCall(applicationContext, rawNumber.displayNumber(), "règle personnalisée")
                    }
                    return@launch
                }
            }

            // ── Numéro déjà connu dans la base spam → rejet immédiat sans TTS ──
            if (normalized.isNotBlank() && db.spamDao().isKnownSpam(normalized)) {
                silentReject(callDetails)
                db.spamDao().incrementReport(normalized)
                db.blockedCallDao().insert(
                    BlockedCallEntity(
                        phoneNumber = rawNumber.displayNumber(),
                        timestamp = System.currentTimeMillis(),
                        modeName = "Auto",
                        replyUsed = "Rejet automatique — numéro connu"
                    )
                )
                if (notificationsEnabled) {
                    NotificationHelper.notifyBlockedCall(applicationContext, rawNumber.displayNumber(), "numéro connu")
                }
                if (contributeDb) SupabaseClient.reportNumber(normalized.toE164(country))
                return@launch
            }

            // ── Numéro ni inconnu ni spam → vérifier les rafales d'appels ────
            if (!isUnknown && !isSpamPrefix) {
                if (normalized.isNotBlank()) {
                    val now = System.currentTimeMillis()
                    db.callAttemptDao().deleteOlderThan(now - BURST_WINDOW_MS)
                    db.callAttemptDao().insert(CallAttemptEntity(number = normalized, timestamp = now))
                    val attempts = db.callAttemptDao().countSince(normalized, now - BURST_WINDOW_MS)
                    if (attempts >= BURST_THRESHOLD) {
                        silentReject(callDetails)
                        db.spamDao().insert(SpamEntry(number = normalized, source = "auto_block_burst"))
                        db.callAttemptDao().deleteByNumber(normalized)
                        db.blockedCallDao().insert(
                            BlockedCallEntity(
                                phoneNumber = rawNumber.displayNumber(),
                                timestamp = now,
                                modeName = "Auto",
                                replyUsed = "Rejet automatique — appels répétés en peu de temps"
                            )
                        )
                        if (notificationsEnabled) {
                            NotificationHelper.notifyBlockedCall(applicationContext, rawNumber.displayNumber(), "appels répétés")
                        }
                        if (contributeDb) SupabaseClient.reportNumber(normalized.toE164(country))
                        return@launch
                    }
                }
                respondToCall(callDetails, CallResponse.Builder().build())
                return@launch
            }

            // ── Traitement normal : TTS + rejet ──────────────────────────────
            val modeStr = prefs.activeMode.first()
            val mode = runCatching { AppMode.valueOf(modeStr) }.getOrDefault(AppMode.POLI)
            val replyLanguage = prefs.replyLanguage.first()

            val defaultBody = when (replyLanguage) {
                "DE" -> "Diese Nummer wünscht keine Werbeanrufe."
                "IT" -> "Questo numero non desidera ricevere chiamate pubblicitarie."
                "EN" -> "This line does not accept commercial solicitations."
                "ES" -> "Este número no desea recibir llamadas publicitarias."
                else -> "Cette ligne refuse les sollicitations commerciales."
            }
            val activeBody = db.replyDao().getEnabledBodyByMode(mode.name, replyLanguage).firstOrNull()
            val salutation = db.replyDao().getEnabledGlobalByPart(MessagePart.SALUTATION.name, replyLanguage).firstOrNull()?.text ?: ""
            val body = activeBody?.text ?: defaultBody
            val ending = db.replyDao().getEnabledGlobalByPart(MessagePart.ENDING.name, replyLanguage).firstOrNull()?.text ?: ""
            val fullMessage = listOf(salutation, body, ending)
                .filter { it.isNotBlank() }
                .joinToString("\n\n")

            val rate = prefs.speechRate.first()
            val pitch = prefs.pitch.first()
            val voiceName = prefs.voiceNameFor(replyLanguage).first().ifBlank { null }
            val monitorLive = prefs.monitorLive.first()
            ttsManager.setLanguage(replyLanguage)
            ttsManager.applySettings(rate, pitch, voiceName)

            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (monitorLive) {
                audioManager.isSpeakerphoneOn = true
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                    0
                )
            }

            if (mode == AppMode.TROLL) {
                ttsManager.speak(fullMessage)
                holdMusicPlayer.start()
                delay(15_000L)
                holdMusicPlayer.stop()
            } else {
                ttsManager.speak(fullMessage)
                delay(5_000L)
            }

            if (monitorLive) {
                audioManager.isSpeakerphoneOn = false
            }

            silentReject(callDetails)

            db.blockedCallDao().insert(
                BlockedCallEntity(
                    phoneNumber = rawNumber.displayNumber(),
                    timestamp = System.currentTimeMillis(),
                    modeName = mode.label,
                    replyUsed = fullMessage
                )
            )

            if (notificationsEnabled) {
                NotificationHelper.notifyBlockedCall(applicationContext, rawNumber.displayNumber(), "mode ${mode.label}")
            }

            // ── Mémoriser ce numéro pour rejet immédiat la prochaine fois ────
            if (normalized.isNotBlank()) {
                db.spamDao().insert(SpamEntry(number = normalized, source = "auto_block"))
                if (contributeDb) SupabaseClient.reportNumber(normalized.toE164(country))
            }
        }
    }

    private fun silentReject(callDetails: Call.Details) {
        respondToCall(
            callDetails,
            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSilenceCall(true)
                .build()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
        holdMusicPlayer.stop()
    }
}
