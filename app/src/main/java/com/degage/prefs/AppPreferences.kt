package com.degage.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.degage.modes.AppMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("degage_prefs")

class AppPreferences(private val context: Context) {

    companion object {
        val KEY_ENABLED = booleanPreferencesKey("is_enabled")
        val KEY_MODE = stringPreferencesKey("active_mode")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_AUTO_REJECT = booleanPreferencesKey("auto_reject")
        val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications")
        val KEY_BLOCK_AFTER_REPLY = booleanPreferencesKey("block_after_reply")
        // Voix TTS
        val KEY_SPEECH_RATE = floatPreferencesKey("speech_rate")
        val KEY_PITCH = floatPreferencesKey("pitch")
        val KEY_VOICE_NAME = stringPreferencesKey("voice_name")
        val KEY_WELCOME_SHOWN = booleanPreferencesKey("welcome_shown")
        val KEY_LAST_SPAM_SYNC = longPreferencesKey("last_spam_sync")
        val KEY_BUNDLED_LOADED = booleanPreferencesKey("bundled_spam_loaded")
        val KEY_MONITOR_LIVE = booleanPreferencesKey("monitor_live")
        val KEY_CONTRIBUTE_DB = booleanPreferencesKey("contribute_db")
        val KEY_BLOCK_HIDDEN = booleanPreferencesKey("block_hidden_numbers")
        val KEY_COUNTRY = stringPreferencesKey("country")
        val KEY_IS_PREMIUM = booleanPreferencesKey("is_premium")
        val KEY_REPLY_LANGUAGE = stringPreferencesKey("reply_language")
        val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
    }

    val isEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_ENABLED] ?: true }
    val activeMode: Flow<String> = context.dataStore.data.map { it[KEY_MODE] ?: AppMode.POLI.name }
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[KEY_ONBOARDING_DONE] ?: false }
    val autoReject: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_REJECT] ?: true }
    val notifications: Flow<Boolean> = context.dataStore.data.map { it[KEY_NOTIFICATIONS] ?: true }
    val blockAfterReply: Flow<Boolean> = context.dataStore.data.map { it[KEY_BLOCK_AFTER_REPLY] ?: true }
    val speechRate: Flow<Float> = context.dataStore.data.map { it[KEY_SPEECH_RATE] ?: 1.0f }
    val pitch: Flow<Float> = context.dataStore.data.map { it[KEY_PITCH] ?: 1.0f }
    // Voix choisie pour une langue de messages vocaux donnée (FR/DE/IT/EN).
    // Pour FR, retombe sur l'ancienne clé unique KEY_VOICE_NAME (migration).
    fun voiceNameFor(lang: String): Flow<String> {
        val key = stringPreferencesKey("voice_name_$lang")
        return context.dataStore.data.map { prefs ->
            prefs[key] ?: if (lang == "FR") prefs[KEY_VOICE_NAME] ?: "" else ""
        }
    }
    val welcomeShown: Flow<Boolean> = context.dataStore.data.map { it[KEY_WELCOME_SHOWN] ?: false }
    val lastSpamSync: Flow<Long> = context.dataStore.data.map { it[KEY_LAST_SPAM_SYNC] ?: 0L }
    val bundledLoaded: Flow<Boolean> = context.dataStore.data.map { it[KEY_BUNDLED_LOADED] ?: false }
    val monitorLive: Flow<Boolean> = context.dataStore.data.map { it[KEY_MONITOR_LIVE] ?: false }
    val contributeDb: Flow<Boolean> = context.dataStore.data.map { it[KEY_CONTRIBUTE_DB] ?: false }
    val blockHiddenNumbers: Flow<Boolean> = context.dataStore.data.map { it[KEY_BLOCK_HIDDEN] ?: false }
    val country: Flow<String> = context.dataStore.data.map { it[KEY_COUNTRY] ?: "FR" }
    val isPremium: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_PREMIUM] ?: false }
    val replyLanguage: Flow<String> = context.dataStore.data.map { it[KEY_REPLY_LANGUAGE] ?: "FR" }
    // "" = suit la langue du système (parmi FR/DE/IT/EN, sinon FR par défaut)
    val appLanguage: Flow<String> = context.dataStore.data.map { it[KEY_APP_LANGUAGE] ?: "" }

    suspend fun setEnabled(value: Boolean) = context.dataStore.edit { it[KEY_ENABLED] = value }
    suspend fun setActiveMode(mode: String) = context.dataStore.edit { it[KEY_MODE] = mode }
    suspend fun setOnboardingDone() = context.dataStore.edit { it[KEY_ONBOARDING_DONE] = true }
    suspend fun setAutoReject(value: Boolean) = context.dataStore.edit { it[KEY_AUTO_REJECT] = value }
    suspend fun setNotifications(value: Boolean) = context.dataStore.edit { it[KEY_NOTIFICATIONS] = value }
    suspend fun setBlockAfterReply(value: Boolean) = context.dataStore.edit { it[KEY_BLOCK_AFTER_REPLY] = value }
    suspend fun setSpeechRate(value: Float) = context.dataStore.edit { it[KEY_SPEECH_RATE] = value }
    suspend fun setPitch(value: Float) = context.dataStore.edit { it[KEY_PITCH] = value }
    suspend fun setVoiceNameFor(lang: String, value: String) =
        context.dataStore.edit { it[stringPreferencesKey("voice_name_$lang")] = value }
    suspend fun setWelcomeShown(value: Boolean) = context.dataStore.edit { it[KEY_WELCOME_SHOWN] = value }
    suspend fun setLastSpamSync(ts: Long) = context.dataStore.edit { it[KEY_LAST_SPAM_SYNC] = ts }
    suspend fun setBundledLoaded() = context.dataStore.edit { it[KEY_BUNDLED_LOADED] = true }
    suspend fun setMonitorLive(value: Boolean) = context.dataStore.edit { it[KEY_MONITOR_LIVE] = value }
    suspend fun setContributeDb(value: Boolean) = context.dataStore.edit { it[KEY_CONTRIBUTE_DB] = value }
    suspend fun setBlockHiddenNumbers(value: Boolean) = context.dataStore.edit { it[KEY_BLOCK_HIDDEN] = value }
    suspend fun setCountry(value: String) = context.dataStore.edit { it[KEY_COUNTRY] = value }
    suspend fun setPremium(value: Boolean) = context.dataStore.edit { it[KEY_IS_PREMIUM] = value }
    suspend fun setReplyLanguage(value: String) = context.dataStore.edit { it[KEY_REPLY_LANGUAGE] = value }
    suspend fun setAppLanguage(value: String) = context.dataStore.edit { it[KEY_APP_LANGUAGE] = value }
}
