package com.degage.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.degage.database.AppDatabase
import com.degage.database.entities.BlockedCallEntity
import com.degage.database.entities.ReplyEntity
import com.degage.modes.AppMode
import com.degage.prefs.AppPreferences
import com.degage.spam.SpamSyncManager
import com.degage.tts.TtsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)
    private val prefs = AppPreferences(app)
    private val ttsManager = TtsManager(app)

    fun previewMode(mode: AppMode) = viewModelScope.launch {
        val phrase = when (mode) {
            AppMode.POLI -> "Cette ligne n'accepte pas les sollicitations commerciales."
            AppMode.ADMINISTRATIF -> "Votre appel a été classé comme démarchage non sollicité."
            AppMode.SARCASTIQUE -> "Félicitations, vous avez atteint la boîte vocale la plus sarcastique de France."
            AppMode.TROLL -> "Merci de patienter, votre appel est très important pour nous."
        }
        ttsManager.speak(phrase)
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
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
        // Sync depuis les sources réseau
        SpamSyncManager.syncAll(getApplication())
        prefs.setLastSpamSync(System.currentTimeMillis())
        _isSyncing.value = false
    }

    fun ensureBundledListLoaded() = viewModelScope.launch {
        if (!prefs.bundledLoaded.first()) {
            SpamSyncManager.loadBundledList(getApplication())
            prefs.setBundledLoaded()
        }
    }

    // ── Message builder : flows par partie ──────────────────────────────
    fun getSalutations(): Flow<List<ReplyEntity>> =
        db.replyDao().getGlobalByPart(com.degage.replies.MessagePart.SALUTATION.name)

    fun getBodiesForMode(mode: AppMode): Flow<List<ReplyEntity>> =
        db.replyDao().getBodyByMode(mode.name)

    fun getEndings(): Flow<List<ReplyEntity>> =
        db.replyDao().getGlobalByPart(com.degage.replies.MessagePart.ENDING.name)

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
        val allInSection = db.replyDao().getAllBySection(reply.modeName, reply.partType)
        allInSection.forEach { item ->
            db.replyDao().update(item.copy(isEnabled = item.id == reply.id))
        }
    }

    fun addCustomReply(text: String, mode: AppMode) = viewModelScope.launch {
        db.replyDao().insert(ReplyEntity(text = text, modeName = mode.name, isCustom = true))
    }

    fun addPartItem(text: String, part: com.degage.replies.MessagePart, mode: AppMode) = viewModelScope.launch {
        val modeName = when (part) {
            com.degage.replies.MessagePart.BODY -> mode.name
            else -> com.degage.replies.MODE_GLOBAL
        }
        db.replyDao().insert(ReplyEntity(text = text, modeName = modeName, partType = part.name, isCustom = true))
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
}
