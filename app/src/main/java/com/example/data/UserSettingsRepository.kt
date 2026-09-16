package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSettings(
    val assistantName: String = "NOVA-X",
    val languageMode: String = "Auto-Detect", // "Auto-Detect", "English", "Hindi", "Hinglish"
    val voicePitch: Float = 1.18f, // Natural young female pitch
    val voiceSpeed: Float = 1.0f,
    val autoSpeakEnabled: Boolean = true,
    val memoryEnabled: Boolean = true,
    val customApiKey: String = "",
    val preferredVoiceName: String = ""
)

class UserSettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("novax_preferences", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        return UserSettings(
            assistantName = prefs.getString("assistant_name", "NOVA-X") ?: "NOVA-X",
            languageMode = prefs.getString("language_mode", "Auto-Detect") ?: "Auto-Detect",
            voicePitch = prefs.getFloat("voice_pitch", 1.18f),
            voiceSpeed = prefs.getFloat("voice_speed", 1.0f),
            autoSpeakEnabled = prefs.getBoolean("auto_speak", true),
            memoryEnabled = prefs.getBoolean("memory_enabled", true),
            customApiKey = prefs.getString("custom_api_key", "") ?: "",
            preferredVoiceName = prefs.getString("preferred_voice_name", "") ?: ""
        )
    }

    fun updateAssistantName(name: String) {
        val trimmed = name.trim().ifEmpty { "NOVA-X" }
        prefs.edit().putString("assistant_name", trimmed).apply()
        _settings.value = _settings.value.copy(assistantName = trimmed)
    }

    fun updateLanguageMode(mode: String) {
        prefs.edit().putString("language_mode", mode).apply()
        _settings.value = _settings.value.copy(languageMode = mode)
    }

    fun updateVoicePitch(pitch: Float) {
        prefs.edit().putFloat("voice_pitch", pitch).apply()
        _settings.value = _settings.value.copy(voicePitch = pitch)
    }

    fun updateVoiceSpeed(speed: Float) {
        prefs.edit().putFloat("voice_speed", speed).apply()
        _settings.value = _settings.value.copy(voiceSpeed = speed)
    }

    fun updateAutoSpeak(enabled: Boolean) {
        prefs.edit().putBoolean("auto_speak", enabled).apply()
        _settings.value = _settings.value.copy(autoSpeakEnabled = enabled)
    }

    fun updateMemoryEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("memory_enabled", enabled).apply()
        _settings.value = _settings.value.copy(memoryEnabled = enabled)
    }

    fun updateCustomApiKey(key: String) {
        prefs.edit().putString("custom_api_key", key.trim()).apply()
        _settings.value = _settings.value.copy(customApiKey = key.trim())
    }

    fun updatePreferredVoiceName(name: String) {
        prefs.edit().putString("preferred_voice_name", name).apply()
        _settings.value = _settings.value.copy(preferredVoiceName = name)
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _settings.value = UserSettings()
    }
}
