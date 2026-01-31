package org.bxkr.octodiary.ai


import androidx.compose.material.icons.Icons
data class AiSettings(
    val selectedProvider: String = "Disabled", // e.g., "Disabled", "OpenAI", "Gemini", "Anthropic", "Custom"
    val openaiApiKey: String = "",
    val geminiApiKey: String = "",
    val anthropicApiKey: String = "",
    val customApiKey: String = "",
    val customBaseUrl: String = "",
    val customModel: String = "" // For custom providers or specific model selection
)



