package org.bxkr.octodiary.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/**
 * Сервис для работы с Gemini API
 * Использует Google AI Studio (бесплатный API)
 */
object GeminiService {
    private const val API_BASE = "https://generativelanguage.googleapis.com/v1beta"
    private const val DEFAULT_MODEL = "gemini-1.5-flash"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * Получить API ключ из SharedPreferences
     * По умолчанию пустой - пользователь должен добавить свой ключ в настройках
     */
    fun getApiKey(context: Context): String {
        val prefs = context.getSharedPreferences("ai_prefs", Context.MODE_PRIVATE)
        return prefs.getString("gemini_api_key", "") ?: ""
    }
    
    fun setApiKey(context: Context, key: String) {
        val prefs = context.getSharedPreferences("ai_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("gemini_api_key", key).apply()
    }
    
    /**
     * Получить модель из настроек
     */
    fun getModel(context: Context): String {
        val prefs = context.getSharedPreferences("main_prefs", Context.MODE_PRIVATE)
        val customModel = prefs.getString("ai_custom_model", "") ?: ""
        return customModel.ifEmpty { "gemini-2.0-flash-exp" }
    }
    
    /**
     * Отправить текстовый запрос к Gemini
     */
    suspend fun sendMessage(
        context: Context,
        prompt: String,
        imageBase64: String? = null,
        history: List<ChatMessage> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey(context)
            if (apiKey.isEmpty()) {
                return@withContext Result.failure(Exception("API ключ не установлен. Добавьте его в настройках AI."))
            }
            
            val model = getModel(context)
            val url = "$API_BASE/models/$model:generateContent?key=$apiKey"
            android.util.Log.d("GeminiService", "sendMessage using model: $model")
            
            val contents = JSONArray()
            
            // Добавляем историю
            history.forEach { msg ->
                contents.put(JSONObject().apply {
                    put("role", if (msg.isUser) "user" else "model")
                    put("parts", JSONArray().put(JSONObject().put("text", msg.content)))
                })
            }
            
            // Добавляем текущее сообщение (с изображением если есть)
            val userMessage = prompt
            
            contents.put(JSONObject().apply {
                put("role", "user")
                val parts = JSONArray()
                
                // Добавляем текст
                parts.put(JSONObject().put("text", userMessage))
                
                // Добавляем изображение если есть
                if (imageBase64 != null) {
                    parts.put(JSONObject().apply {
                        put("inline_data", JSONObject().apply {
                            put("mime_type", "image/png")
                            put("data", imageBase64)
                        })
                    })
                }
                
                put("parts", parts)
            })
            
            val requestBody = JSONObject().apply {
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topK", 40)
                    put("topP", 0.95)
                    put("maxOutputTokens", 2048)
                })
            }
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API Error: ${response.code} - $responseBody"))
            }
            
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.getJSONArray("candidates")
            if (candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                if (parts.length() > 0) {
                    val text = parts.getJSONObject(0).getString("text")
                    return@withContext Result.success(text)
                }
            }
            
            Result.failure(Exception("Нет ответа от API"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Отправить запрос с изображением
     */
    suspend fun sendImageMessage(
        context: Context,
        message: String,
        image: Bitmap,
        systemPrompt: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey(context)
            if (apiKey.isEmpty()) {
                return@withContext Result.failure(Exception("API ключ не установлен"))
            }
            
            val model = getModel(context)
            val url = "$API_BASE/models/$model:generateContent?key=$apiKey"
            android.util.Log.d("GeminiService", "sendImageMessage using model: $model")
            
            // Конвертируем Bitmap в base64
            val stream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val imageBytes = stream.toByteArray()
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            
            val fullMessage = if (systemPrompt.isNotEmpty()) {
                "$systemPrompt\n\n$message"
            } else {
                message
            }
            
            val requestBody = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", fullMessage))
                        put(JSONObject().apply {
                            put("inline_data", JSONObject().apply {
                                put("mime_type", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                    })
                }))
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                    put("topK", 32)
                    put("topP", 1.0)
                    put("maxOutputTokens", 4096)
                })
            }
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API Error: ${response.code}"))
            }
            
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.getJSONArray("candidates")
            if (candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                if (parts.length() > 0) {
                    val text = parts.getJSONObject(0).getString("text")
                    return@withContext Result.success(text)
                }
            }
            
            Result.failure(Exception("Нет ответа от API"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null
)
