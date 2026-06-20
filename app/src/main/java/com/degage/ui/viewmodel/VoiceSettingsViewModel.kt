@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.degage.ui.viewmodel

import android.app.Application
import android.speech.tts.Voice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.degage.prefs.AppPreferences
import com.degage.tts.TtsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VoiceSettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = AppPreferences(app)
    private val ttsManager = TtsManager(app)

    val replyLanguage: StateFlow<String> = prefs.replyLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "FR")

    val voices: StateFlow<List<Voice>> = combine(replyLanguage, ttsManager.isReady) { lang, ready ->
        if (ready) ttsManager.getAvailableVoices(lang) else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val speechRate: StateFlow<Float> = prefs.speechRate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val pitch: StateFlow<Float> = prefs.pitch
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val selectedVoiceName: StateFlow<String> = replyLanguage.flatMapLatest { lang ->
        prefs.voiceNameFor(lang)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _previewingVoiceName = MutableStateFlow<String?>(null)
    val previewingVoiceName: StateFlow<String?> = _previewingVoiceName.asStateFlow()

    fun previewVoice(voiceName: String, rate: Float, pitch: Float) = viewModelScope.launch {
        val lang = replyLanguage.value
        val phrase = when (lang) {
            "DE" -> "Hallo. Diese Leitung nimmt keine kommerziellen Werbeanrufe entgegen."
            "IT" -> "Salve. Questa linea non accetta sollecitazioni commerciali."
            "EN" -> "Hello. This line does not accept commercial solicitations."
            "ES" -> "Hola. Esta línea no acepta solicitudes comerciales."
            else -> "Bonjour, vous êtes bien en communication avec un assistant IA. Votre appel a été identifié comme indésirable. Au revoir."
        }
        ttsManager.setLanguage(lang)
        ttsManager.applySettings(rate, pitch, voiceName)
        _previewingVoiceName.value = voiceName
        try {
            ttsManager.speak(phrase)
        } finally {
            if (_previewingVoiceName.value == voiceName) _previewingVoiceName.value = null
        }
    }

    fun setRate(value: Float) = viewModelScope.launch { prefs.setSpeechRate(value) }
    fun setPitch(value: Float) = viewModelScope.launch { prefs.setPitch(value) }
    fun setVoice(name: String) = viewModelScope.launch { prefs.setVoiceNameFor(replyLanguage.value, name) }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
