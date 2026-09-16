package com.example.data

import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {
    val allMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()

    suspend fun addMessage(
        role: String,
        content: String,
        language: String = "English",
        isVoiceInput: Boolean = false
    ): Long {
        val entity = ChatMessageEntity(
            role = role,
            content = content,
            timestamp = System.currentTimeMillis(),
            detectedLanguage = language,
            isVoiceInput = isVoiceInput
        )
        return chatDao.insertMessage(entity)
    }

    suspend fun getRecentMessages(limit: Int): List<ChatMessageEntity> {
        return chatDao.getRecentMessages(limit).reversed()
    }

    suspend fun clearAll() {
        chatDao.clearAllMessages()
    }

    suspend fun deleteMessage(id: Long) {
        chatDao.deleteMessage(id)
    }
}
