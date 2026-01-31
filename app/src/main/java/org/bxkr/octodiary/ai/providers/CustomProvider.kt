package org.bxkr.octodiary.ai.providers


import androidx.compose.material.icons.Icons
import android.graphics.Bitmap
import android.content.Context

class CustomProvider(private val context: Context) : AiProvider {
    override val name: String = "Custom"
    private var apiKey: String = ""
    private var baseUrl: String? = null

    override fun getApiKey(): String = apiKey
    override fun setApiKey(apiKey: String) { this.apiKey = apiKey }
    override fun getBaseUrl(): String? = baseUrl
    override fun setBaseUrl(baseUrl: String?) { this.baseUrl = baseUrl }

    override suspend fun generateText(prompt: String, history: List<ChatMessage>, model: String?): Result<String> {
        return Result.success("Mocked Custom response for: $prompt with base URL: $baseUrl")
    }

    override suspend fun generateTextFromImage(prompt: String, image: Bitmap, model: String?): Result<String> {
        return Result.success("Mocked Custom image response for: $prompt with base URL: $baseUrl")
    }

    override suspend fun getAvailableModels(): List<String> {
        return listOf("custom-model-1", "custom-model-2")
    }
}



