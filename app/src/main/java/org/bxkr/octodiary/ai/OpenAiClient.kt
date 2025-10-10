package org.bxkr.octodiary.ai

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import java.util.concurrent.TimeUnit

data class OpenAiMessage(
    val role: String,
    val content: String
)

data class OpenAiRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val temperature: Double = 0.2,
    @SerializedName("max_tokens") val maxTokens: Int = 2048
)

data class OpenAiChoice(
    val message: OpenAiMessage,
    val index: Int
)

data class OpenAiResponse(
    val choices: List<OpenAiChoice>
)

// Gemini-specific structures
data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiCandidate(
    val content: GeminiContent
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>
)

data class ScreenAiStep(
    val x: Int? = null,
    val y: Int? = null,
    val waitMs: Int = 0,
    val done: Boolean = false,
    val message: String? = null
)

object OpenAiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun testApiKey(context: Context): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = context.mainPrefs.get<String>("ai_api_key") ?: ""
                if (apiKey.isBlank()) return@withContext false to "API-ключ не указан"
                
                val provider = context.mainPrefs.get<String>("ai_provider") ?: "openai"
                val model = when (provider) {
                    "openai" -> "gpt-4o-mini"
                    "gemini" -> "gemini-flash-latest"
                    "openrouter" -> "openai/gpt-4o-mini"
                    else -> "gpt-4o-mini"
                }
                
                val result = complete(context, model, "Test: respond with 'OK'")
                if (result.contains("Ошибка API")) {
                    false to result
                } else {
                    true to "API-ключ работает"
                }
            } catch (e: Exception) {
                false to "Ошибка: ${e.message}"
            }
        }
    }

    suspend fun complete(context: Context, model: String, prompt: String): String {
        return withContext(Dispatchers.IO) {
            val apiKey = context.mainPrefs.get<String>("ai_api_key") ?: ""
            val provider = context.mainPrefs.get<String>("ai_provider") ?: "openai"
            
            val baseUrl = when (provider) {
                "openai" -> "https://api.openai.com/v1"
                "gemini" -> "https://generativelanguage.googleapis.com/v1beta/models"
                "openrouter" -> "https://openrouter.ai/api/v1"
                "custom" -> context.mainPrefs.get<String>("ai_base_url")?.takeIf { it.isNotBlank() }
                    ?: "https://api.openai.com/v1"
                else -> "https://api.openai.com/v1"
            }
            
            // Use custom model if specified
            val actualModel = if (model == "custom") {
                context.mainPrefs.get<String>("ai_custom_model") ?: "gpt-4o"
            } else {
                model
            }

            val systemPrompt = """You are an automated screen interaction assistant. Analyze the screenshot and return ONLY a JSON response with coordinates to click.

CRITICAL: Return ONLY valid JSON, no explanations, no markdown, no text outside JSON.

Format:
- To click: {"x": 123, "y": 456, "wait": 1500, "done": false}
- To finish: {"done": true}

Rules:
1. x, y are pixel coordinates to click on the screenshot
2. wait is milliseconds to wait after click (200-15000)
3. done=true only when task is completely finished
4. If you cannot find anything to click, return {"done": true}

Examples:
{"x": 250, "y": 300, "wait": 1000, "done": false}
{"x": 100, "y": 200, "wait": 2000, "done": false}
{"done": true}

Task: $prompt""".trimMargin()
            
            val json = if (provider == "gemini") {
                val geminiRequest = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart("$systemPrompt\n\n$prompt")))
                    )
                )
                Gson().toJson(geminiRequest)
            } else {
                val requestBody = OpenAiRequest(
                    model = actualModel,
                    messages = listOf(
                        OpenAiMessage("system", systemPrompt),
                        OpenAiMessage("user", prompt)
                    ),
                    temperature = 0.2,
                    maxTokens = 2048
                )
                Gson().toJson(requestBody)
            }
            
            val mediaType = MediaType.parse("application/json; charset=utf-8")
            val body = RequestBody.create(mediaType, json)

            val url = if (provider == "gemini") {
                "$baseUrl/$actualModel:generateContent?key=$apiKey"
            } else {
                "$baseUrl/chat/completions"
            }
            
            val request = Request.Builder()
                .url(url)
                .apply {
                    if (provider != "gemini") {
                        addHeader("Authorization", "Bearer $apiKey")
                    }
                }
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body()?.string() ?: ""
                android.util.Log.e("OpenAiClient", "API Error: ${response.code()} ${response.message()}\nBody: $errorBody")
                return@withContext "Ошибка API: ${response.code()} ${response.message()}\n$errorBody"
            }

            val responseBody = response.body()?.string() ?: return@withContext "Пустой ответ от API"
            
            if (provider == "gemini") {
                val geminiResponse = Gson().fromJson(responseBody, GeminiResponse::class.java)
                geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Нет ответа от модели"
            } else {
                val openAiResponse = Gson().fromJson(responseBody, OpenAiResponse::class.java)
                openAiResponse.choices.firstOrNull()?.message?.content ?: "Нет ответа от модели"
            }
        }
    }

    suspend fun completeWithScreenshot(
        context: Context,
        model: String,
        prompt: String,
        bitmap: android.graphics.Bitmap
    ): ScreenAiStep = withContext(Dispatchers.IO) {
        try {
            val apiKey = context.mainPrefs.get<String>("ai_api_key") ?: ""
            val provider = context.mainPrefs.get<String>("ai_provider") ?: "openai"
            val baseUrl = when (provider) {
                "openai" -> "https://api.openai.com/v1"
                "gemini" -> "https://generativelanguage.googleapis.com/v1beta/models"
                "openrouter" -> "https://openrouter.ai/api/v1"
                "custom" -> context.mainPrefs.get<String>("ai_base_url")?.takeIf { it.isNotBlank() } ?: "https://api.openai.com/v1"
                else -> "https://api.openai.com/v1"
            }
            val endpoint = "$baseUrl/screen_click/step"
            val byteStream = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, byteStream)
            val imageBody = okhttp3.RequestBody.create(MediaType.parse("image/png"), byteStream.toByteArray())
            val promptBody = okhttp3.RequestBody.create(MediaType.parse("text/plain"), prompt)
            val multipartBody = okhttp3.MultipartBody.Builder().setType(okhttp3.MultipartBody.FORM)
                .addFormDataPart("model", model)
                .addFormDataPart("prompt", null, promptBody)
                .addFormDataPart("image", "screen.png", imageBody)
                .build()
            val reqBuilder = okhttp3.Request.Builder()
                .url(endpoint)
                .post(multipartBody)
            if (apiKey.isNotBlank()) {
                reqBuilder.addHeader("Authorization", "Bearer $apiKey")
            }
            val request = reqBuilder.build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext ScreenAiStep(done = true, message = "Network/API error: ${response.code()} ${response.message()}")
            }
            val body = response.body()?.string() ?: return@withContext ScreenAiStep(done = true, message = "empty response")
            try {
                return@withContext Gson().fromJson(body, ScreenAiStep::class.java)
            } catch (e: Exception) {
                return@withContext ScreenAiStep(done = true, message = "parse error: $body")
            }
        } catch (e: Exception) {
            return@withContext ScreenAiStep(done = true, message = "exception: ${e.message}")
        }
    }
}
