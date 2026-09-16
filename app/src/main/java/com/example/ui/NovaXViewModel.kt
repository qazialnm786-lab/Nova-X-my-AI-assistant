package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiService
import com.example.data.AppDatabase
import com.example.data.ChatMessageEntity
import com.example.data.ChatRepository
import com.example.data.UserSettings
import com.example.data.UserSettingsRepository
import com.example.ui.components.OrbState
import com.example.voice.VoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NovaXViewModel(application: Application) : AndroidViewModel(application) {

    private val chatRepository: ChatRepository
    private val settingsRepository: UserSettingsRepository
    val voiceManager: VoiceManager

    val allMessages: StateFlow<List<ChatMessageEntity>>
    val settings: StateFlow<UserSettings>

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentSpeakingContent = MutableStateFlow<String?>(null)
    val currentSpeakingContent: StateFlow<String?> = _currentSpeakingContent.asStateFlow()

    // Combined Orb state derived from listening, thinking, speaking
    val orbState: StateFlow<OrbState>

    init {
        val database = AppDatabase.getDatabase(application)
        chatRepository = ChatRepository(database.chatDao())
        settingsRepository = UserSettingsRepository(application)
        settings = settingsRepository.settings

        voiceManager = VoiceManager(
            context = application,
            onSpeechRecognized = { text ->
                sendMessage(text, isVoice = true)
            },
            onError = { err ->
                _errorMessage.value = err
            }
        )

        // Keep VoiceManager pitch and speed synced with settings
        viewModelScope.launch {
            settings.collect { currentSettings ->
                voiceManager.pitch = currentSettings.voicePitch
                voiceManager.speechRate = currentSettings.voiceSpeed
                voiceManager.preferredVoiceName = currentSettings.preferredVoiceName
            }
        }

        allMessages = chatRepository.allMessages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        orbState = combine(
            voiceManager.isListening,
            _isThinking,
            voiceManager.isSpeaking
        ) { isListening, isThinking, isSpeaking ->
            when {
                isListening -> OrbState.LISTENING
                isThinking -> OrbState.THINKING
                isSpeaking -> OrbState.SPEAKING
                else -> OrbState.IDLE
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OrbState.IDLE
        )

        // Add welcome message if chat is empty
        viewModelScope.launch {
            chatRepository.allMessages.collect { list ->
                if (list.isEmpty()) {
                    val welcomeText = "Namaste! I'm NOVA-X, your personal voice AI assistant. You can speak with me naturally in English, Hindi, or Hinglish, or ask me anything from study and coding to daily tasks."
                    chatRepository.addMessage(
                        role = "assistant",
                        content = welcomeText,
                        language = "English",
                        isVoiceInput = false
                    )
                }
            }
        }
    }

    fun sendMessage(text: String, isVoice: Boolean = false) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        _errorMessage.value = null
        val detectedUserLang = GeminiService.detectLanguage(trimmed)

        viewModelScope.launch {
            // Save user message to database
            chatRepository.addMessage(
                role = "user",
                content = trimmed,
                language = detectedUserLang,
                isVoiceInput = isVoice
            )

            _isThinking.value = true

            val currentSettings = settings.value
            val history = if (currentSettings.memoryEnabled) {
                chatRepository.getRecentMessages(12)
            } else {
                emptyList()
            }

            val result = GeminiService.generateResponse(
                prompt = trimmed,
                history = history,
                memoryEnabled = currentSettings.memoryEnabled,
                customApiKey = currentSettings.customApiKey,
                languagePreference = currentSettings.languageMode
            )

            _isThinking.value = false

            result.onSuccess { reply ->
                val detectedReplyLang = GeminiService.detectLanguage(reply)
                chatRepository.addMessage(
                    role = "assistant",
                    content = reply,
                    language = detectedReplyLang,
                    isVoiceInput = false
                )

                // Auto-speak response if enabled
                if (currentSettings.autoSpeakEnabled) {
                    speakResponse(reply, detectedReplyLang)
                }
            }.onFailure { err ->
                val errorMsg = err.localizedMessage ?: "Unknown connection issue"
                _errorMessage.value = errorMsg
                chatRepository.addMessage(
                    role = "assistant",
                    content = "I encountered an issue: $errorMsg. Please check your network connection or API key in Settings.",
                    language = "English",
                    isVoiceInput = false
                )
            }
        }
    }

    fun toggleVoiceInput() {
        if (voiceManager.isListening.value) {
            voiceManager.stopListening()
        } else {
            voiceManager.stopSpeaking()
            _currentSpeakingContent.value = null
            voiceManager.startListening(settings.value.languageMode)
        }
    }

    fun speakResponse(content: String, language: String) {
        _currentSpeakingContent.value = content
        voiceManager.speak(content, language)
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
        _currentSpeakingContent.value = null
    }

    fun startNewChat() {
        stopSpeaking()
        voiceManager.stopListening()
        viewModelScope.launch {
            chatRepository.clearAll()
            val welcomeText = "Fresh conversation started! How can I assist you right now?"
            chatRepository.addMessage(
                role = "assistant",
                content = welcomeText,
                language = "English",
                isVoiceInput = false
            )
        }
    }

    fun clearChat() {
        stopSpeaking()
        voiceManager.stopListening()
        viewModelScope.launch {
            chatRepository.clearAll()
        }
    }

    fun resetAllData() {
        stopSpeaking()
        voiceManager.stopListening()
        viewModelScope.launch {
            chatRepository.clearAll()
            settingsRepository.resetToDefaults()
        }
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    // Settings actions
    fun updateAssistantName(name: String) = settingsRepository.updateAssistantName(name)
    fun updateLanguageMode(lang: String) = settingsRepository.updateLanguageMode(lang)
    fun updateVoicePitch(pitch: Float) = settingsRepository.updateVoicePitch(pitch)
    fun updateVoiceSpeed(speed: Float) = settingsRepository.updateVoiceSpeed(speed)
    fun updateAutoSpeak(enabled: Boolean) = settingsRepository.updateAutoSpeak(enabled)
    fun updateMemory(enabled: Boolean) = settingsRepository.updateMemoryEnabled(enabled)
    fun updateCustomApiKey(key: String) = settingsRepository.updateCustomApiKey(key)

    fun testVoice() {
        val testGreeting = when (settings.value.languageMode) {
            "Hindi" -> "नमस्ते, मैं नोवा-एक्स हूँ। आपकी क्या मदद कर सकती हूँ?"
            "Hinglish" -> "Namaste! Main NOVA-X hoon. Aapki kya madad kar sakti hoon?"
            else -> "Hello! I am NOVA-X, your personal voice AI assistant."
        }
        speakResponse(testGreeting, settings.value.languageMode)
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.cleanup()
    }
}
