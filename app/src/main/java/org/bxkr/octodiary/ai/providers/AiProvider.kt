package org.bxkr.octodiary.ai.providers


import androidx.compose.material.icons.Icons
import android.graphics.Bitmap

interface AiProvider {
    val name: String
    fun getApiKey(): String
    fun setApiKey(apiKey: String)
    fun getBaseUrl(): String?
    fun setBaseUrl(baseUrl: String?)

    suspend fun generateText(prompt: String, history: List<ChatMessage>, model: String? = null): Result<String>
    suspend fun generateTextFromImage(prompt: String, image: Bitmap, model: String? = null): Result<String>
    suspend fun getAvailableModels(): List<String>
}

// Data class для сообщения чата (будет использоваться в истории)
data class ChatMessage(val content: String, val isUser: Boolean, val imageUri: String? = null)


