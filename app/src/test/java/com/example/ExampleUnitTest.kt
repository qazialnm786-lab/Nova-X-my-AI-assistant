package com.example

import com.example.ai.GeminiService
import com.example.data.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testLanguageDetection_Hindi() {
        val hindiText = "नमस्ते, आप कैसे हैं?"
        val detected = GeminiService.detectLanguage(hindiText)
        assertEquals("Hindi", detected)
    }

    @Test
    fun testLanguageDetection_Hinglish() {
        val hinglishText = "Mujhe coding mein help karo, kaise shuru karun?"
        val detected = GeminiService.detectLanguage(hinglishText)
        assertEquals("Hinglish", detected)
    }

    @Test
    fun testLanguageDetection_English() {
        val englishText = "Can you explain how neural networks function?"
        val detected = GeminiService.detectLanguage(englishText)
        assertEquals("English", detected)
    }

    @Test
    fun testUserSettings_Defaults() {
        val settings = UserSettings()
        assertEquals("NOVA-X", settings.assistantName)
        assertEquals(true, settings.autoSpeakEnabled)
        assertEquals(true, settings.memoryEnabled)
        assertEquals("Auto-Detect", settings.languageMode)
    }
}

