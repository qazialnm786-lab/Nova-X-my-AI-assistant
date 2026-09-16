package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceManager(
    private val context: Context,
    private val onSpeechRecognized: (String) -> Unit,
    private val onError: (String) -> Unit
) : TextToSpeech.OnInitListener {

    private val TAG = "VoiceManager"

    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private var speechRecognizer: SpeechRecognizer? = null

    // State flows for Compose UI
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _availableFemaleVoices = MutableStateFlow<List<Voice>>(emptyList())
    val availableFemaleVoices: StateFlow<List<Voice>> = _availableFemaleVoices.asStateFlow()

    // Configurable voice parameters - strictly female defaults
    var pitch: Float = 1.18f // Warm, youthful, natural female voice pitch
    var speechRate: Float = 1.0f
    var preferredVoiceName: String = ""

    // Session cache: keeps the exact same female voice consistently throughout the conversation
    private val lockedFemaleVoicesByLocale = mutableMapOf<String, Voice>()

    companion object {
        // Voice tokens known to be male in Google TTS & Android TTS engines
        private val EXCLUDED_MALE_TOKENS = listOf(
            "male", "man", "boy", "guy", "masculine", "masculino",
            "-sfb", "-sfd", "-tpa", "-iom", "-hia", "-hif", "-cfa", "-cmd"
        )

        // Tokens indicating authentic female voice quality
        private val FEMALE_TOKENS = listOf(
            "female", "woman", "girl", "femme", "femenino",
            // Google US English female voices
            "-sfg", "-tpf", "-iob", "-iol", "-tpc",
            // Google Indian English (Hinglish) female voices
            "-enc", "-end", "-ena", "-cxx",
            // Google Hindi female voices
            "-hie", "-hid", "-cfc", "-hic"
        )
    }

    init {
        try {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                }
            })

            // Filter available female voices across all installed languages
            try {
                val voices = textToSpeech?.voices?.filter { voice ->
                    isVoiceFemale(voice)
                } ?: emptyList()
                _availableFemaleVoices.value = voices
            } catch (e: Exception) {
                Log.w(TAG, "Voices list query unavailable on this engine", e)
            }
        } else {
            Log.e(TAG, "TTS Initialization failed with status: $status")
            isTtsInitialized = false
        }
    }

    /**
     * Strictly verifies whether a voice candidate is female and not male.
     */
    private fun isVoiceFemale(voice: Voice): Boolean {
        val name = voice.name.lowercase()
        // Strictly reject any male voice
        if (EXCLUDED_MALE_TOKENS.any { name.contains(it) }) {
            return false
        }
        // Match recognized female voice identifiers
        return FEMALE_TOKENS.any { name.contains(it) }
    }

    /**
     * Speaks text using a natural female voice.
     * Automatically configures language (Hindi vs English/Hinglish).
     * Enforces female voice only.
     */
    fun speak(text: String, detectedLang: String = "English") {
        if (!isTtsInitialized || textToSpeech == null) {
            onError("Voice synthesis is still initializing. Please try again.")
            return
        }

        stopSpeaking()

        val cleanText = sanitizeTextForSpeech(text)
        if (cleanText.isBlank()) return

        val tts = textToSpeech ?: return

        // Always guarantee pitch is in the warm, youthful female frequency band (at least 1.15f)
        val guaranteedFemalePitch = pitch.coerceIn(1.15f, 1.35f)
        tts.setPitch(guaranteedFemalePitch)
        tts.setSpeechRate(speechRate)

        // Select best language and female voice
        val targetLocale = when (detectedLang) {
            "Hindi" -> Locale("hi", "IN")
            "Hinglish" -> Locale("en", "IN") // Indian English accent fits Hinglish best
            else -> Locale("en", "US")
        }

        try {
            val langResult = tts.setLanguage(targetLocale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.language = Locale.getDefault()
            }

            // Exclusively pick and lock the female voice
            selectConsistentFemaleVoice(tts, targetLocale)
        } catch (e: Exception) {
            Log.w(TAG, "Failed setting language for TTS", e)
        }

        val utteranceId = "NOVA_X_${System.currentTimeMillis()}"
        val params = Bundle()
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    /**
     * Selects and locks a female voice. Never switches or falls back to a male voice.
     */
    private fun selectConsistentFemaleVoice(tts: TextToSpeech, targetLocale: Locale) {
        val localeKey = "${targetLocale.language}_${targetLocale.country}"

        // 1. Maintain consistent voice across entire conversation
        val lockedVoice = lockedFemaleVoicesByLocale[localeKey]
        if (lockedVoice != null && tts.voices?.contains(lockedVoice) == true) {
            tts.voice = lockedVoice
            return
        }

        try {
            val voices = tts.voices ?: return
            val languageVoices = voices.filter { it.locale.language == targetLocale.language }

            // User preference if specified and verified female
            if (preferredVoiceName.isNotBlank()) {
                val userMatch = languageVoices.firstOrNull { it.name == preferredVoiceName && isVoiceFemale(it) }
                if (userMatch != null) {
                    tts.voice = userMatch
                    lockedFemaleVoicesByLocale[localeKey] = userMatch
                    return
                }
            }

            // Prioritize high-quality female voices
            val femaleCandidates = languageVoices.filter { isVoiceFemale(it) }

            val chosenVoice = femaleCandidates.firstOrNull { voice ->
                // Prefer high-quality, local installed voices
                !voice.isNetworkConnectionRequired && voice.quality >= Voice.QUALITY_HIGH
            } ?: femaleCandidates.firstOrNull { voice ->
                voice.quality >= Voice.QUALITY_HIGH
            } ?: femaleCandidates.firstOrNull()
              ?: languageVoices.firstOrNull { voice ->
                  // Safe fallback: any voice not having male tokens
                  val lowerName = voice.name.lowercase()
                  !EXCLUDED_MALE_TOKENS.any { lowerName.contains(it) }
              }

            if (chosenVoice != null) {
                tts.voice = chosenVoice
                lockedFemaleVoicesByLocale[localeKey] = chosenVoice
            }
        } catch (e: Exception) {
            Log.w(TAG, "Voice selection error: ${e.message}")
        }
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
            _isSpeaking.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech", e)
        }
    }

    /**
     * Starts listening via SpeechRecognizer.
     */
    fun startListening(languagePreference: String = "Auto-Detect") {
        if (_isListening.value) {
            stopListening()
            return
        }

        stopSpeaking()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition is not available on this device. Please type your message.")
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _audioRms.value = 0f
                    }

                    override fun onBeginningOfSpeech() {
                        _audioRms.value = 2f
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize RMS from ~[-2, 10] dB to [0, 1]
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _audioRms.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _audioRms.value = 0f
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _audioRms.value = 0f
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Client recognition error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error during speech recognition"
                            SpeechRecognizer.ERROR_NO_MATCH -> "Couldn't catch that. Please speak again or type."
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice recognizer is busy"
                            SpeechRecognizer.ERROR_SERVER -> "Voice recognition server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                            else -> "Voice recognition error ($error)"
                        }
                        // Only notify user if not just a quiet timeout
                        if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                            onError(msg)
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _audioRms.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim()
                        if (!text.isNullOrBlank()) {
                            onSpeechRecognized(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

                val reqLang = when (languagePreference) {
                    "Hindi" -> "hi-IN"
                    "Hinglish" -> "en-IN"
                    "English" -> "en-US"
                    else -> Locale.getDefault().toLanguageTag()
                }
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, reqLang)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, reqLang)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            _audioRms.value = 0f
            Log.e(TAG, "Error starting speech recognition", e)
            onError("Unable to open microphone: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recognition", e)
        } finally {
            _isListening.value = false
            _audioRms.value = 0f
        }
    }

    fun cleanup() {
        stopSpeaking()
        stopListening()
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying speech recognizer", e)
        }
        try {
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        }
    }

    /**
     * Strips markdown symbols, code fences, and asterisks for smooth, natural speech.
     */
    private fun sanitizeTextForSpeech(text: String): String {
        return text
            // Remove code blocks
            .replace(Regex("```[\\s\\S]*?```"), " Here is the code snippet. ")
            // Remove inline code
            .replace(Regex("`([^`]+)`"), "$1")
            // Remove markdown headers
            .replace(Regex("^#+\\s*", RegexOption.MULTILINE), "")
            // Remove bold/italics
            .replace(Regex("[*_~]"), "")
            // Replace URLs
            .replace(Regex("https?://\\S+"), "web link")
            // Remove bullet point markers
            .replace(Regex("^\\s*[-•*]\\s+", RegexOption.MULTILINE), "")
            .trim()
    }
}
