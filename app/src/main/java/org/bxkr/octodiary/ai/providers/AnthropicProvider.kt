package org.bxkr.octodiary.ai.providers


import androidx.compose.material.icons.Icons
import android.graphics.Bitmap
import android.content.Context

class AnthropicProvider(private val context: Context) : AiProvider {
    override val name: String = "Anthropic"
    private var apiKey: String = ""
    private var baseUrl: String? = null

    override fun getApiKey(): String = apiKey
    override fun setApiKey(apiKey: String) { this.apiKey = apiKey }
    override fun getBaseUrl(): String? = baseUrl
    override fun setBaseUrl(baseUrl: String?) { this.baseUrl = baseUrl }

    override suspend fun generateText(prompt: String, history: List<ChatMessage>, model: String?): Result<String> {
        return Result.success("Mocked Anthropic response for: $prompt")
    }

    override suspend fun generateTextFromImage(prompt: String, image: Bitmap, model: String?): Result<String> {
        return Result.success("Mocked Anthropic image response for: $prompt")
    }

    override suspend fun getAvailableModels(): List<String> {
        return listOf("claude-3-opus-20240229", "claude-3-sonnet-20240229")
    }
}



