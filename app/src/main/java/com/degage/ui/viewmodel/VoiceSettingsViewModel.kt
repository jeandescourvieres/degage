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

    private val _voices = MutableStateFlow<List<Voice>>(emptyList())
    val voices: StateFlow<List<Voice>> = _voices.asStateFlow()

    val speechRate: StateFlow<Float> = prefs.speechRate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val pitch: StateFlow<Float> = prefs.pitch
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val selectedVoiceName: StateFlow<String> = prefs.voiceName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    init {
        // Attendre que TTS soit prêt pour récupérer les voix disponibles
        viewModelScope.launch {
            ttsManager.isReady.filter { it }.first()
            _voices.value = ttsManager.getAvailableVoices()
        }
    }

    fun previewVoice(voiceName: String, rate: Float, pitch: Float) = viewModelScope.launch {
        ttsManager.applySettings(rate, pitch, voiceName)
        ttsManager.speak("Bonjour. Cette ligne est allergique au démarchage. À pas bientôt.")
    }

    fun setRate(value: Float) = viewModelScope.launch { prefs.setSpeechRate(value) }
    fun setPitch(value: Float) = viewModelScope.launch { prefs.setPitch(value) }
    fun setVoice(name: String) = viewModelScope.launch { prefs.setVoiceName(name) }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
