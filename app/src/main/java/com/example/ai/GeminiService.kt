package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.ChatMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val NOVA_X_SYSTEM_INSTRUCTION = """
You are NOVA-X, a personal AI voice assistant with a warm, intelligent, confident, and witty persona.
You have a natural, youthful female voice and attitude.
You are a real personal companion and assistant, never a robotic FAQ bot.

Key Instructions:
1. Speak naturally, warmly, and directly. Avoid robotic introductions or repetitive generic greetings.
2. Fluently support Hindi, English, and Hinglish.
   - If the user talks to you in Hindi (Devanagari or Romanized), reply in warm, natural Hindi.
   - If the user talks to you in Hinglish (e.g. "Kya haal hai? mujhe code samjhao"), reply in natural, fluent Hinglish.
   - If the user talks in English, reply in natural conversational English.
   - Automatically match the user's language and tone.
3. When explaining complicated concepts (coding, math, science, tech), break them down step-by-step with simple, intuitive analogies.
4. Format output cleanly: use concise paragraphs, bullet points when listing items, and code blocks with syntax when providing code.
5. You can assist with general questions, study, coding, mathematics, creative writing, translation, summaries, planning, and troubleshooting.
6. Maintain conversational continuity by referencing the user's previous context when relevant.
7. Be genuine: if you do not know a real-time fact or if something is unavailable, acknowledge it gracefully. Never invent false facts.
8. Keep responses conversational and easy to listen to when spoken aloud by text-to-speech.
"""

    suspend fun generateResponse(
        prompt: String,
        history: List<ChatMessageEntity> = emptyList(),
        memoryEnabled: Boolean = true,
        customApiKey: String = "",
        languagePreference: String = "Auto-Detect"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = when {
            customApiKey.isNotBlank() -> customApiKey
            BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }

        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please add your GEMINI_API_KEY to AI Studio Secrets panel or enter it in NOVA-X Settings.")
            )
        }

        try {
            val url = "$BASE_URL?key=$apiKey"
            val rootJson = JSONObject()

            // System Instruction
            var sysPrompt = NOVA_X_SYSTEM_INSTRUCTION
            if (languagePreference != "Auto-Detect") {
                sysPrompt += "\nThe user preferred language setting is: $languagePreference. Respond primarily in $languagePreference."
            }

            val systemInstructionObj = JSONObject()
            val sysPartsArray = JSONArray()
            val sysPart = JSONObject().put("text", sysPrompt.trim())
            sysPartsArray.put(sysPart)
            systemInstructionObj.put("parts", sysPartsArray)
            rootJson.put("systemInstruction", systemInstructionObj)

            // Contents array with conversational history
            val contentsArray = JSONArray()

            if (memoryEnabled && history.isNotEmpty()) {
                // Include up to last 10 messages for memory context
                val memoryMessages = history.takeLast(10)
                for (msg in memoryMessages) {
                    val contentObj = JSONObject()
                    val role = if (msg.role == "user") "user" else "model"
                    contentObj.put("role", role)

                    val parts = JSONArray()
                    parts.put(JSONObject().put("text", msg.content))
                    contentObj.put("parts", parts)
                    contentsArray.put(contentObj)
                }
            }

            // Current user turn
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", prompt))
            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)

            rootJson.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            genConfig.put("topP", 0.95)
            genConfig.put("topK", 40)
            rootJson.put("generationConfig", genConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = rootJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "API call failed with code ${response.code}: $responseBody")
                val errorMessage = parseErrorMessage(responseBody, response.code)
                return@withContext Result.failure(Exception(errorMessage))
            }

            val parsedJson = JSONObject(responseBody)
            val candidates = parsedJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val replyText = parts.getJSONObject(0).optString("text", "")
                    if (replyText.isNotBlank()) {
                        return@withContext Result.success(replyText)
                    }
                }
            }

            Result.failure(Exception("NOVA-X did not receive any text in response."))
        } catch (e: Exception) {
            Log.e(TAG, "Error generating AI response", e)
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(jsonBody: String, statusCode: Int): String {
        return try {
            val json = JSONObject(jsonBody)
            val errorObj = json.optJSONObject("error")
            val message = errorObj?.optString("message")
            if (!message.isNullOrBlank()) {
                "NOVA-X: $message (HTTP $statusCode)"
            } else {
                "NOVA-X: Service error (HTTP $statusCode)"
            }
        } catch (e: Exception) {
            "NOVA-X connection error (HTTP $statusCode)"
        }
    }

    fun detectLanguage(text: String): String {
        // Check for Devanagari script (Hindi)
        val hasHindiScript = text.any { it in '\u0900'..'\u097F' }
        if (hasHindiScript) return "Hindi"

        // Check for common Hinglish vocabulary
        val lower = text.lowercase()
        val hinglishKeywords = listOf(
            "kya", "hai", "kaise", "apna", "mera", "meri", "batao", "bataiye",
            "mujhe", "karna", "acha", "theek", "shukriya", "namaste", "dost",
            "samjhao", "bolo", "suno", "aaj", "kal", "kyun", "kahan", "kaun",
            "haan", "nahi", "accha", "kuch", "sab"
        )
        val matches = hinglishKeywords.count { lower.contains(it) }
        if (matches >= 2) return "Hinglish"

        return "English"
    }
}
