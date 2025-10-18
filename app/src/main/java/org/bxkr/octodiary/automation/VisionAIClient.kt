package org.bxkr.octodiary.automation

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/**
 * Клиент для Vision-моделей (GPT-4o, Claude 3.5, Gemini 2.5)
 * Анализирует скриншот и возвращает следующее действие
 */
object VisionAIClient {
    private val TAG = "VisionAIClient"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Анализ скриншота и получение следующего действия
     * 
     * @param context Android context
     * @param screenshot Скриншот WebView
     * @param task Описание задачи
     * @param history История предыдущих действий
     * @return Следующее действие или null при ошибке
     */
    suspend fun getNextAction(
        context: Context,
        screenshot: Bitmap,
        task: String,
        history: List<String> = emptyList()
    ): AutomationAction? = withContext(Dispatchers.IO) {
        try {
            val provider = context.mainPrefs.get<String>("automation_provider") ?: return@withContext null
            val apiKey = context.mainPrefs.get<String>("automation_api_key") ?: return@withContext null
            val model = context.mainPrefs.get<String>("automation_model") ?: "gpt-4o"

            val base64Image = bitmapToBase64(screenshot)
            val prompt = buildPrompt(task, history)

            when (provider) {
                "openai" -> callOpenAI(apiKey, model, prompt, base64Image)
                "anthropic" -> callAnthropic(apiKey, model, prompt, base64Image)
                "google" -> callGemini(apiKey, model, prompt, base64Image)
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting next action", e)
            null
        }
    }

    private fun buildPrompt(task: String, history: List<String>): String {
        return buildString {
            appendLine("Ты эксперт по автоматизации веб-интерфейсов. Проанализируй скриншот и определи следующее действие.")
            appendLine()
            appendLine("ЗАДАЧА: $task")
            appendLine()
            if (history.isNotEmpty()) {
                appendLine("ИСТОРИЯ ДЕЙСТВИЙ:")
                history.forEach { appendLine("- $it") }
                appendLine()
            }
            appendLine("ДОСТУПНЫЕ ДЕЙСТВИЯ:")
            appendLine("1. click(x, y) или click(text=\"текст кнопки\")")
            appendLine("2. type(text=\"текст\", selector=\"CSS селектор\")")
            appendLine("3. scroll(direction=\"up/down/left/right\", amount=300)")
            appendLine("4. swipe(fromX, fromY, toX, toY)")
            appendLine("5. drag(fromX, fromY, toX, toY)")
            appendLine("6. wait(ms)")
            appendLine("7. finish(message=\"результат\")")
            appendLine()
            appendLine("ВЕРНИ ОТВЕТ В JSON ФОРМАТЕ:")
            appendLine("{")
            appendLine("  \"action\": \"click|type|scroll|swipe|drag|wait|finish\",")
            appendLine("  \"reasoning\": \"почему выбрано это действие\",")
            appendLine("  \"params\": {")
            appendLine("    /* параметры действия */")
            appendLine("  }")
            appendLine("}")
            appendLine()
            appendLine("ВАЖНО: Отвечай ТОЛЬКО JSON, без дополнительного текста!")
        }
    }

    private suspend fun callOpenAI(
        apiKey: String,
        model: String,
        prompt: String,
        base64Image: String
    ): AutomationAction? = withContext(Dispatchers.IO) {
        try {
            val requestBody = JsonObject().apply {
                addProperty("model", model)
                add("messages", Gson().toJsonTree(listOf(
                    mapOf(
                        "role" to "user",
                        "content" to listOf(
                            mapOf("type" to "text", "text" to prompt),
                            mapOf(
                                "type" to "image_url",
                                "image_url" to mapOf("url" to "data:image/jpeg;base64,$base64Image")
                            )
                        )
                    )
                )))
                addProperty("max_tokens", 500)
                addProperty("temperature", 0.7)
            }

            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "OpenAI API error: ${response.code}")
                return@withContext null
            }

            val responseBody = response.body?.string() ?: return@withContext null
            parseActionFromResponse(responseBody)
        } catch (e: Exception) {
            Log.e(TAG, "Error calling OpenAI", e)
            null
        }
    }

    private suspend fun callAnthropic(
        apiKey: String,
        model: String,
        prompt: String,
        base64Image: String
    ): AutomationAction? = withContext(Dispatchers.IO) {
        try {
            val requestBody = JsonObject().apply {
                addProperty("model", model)
                addProperty("max_tokens", 500)
                add("messages", Gson().toJsonTree(listOf(
                    mapOf(
                        "role" to "user",
                        "content" to listOf(
                            mapOf(
                                "type" to "image",
                                "source" to mapOf(
                                    "type" to "base64",
                                    "media_type" to "image/jpeg",
                                    "data" to base64Image
                                )
                            ),
                            mapOf("type" to "text", "text" to prompt)
                        )
                    )
                )))
            }

            val request = Request.Builder()
                .url("https://api.anthropic.com/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("Content-Type", "application/json")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "Anthropic API error: ${response.code}")
                return@withContext null
            }

            val responseBody = response.body?.string() ?: return@withContext null
            parseActionFromResponse(responseBody)
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Anthropic", e)
            null
        }
    }

    private suspend fun callGemini(
        apiKey: String,
        model: String,
        prompt: String,
        base64Image: String
    ): AutomationAction? = withContext(Dispatchers.IO) {
        try {
            val requestBody = JsonObject().apply {
                add("contents", Gson().toJsonTree(listOf(
                    mapOf(
                        "parts" to listOf(
                            mapOf("text" to prompt),
                            mapOf(
                                "inline_data" to mapOf(
                                    "mime_type" to "image/jpeg",
                                    "data" to base64Image
                                )
                            )
                        )
                    )
                )))
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1/models/$model:generateContent?key=$apiKey")
                .header("Content-Type", "application/json")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error: ${response.code}")
                return@withContext null
            }

            val responseBody = response.body?.string() ?: return@withContext null
            parseActionFromResponse(responseBody)
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini", e)
            null
        }
    }

    private fun parseActionFromResponse(responseBody: String): AutomationAction? {
        return try {
            val json = Gson().fromJson(responseBody, JsonObject::class.java)
            
            // Извлекаем текст ответа в зависимости от провайдера
            val content = when {
                json.has("choices") -> {
                    // OpenAI format
                    json.getAsJsonArray("choices")
                        .firstOrNull()
                        ?.asJsonObject
                        ?.getAsJsonObject("message")
                        ?.get("content")
                        ?.asString
                }
                json.has("content") -> {
                    // Anthropic format
                    json.getAsJsonArray("content")
                        .firstOrNull()
                        ?.asJsonObject
                        ?.get("text")
                        ?.asString
                }
                json.has("candidates") -> {
                    // Gemini format
                    json.getAsJsonArray("candidates")
                        .firstOrNull()
                        ?.asJsonObject
                        ?.getAsJsonObject("content")
                        ?.getAsJsonArray("parts")
                        ?.firstOrNull()
                        ?.asJsonObject
                        ?.get("text")
                        ?.asString
                }
                else -> null
            } ?: return null

            // Извлекаем JSON из ответа (может быть обернут в markdown code block)
            val jsonText = content
                .substringAfter("```json", "")
                .substringAfter("```", content)
                .substringBefore("```", content)
                .trim()

            val actionJson = Gson().fromJson(jsonText, JsonObject::class.java)
            val actionType = actionJson.get("action")?.asString ?: return null
            val params = actionJson.getAsJsonObject("params")

            when (actionType) {
                "click" -> {
                    val x = params?.get("x")?.asInt
                    val y = params?.get("y")?.asInt
                    val text = params?.get("text")?.asString
                    val selector = params?.get("selector")?.asString
                    AutomationAction.Click(x, y, text, selector)
                }
                "type" -> {
                    val text = params?.get("text")?.asString ?: return null
                    val selector = params?.get("selector")?.asString
                    AutomationAction.Type(text, selector)
                }
                "scroll" -> {
                    val dirStr = params?.get("direction")?.asString ?: "down"
                    val direction = AutomationAction.Scroll.Direction.valueOf(dirStr.uppercase())
                    val amount = params?.get("amount")?.asInt ?: 300
                    AutomationAction.Scroll(direction, amount)
                }
                "swipe" -> {
                    val fromX = params?.get("fromX")?.asInt ?: return null
                    val fromY = params?.get("fromY")?.asInt ?: return null
                    val toX = params?.get("toX")?.asInt ?: return null
                    val toY = params?.get("toY")?.asInt ?: return null
                    val duration = params?.get("durationMs")?.asLong ?: 300L
                    AutomationAction.Swipe(fromX, fromY, toX, toY, duration)
                }
                "drag" -> {
                    val fromX = params?.get("fromX")?.asInt ?: return null
                    val fromY = params?.get("fromY")?.asInt ?: return null
                    val toX = params?.get("toX")?.asInt ?: return null
                    val toY = params?.get("toY")?.asInt ?: return null
                    AutomationAction.Drag(fromX, fromY, toX, toY)
                }
                "wait" -> {
                    val ms = params?.get("ms")?.asLong ?: 1000L
                    AutomationAction.Wait(ms)
                }
                "finish" -> {
                    val message = params?.get("message")?.asString ?: "Готово"
                    AutomationAction.Finish(message)
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing action from response", e)
            null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
