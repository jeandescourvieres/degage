@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.degage.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.degage.callscreen.RecentCallEntry
import com.degage.callscreen.getRecentIncomingCalls
import com.degage.callscreen.isNumberInContacts
import com.degage.callscreen.normalizeNumber
import com.degage.database.AppDatabase
import com.degage.database.entities.BlockedCallEntity
import com.degage.database.entities.CustomBlockEntity
import com.degage.database.entities.ReplyEntity
import com.degage.database.entities.WhitelistEntry
import com.degage.modes.AppMode
import com.degage.prefs.AppPreferences
import com.degage.spam.SpamSyncManager
import com.degage.tts.HoldMusicPlayer
import com.degage.tts.TtsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)
    private val prefs = AppPreferences(app)
    private val ttsManager = TtsManager(app)
    private val holdMusicPlayer = HoldMusicPlayer()

    // Joue exactement le message actif pour ce mode (modele de base ou compose par
    // l'utilisateur) plutot qu'une phrase figee : l'apercu reste toujours fidele a la realite.
    fun previewMode(mode: AppMode) = viewModelScope.launch {
        val lang = prefs.replyLanguage.first()
        val activeBody = db.replyDao().getEnabledBodyByMode(mode.name, lang).firstOrNull()
        val phrase = activeBody?.text ?: when (lang) {
            "DE" -> "Diese Nummer wünscht keine Werbeanrufe."
            "IT" -> "Questo numero non desidera ricevere chiamate pubblicitarie."
            "EN" -> "This line does not accept commercial solicitations."
            "ES" -> "Este número no desea recibir llamadas publicitarias."
            else -> "Cette ligne refuse les sollicitations commerciales."
        }
        ttsManager.setLanguage(lang)
        ttsManager.speak(phrase)
        if (mode == AppMode.TROLL) {
            delay(300L)
            holdMusicPlayer.start()
            delay(20_000L)
            holdMusicPlayer.stop()
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        holdMusicPlayer.stop()
    }

    val isEnabled: StateFlow<Boolean> = prefs.isEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val activeModeName: StateFlow<String> = prefs.activeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppMode.POLI.name)

    val activeMode: StateFlow<AppMode> = activeModeName
        .map { runCatching { AppMode.valueOf(it) }.getOrDefault(AppMode.POLI) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppMode.POLI)

    val totalBlocked: StateFlow<Int> = db.blockedCallDao().getCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allCalls: StateFlow<List<BlockedCallEntity>> = db.blockedCallDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayCount: StateFlow<Int> = allCalls.map { calls ->
        val today = LocalDate.now()
        calls.count { call ->
            Instant.ofEpochMilli(call.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() == today
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Temps économisé : 4.8 min en moyenne par appel bloqué
    val timeSavedMinutes: StateFlow<Int> = totalBlocked.map { count ->
        (count * 4.8).toInt()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val autoReject: StateFlow<Boolean> = prefs.autoReject
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val notifications: StateFlow<Boolean> = prefs.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val blockAfterReply: StateFlow<Boolean> = prefs.blockAfterReply
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val monitorLive: StateFlow<Boolean> = prefs.monitorLive
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val contributeDb: StateFlow<Boolean> = prefs.contributeDb
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val blockHiddenNumbers: StateFlow<Boolean> = prefs.blockHiddenNumbers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val strictMode: StateFlow<Boolean> = prefs.strictMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val country: StateFlow<String> = prefs.country
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "FR")

    // Abonnement reel (toggle dev en attendant le vrai paiement) — utilise uniquement pour
    // l'ecran Premium lui-meme, distinct de l'acces effectif aux fonctions (cf. isPremiumUnlocked).
    val isPremium: StateFlow<Boolean> = prefs.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val TRIAL_DURATION_MS = 30L * 24 * 60 * 60 * 1000

    val trialDaysRemaining: StateFlow<Int> = prefs.firstLaunch.map { first ->
        if (first == 0L) 30
        else ((TRIAL_DURATION_MS - (System.currentTimeMillis() - first)) / (24 * 60 * 60 * 1000))
            .toInt().coerceIn(0, 30)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)

    private val isTrialActive: Flow<Boolean> = trialDaysRemaining.map { it > 0 }

    // Acces effectif aux fonctions Premium : abonnement reel OU essai gratuit (30 jours
    // depuis le premier lancement) encore actif. C'est cette valeur qui doit etre utilisee
    // pour deverrouiller les ecrans, jamais isPremium directement.
    val isPremiumUnlocked: StateFlow<Boolean> = combine(isPremium, isTrialActive) { real, trial -> real || trial }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val replyLanguage: StateFlow<String> = prefs.replyLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "FR")

    val appLanguage: StateFlow<String> = prefs.appLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val homeCountry: StateFlow<String> = prefs.homeCountry
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val welcomeMusic: StateFlow<Boolean> = prefs.welcomeMusic
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Texte integral du message actif par mode, pour la popup "voir le message complet" —
    // meme source que previewMode(), pour eviter tout decalage avec ce qui est reellement joue.
    val modeFullTexts: StateFlow<Map<String, String>> = replyLanguage.flatMapLatest { lang ->
        flow {
            val map = AppMode.values().associate { mode ->
                mode.name to (db.replyDao().getEnabledBodyByMode(mode.name, lang).firstOrNull()?.text ?: "")
            }
            emit(map)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun setWelcomeMusic(value: Boolean) = viewModelScope.launch { prefs.setWelcomeMusic(value) }

    /** Enregistre la date du tout premier lancement, point de départ de l'essai gratuit. */
    fun ensureFirstLaunchRecorded() = viewModelScope.launch {
        prefs.setFirstLaunchIfNeeded()
    }

    /** Détecte le pays de l'utilisateur (SIM, sinon région système) et le fige une seule fois. */
    fun ensureHomeCountryDetected() = viewModelScope.launch {
        if (prefs.homeCountry.first().isBlank()) {
            prefs.setHomeCountry(com.degage.locale.CountryDetector.detectHomeCountry(getApplication()))
        }
    }

    fun setAppLanguage(value: String) = viewModelScope.launch {
        prefs.setAppLanguage(value)
        com.degage.locale.LocaleHelper.applyLanguage(value)
        val replyLang = when (value) {
            "DE" -> "DE"
            "IT" -> "IT"
            "EN" -> "EN"
            "ES" -> "ES"
            "FR" -> "FR"
            else -> when (java.util.Locale.getDefault().language) {
                "de" -> "DE"
                "it" -> "IT"
                "en" -> "EN"
                "es" -> "ES"
                else -> "FR"
            }
        }
        prefs.setReplyLanguage(replyLang)
    }

    val customBlocks: StateFlow<List<CustomBlockEntity>> = db.customBlockDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val spamDbCount: StateFlow<Int> = db.spamDao().getCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lastSpamSync: StateFlow<Long> = prefs.lastSpamSync
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    fun syncSpamList() = viewModelScope.launch {
        if (_isSyncing.value) return@launch
        _isSyncing.value = true
        // Charge la liste bundled une seule fois
        if (!prefs.bundledLoaded.first()) {
            SpamSyncManager.loadBundledList(getApplication())
            prefs.setBundledLoaded()
        }
        // Sync base communautaire Supabase
        SpamSyncManager.syncFromSupabase(getApplication(), prefs.country.first())
        // Purge les numéros non reconfirmés depuis longtemps (recyclage téléphonique)
        SpamSyncManager.purgeStaleEntries(getApplication())
        prefs.setLastSpamSync(System.currentTimeMillis())
        _isSyncing.value = false
    }

    fun ensureBundledListLoaded() = viewModelScope.launch {
        if (!prefs.bundledLoaded.first()) {
            SpamSyncManager.loadBundledList(getApplication())
            prefs.setBundledLoaded()
        }
    }

    /** Sync silencieuse de la base spam au lancement, si la dernière date de plus de 24h. */
    fun autoSyncIfNeeded() = viewModelScope.launch {
        val last = prefs.lastSpamSync.first()
        val dayMs = 24 * 60 * 60 * 1000L
        if (System.currentTimeMillis() - last >= dayMs) {
            syncSpamList()
        }
    }

    // ── Message builder : flows par partie, réactifs à la langue des réponses ──
    fun getSalutations(): Flow<List<ReplyEntity>> =
        replyLanguage.flatMapLatest { lang ->
            db.replyDao().getGlobalByPart(com.degage.replies.MessagePart.SALUTATION.name, lang)
        }

    fun getBodiesForMode(mode: AppMode): Flow<List<ReplyEntity>> =
        replyLanguage.flatMapLatest { lang ->
            db.replyDao().getBodyByMode(mode.name, lang)
        }

    fun getEndings(): Flow<List<ReplyEntity>> =
        replyLanguage.flatMapLatest { lang ->
            db.replyDao().getGlobalByPart(com.degage.replies.MessagePart.ENDING.name, lang)
        }

    // Compat écran Réponses existant
    fun getRepliesForMode(mode: AppMode): Flow<List<ReplyEntity>> =
        db.replyDao().getByMode(mode.name)

    fun toggleEnabled() = viewModelScope.launch {
        prefs.setEnabled(!isEnabled.value)
    }

    fun setMode(mode: AppMode) = viewModelScope.launch {
        prefs.setActiveMode(mode.name)
    }

    fun toggleReply(reply: ReplyEntity) = viewModelScope.launch {
        db.replyDao().update(reply.copy(isEnabled = !reply.isEnabled))
    }

    fun selectReply(reply: ReplyEntity) = viewModelScope.launch {
        val allInSection = db.replyDao().getAllBySection(reply.modeName, reply.partType, reply.language)
        allInSection.forEach { item ->
            db.replyDao().update(item.copy(isEnabled = item.id == reply.id))
        }
    }

    fun addCustomReply(text: String, mode: AppMode) = viewModelScope.launch {
        val lang = prefs.replyLanguage.first()
        db.replyDao().insert(ReplyEntity(text = text, modeName = mode.name, isCustom = true, language = lang))
    }

    fun addPartItem(text: String, part: com.degage.replies.MessagePart, mode: AppMode) = viewModelScope.launch {
        val modeName = when (part) {
            com.degage.replies.MessagePart.BODY -> mode.name
            else -> com.degage.replies.MODE_GLOBAL
        }
        val lang = prefs.replyLanguage.first()
        db.replyDao().insert(ReplyEntity(text = text, modeName = modeName, partType = part.name, isCustom = true, language = lang))
    }

    fun deleteReply(reply: ReplyEntity) = viewModelScope.launch {
        if (reply.isCustom) db.replyDao().delete(reply)
    }

    fun deleteHistoryEntry(id: Long) = viewModelScope.launch {
        db.blockedCallDao().deleteById(id)
    }

    fun setAutoReject(v: Boolean) = viewModelScope.launch { prefs.setAutoReject(v) }
    fun setNotifications(v: Boolean) = viewModelScope.launch { prefs.setNotifications(v) }
    fun setBlockAfterReply(v: Boolean) = viewModelScope.launch { prefs.setBlockAfterReply(v) }
    fun setMonitorLive(v: Boolean) = viewModelScope.launch { prefs.setMonitorLive(v) }
    fun setContributeDb(v: Boolean) = viewModelScope.launch { prefs.setContributeDb(v) }
    fun setBlockHiddenNumbers(v: Boolean) = viewModelScope.launch { prefs.setBlockHiddenNumbers(v) }
    fun setStrictMode(v: Boolean) = viewModelScope.launch { prefs.setStrictMode(v) }
    fun setCountry(v: String) = viewModelScope.launch { prefs.setCountry(v) }
    fun setReplyLanguage(v: String) = viewModelScope.launch { prefs.setReplyLanguage(v) }

    /** Mode développeur : bascule manuelle de l'état Premium, en attendant l'intégration Google Play Billing. */
    fun setPremium(v: Boolean) = viewModelScope.launch { prefs.setPremium(v) }

    fun addCustomBlock(value: String, isPrefix: Boolean) = viewModelScope.launch {
        val cleaned = value.trim().filter { it.isDigit() || it == '+' }
        if (cleaned.isBlank()) return@launch
        db.customBlockDao().insert(CustomBlockEntity(value = cleaned, isPrefix = isPrefix))
    }

    fun deleteCustomBlock(entry: CustomBlockEntity) = viewModelScope.launch {
        db.customBlockDao().delete(entry)
    }

    /** Marque un appel comme légitime : retire le numéro de la base spam et des
     *  règles personnalisées, et l'ajoute à la liste blanche locale. */
    fun markNotSpam(call: BlockedCallEntity) = viewModelScope.launch {
        val normalized = call.phoneNumber.normalizeNumber()
        if (normalized.isBlank()) return@launch
        db.spamDao().deleteByNumber(normalized)
        db.customBlockDao().deleteExactByValue(normalized)
        db.whitelistDao().insert(WhitelistEntry(number = normalized))
    }

    private val _recentUnblockedCalls = MutableStateFlow<List<RecentCallEntry>>(emptyList())
    val recentUnblockedCalls: StateFlow<List<RecentCallEntry>> = _recentUnblockedCalls.asStateFlow()

    /** Liste les derniers appels reçus qui n'ont pas été bloqués par Tu dégages,
     *  pour permettre de les ajouter en un tap aux numéros bloqués manuellement. */
    fun loadRecentUnblockedCalls() = viewModelScope.launch {
        val context = getApplication<Application>()
        val alreadyBlocked = allCalls.value.map { it.phoneNumber.normalizeNumber() }.toSet()
        val entries = context.getRecentIncomingCalls(20).filter { entry ->
            entry.normalized !in alreadyBlocked &&
                !db.customBlockDao().isExactBlocked(entry.normalized) &&
                !db.whitelistDao().isWhitelisted(entry.normalized) &&
                !context.isNumberInContacts(entry.number)
        }
        _recentUnblockedCalls.value = entries.take(5)
    }

    /** Ajoute le numéro d'un appel récent non bloqué à la liste personnelle de blocage. */
    fun blockRecentCall(entry: RecentCallEntry) = viewModelScope.launch {
        db.customBlockDao().insert(CustomBlockEntity(value = entry.normalized, isPrefix = false))
        _recentUnblockedCalls.value = _recentUnblockedCalls.value.filter { it != entry }
    }
}
