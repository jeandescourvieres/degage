package com.degage.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

class TtsManager(context: Context) {

    private var tts: TextToSpeech? = null

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.FRENCH
                _isReady.value = true
            }
        }
    }

    // Applique les réglages utilisateur avant chaque lecture
    fun applySettings(rate: Float, pitch: Float, voiceName: String?) {
        tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
        if (!voiceName.isNullOrBlank()) {
            tts?.voices?.find { it.name == voiceName }?.let { tts?.voice = it }
        }
    }

    // Bascule la langue de synthèse vocale ("FR", "DE", "IT" ou "EN")
    fun setLanguage(languageCode: String) {
        val locale = when (languageCode) {
            "DE" -> Locale.GERMAN
            "IT" -> Locale.ITALIAN
            "EN" -> Locale.UK
            else -> Locale.FRENCH
        }
        tts?.language = locale
        tts?.voice?.let { voice ->
            if (voice.locale.language != locale.language) {
                tts?.voices
                    ?.firstOrNull { it.locale.language == locale.language }
                    ?.let { tts?.voice = it }
            }
        }
    }

    // Retourne les voix installées pour la langue donnée ("FR", "DE", "IT" ou "EN"), triées par nom
    fun getAvailableVoices(languageCode: String = "FR"): List<Voice> {
        val isoLanguage = when (languageCode) {
            "DE" -> "de"
            "IT" -> "it"
            "EN" -> "en"
            else -> "fr"
        }
        return tts?.voices
            ?.filter { voice ->
                voice.locale.language == isoLanguage &&
                voice.features?.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) != true
            }
            ?.sortedBy { it.name }
            ?.toList() ?: emptyList()
    }

    suspend fun speak(text: String): Boolean = suspendCancellableCoroutine { cont ->
        if (!_isReady.value) { cont.resume(false); return@suspendCancellableCoroutine }
        val utteranceId = "degage_${System.currentTimeMillis()}"
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onDone(id: String?) { if (!cont.isCompleted) cont.resume(true) }
            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) { if (!cont.isCompleted) cont.resume(false) }
        })
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        cont.invokeOnCancellation { tts?.stop() }
    }

    fun stop() = tts?.stop()

    fun shutdown() {
        tts?.shutdown()
        tts = null
    }
}
