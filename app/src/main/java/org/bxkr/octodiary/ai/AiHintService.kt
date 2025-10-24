package org.bxkr.octodiary.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bxkr.octodiary.ai.GeminiService
import java.io.ByteArrayOutputStream
import android.util.Base64

/**
 * AI помощник для подсказок (без автоматического решения)
 */
object AiHintService {
    
    /**
     * Получить подсказку от AI по скриншоту
     */
    suspend fun getHint(
        context: Context,
        url: String,
        screenshot: Bitmap
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d("AiHintService", "Отправляю скриншот AI для подсказки: ${screenshot.width}x${screenshot.height}")
            
            val base64Image = bitmapToBase64(screenshot)
            val prompt = buildHintPrompt(url)
            
            val result = GeminiService.sendMessage(
                context = context,
                prompt = prompt,
                imageBase64 = base64Image
            )
            
            result.onSuccess { hint: String ->
                Log.d("AiHintService", "Получена подсказка от AI (${hint.length} символов)")
            }.onFailure { error: Throwable ->
                Log.e("AiHintService", "Ошибка получения подсказки", error)
            }
            
            result
        } catch (e: Exception) {
            Log.e("AiHintService", "Ошибка AiHintService", e)
            Result.failure(e)
        }
    }
    
    /**
     * Создать промпт для подсказки
     */
    private fun buildHintPrompt(url: String): String {
        return """
            Ты - AI помощник для решения заданий. Твоя задача - дать ПОДСКАЗКУ, а не решить полностью.
            
            URL страницы: $url
            
            **Твоя задача**:
            1. Посмотри на скриншот и пойми, какое задание перед учеником
            2. Определи тип задания: тест, задача, упражнение, вопрос
            3. Дай КРАТКУЮ ПОДСКАЗКУ как подойти к решению (не решай полностью!)
            4. Укажи ключевые формулы/правила если нужно
            5. Намекни на первый шаг решения
            
            **ВАЖНО**:
            - НЕ решай задание полностью
            - НЕ давай готовый ответ
            - Дай направление мысли, подсказку, наводящий вопрос
            - Будь кратким (2-4 предложения)
            
            **Примеры хороших подсказок**:
            - "Это задача на кинематику. Вспомни формулу v = v₀ + at. Какая начальная скорость?"
            - "Здесь нужно применить теорему Пифагора. Посмотри на треугольник - что известно?"
            - "Это задача на проценты. Составь пропорцию: x/100 = что/что?"
            - "Вспомни правило согласования времён в английском. Главное предложение в прошедшем времени..."
            
            **Примеры плохих подсказок** (так НЕ делай):
            - "Ответ: 42" ❌
            - "1.2, потом 3, потом 5" ❌
            - "Решение: v = 12/10 = 1.2 секунды" ❌
            
            Верни ТОЛЬКО текст подсказки на русском, без форматирования.
        """.trimIndent()
    }
    
    /**
     * Конвертировать Bitmap в Base64
     */
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
