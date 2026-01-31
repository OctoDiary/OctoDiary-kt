package org.bxkr.octodiary.ai


import androidx.compose.material.icons.Icons
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.bxkr.octodiary.ai.providers.AiProvider
import org.bxkr.octodiary.ai.providers.AnthropicProvider
import org.bxkr.octodiary.ai.providers.CustomProvider
import org.bxkr.octodiary.ai.providers.GeminiProvider
import org.bxkr.octodiary.ai.providers.OpenAiProvider
import android.content.Context
import android.graphics.Bitmap // Import Bitmap for AiProvider interface
import org.bxkr.octodiary.ai.providers.ChatMessage

class AiSettingsViewModel(private val context: Context) : ViewModel() { // Context will be passed manually for now
    private val _uiState = MutableStateFlow(AiSettings())
    val uiState: StateFlow<AiSettings> = _uiState

    // Map of available providers
    private val providers: Map<String, AiProvider> by lazy {
        mapOf(
            "Disabled" to object : AiProvider { // Dummy provider for "Disabled" state
                override val name: String = "Disabled"
                override fun getApiKey(): String = ""
                override fun setApiKey(apiKey: String) {}
                override fun getBaseUrl(): String? = null
                override fun setBaseUrl(baseUrl: String?) {}
                override suspend fun generateText(prompt: String, history: List<ChatMessage>, model: String?): Result<String> = Result.failure(IllegalStateException("AI Disabled"))
                override suspend fun generateTextFromImage(prompt: String, image: Bitmap, model: String?): Result<String> = Result.failure(IllegalStateException("AI Disabled"))
                override suspend fun getAvailableModels(): List<String> = emptyList()
            },
            "OpenAI" to OpenAiProvider(context),
            "Gemini" to GeminiProvider(context),
            "Anthropic" to AnthropicProvider(context),
            "Custom" to CustomProvider(context)
        )
    }

    init {
        // Load initial settings (e.g., from SharedPreferences, mock for now)
        loadSettings()
    }

    fun loadSettings() {
        // Mock loading for now, will integrate SharedPreferences later
        viewModelScope.launch {
            _uiState.value = AiSettings(
                selectedProvider = "Disabled",
                openaiApiKey = "mock_openai_key",
                geminiApiKey = "mock_gemini_key",
                customBaseUrl = "http://localhost:8080/v1"
            )
        }
    }

    fun saveSettings(newSettings: AiSettings) {
        viewModelScope.launch {
            _uiState.value = newSettings
            // Mock saving for now, will integrate SharedPreferences later
        }
    }

    fun getAvailableProviders(): List<String> = providers.keys.toList()

    fun getProvider(name: String): AiProvider? = providers[name]

    // Simplified way to get the active provider based on current settings
    fun getActiveProvider(): AiProvider {
        val currentSettings = _uiState.value
        return providers[currentSettings.selectedProvider] ?: providers["Disabled"]!!
    }
}



