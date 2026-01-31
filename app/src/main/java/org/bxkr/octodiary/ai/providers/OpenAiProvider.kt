package org.bxkr.octodiary.ai.providers


import androidx.compose.material.icons.Icons
import android.graphics.Bitmap
import android.content.Context

class OpenAiProvider(private val context: Context) : AiProvider {
    override val name: String = "OpenAI"
    private var apiKey: String = ""
    private var baseUrl: String? = null

    override fun getApiKey(): String = apiKey
    override fun setApiKey(apiKey: String) { this.apiKey = apiKey }
    override fun getBaseUrl(): String? = baseUrl
    override fun setBaseUrl(baseUrl: String?) { this.baseUrl = baseUrl }

    override suspend fun generateText(prompt: String, history: List<ChatMessage>, model: String?): Result<String> {
        return Result.success("Mocked OpenAI response for: $prompt")
    }

    override suspend fun generateTextFromImage(prompt: String, image: Bitmap, model: String?): Result<String> {
        return Result.success("Mocked OpenAI image response for: $prompt")
    }

    override suspend fun getAvailableModels(): List<String> {
        return listOf("gpt-3.5-turbo", "gpt-4", "gpt-4o")
    }
}



