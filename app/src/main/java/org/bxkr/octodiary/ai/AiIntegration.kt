package org.bxkr.octodiary.ai

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.bxkr.octodiary.cachePrefs
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.save

data class AiSolution(
    val provider: String,
    val model: String,
    val text: String,
    val createdAt: Long,
    val estimatedSeconds: Int,
)

interface AiProvider {
    suspend fun estimateSolveSeconds(context: Context, taskText: String): Int
    suspend fun solve(context: Context, taskText: String, model: String): String
    fun defaultModels(): List<Pair<String, String>>
}

class DisabledProvider : AiProvider {
    override suspend fun estimateSolveSeconds(context: Context, taskText: String) = 0
    override suspend fun solve(context: Context, taskText: String, model: String) = ""
    override fun defaultModels() = emptyList<Pair<String, String>>()
}

class OpenAiLikeProvider : AiProvider {
    override suspend fun estimateSolveSeconds(context: Context, taskText: String): Int {
        val base = 10
        val per1k = 8
        val tokens = (taskText.length / 4).coerceAtLeast(1)
        return (base + tokens / 1000 * per1k).coerceIn(5, 120)
    }

    override suspend fun solve(context: Context, taskText: String, model: String): String {
        return try {
            val response = OpenAiClient.complete(context, model, taskText)
            // Try parse JSON response
            try {
                val json = com.google.gson.Gson().fromJson(response, Map::class.java)
                val answer = json["final_answer"] as? String ?: response
                val notes = json["notes"] as? String ?: ""
                if (notes.isNotBlank()) "$answer\n\nШаги решения:\n$notes" else answer
            } catch (_: Throwable) {
                response
            }
        } catch (e: Exception) {
            "Ошибка при вызове API: ${e.message}"
        }
    }

    override fun defaultModels() = listOf(
        "gpt-4o" to "Лучший баланс точности и скорости, решает задачи по фото.",
        "gpt-4o-mini" to "Самый дешевый, быстро анализирует фото простых заданий.",
        "gpt-4-turbo" to "Высокая надежность в сложных темах, читает формулы и схемы.",
        "anthropic/claude-3.5-sonnet" to "Очень точный в расчетах и заданиях, читает графики, средний бюджет.",
        "anthropic/claude-3-opus" to "Высший класс для сложной логики, детальный анализ схем и чертежей.",
        "google/gemini-2.0-flash-exp" to "Сильная логика, понимает фото и огромные объемы учебного материала.",
        "google/gemini-flash-1.5" to "Дешевый и очень быстрый, анализирует изображения для быстрых ответов.",
        "x-ai/grok-2-vision-1212" to "Мультимодальный, решает задачи с учетом данных в реальном времени.",
        "meta-llama/llama-3.2-90b-vision-instruct" to "Одна из лучших бесплатных моделей, умеет работать с изображениями.",
        "qwen/qwen-2-vl-72b-instruct" to "Мощный помощник, отлично работает с анализом текста и графиков."
    )
}

class GeminiProvider : AiProvider {
    override suspend fun estimateSolveSeconds(context: Context, taskText: String): Int {
        val base = 8
        val per1k = 6
        val tokens = (taskText.length / 4).coerceAtLeast(1)
        return (base + tokens / 1000 * per1k).coerceIn(5, 120)
    }

    override suspend fun solve(context: Context, taskText: String, model: String): String {
        return try {
            val response = OpenAiClient.complete(context, model, taskText)
            // Try parse JSON response
            try {
                val json = com.google.gson.Gson().fromJson(response, Map::class.java)
                val answer = json["final_answer"] as? String ?: response
                val notes = json["notes"] as? String ?: ""
                if (notes.isNotBlank()) "$answer\n\nШаги решения:\n$notes" else answer
            } catch (_: Throwable) {
                response
            }
        } catch (e: Exception) {
            "Ошибка при вызове Gemini API: ${e.message}"
        }
    }

    override fun defaultModels() = listOf(
        "gemini-2.5-pro" to "Самая мощная модель для сложных задач, кодинга и логики.",
        "gemini-flash-latest" to "Гибридная модель с 1M токенов контекста и thinking budgets.",
        "gemini-1.5-pro" to "Сильная логика, понимает фото и огромные объемы учебного материала.",
        "gemini-1.5-flash" to "Дешевый и очень быстрый, анализирует изображения для быстрых ответов."
    )
}

class MistralProvider : AiProvider {
    override suspend fun estimateSolveSeconds(context: Context, taskText: String): Int {
        val base = 9
        val per1k = 7
        val tokens = (taskText.length / 4).coerceAtLeast(1)
        return (base + tokens / 1000 * per1k).coerceIn(5, 120)
    }

    override suspend fun solve(context: Context, taskText: String, model: String): String {
        return withContext(Dispatchers.Default) {
            delay(200)
            "Автопилот решения пока не подключён. Провайдер: Mistral, модель: $model."
        }
    }

    override fun defaultModels() = listOf(
        "mistral-small" to "Быстрая и экономичная.",
        "mistral-medium" to "Выше качество, разумная скорость."
    )
}

object AiManager {
    fun getProvider(context: Context): AiProvider {
        val enabled = context.mainPrefs.get<String>("ai_provider") ?: "disabled"
        return when (enabled) {
            "disabled" -> DisabledProvider()
            "openai" -> OpenAiLikeProvider()
            "gemini" -> GeminiProvider()
            "openrouter" -> OpenAiLikeProvider()
            "custom" -> OpenAiLikeProvider()
            else -> DisabledProvider()
        }
    }

    fun getSelectedModel(context: Context, provider: AiProvider): String {
        val saved = context.mainPrefs.get<String>("ai_model")
        if (!saved.isNullOrBlank()) return saved
        return provider.defaultModels().firstOrNull()?.first ?: ""
    }
    
    fun isConfigured(context: Context): Boolean {
        val enabled = context.mainPrefs.get<String>("ai_provider") ?: "disabled"
        val hasKey = !context.mainPrefs.get<String>("ai_api_key").isNullOrBlank()
        return enabled != "disabled" && hasKey
    }
}

object AiSolutionStore {
    private fun key(id: Long) = "ai_solutions_$id"

    fun load(context: Context, homeworkEntryStudentId: Long): List<AiSolution> {
        val json = context.cachePrefs.get<String>(key(homeworkEntryStudentId)) ?: return emptyList()
        return try {
            Gson().fromJson(json, object : TypeToken<List<AiSolution>>() {}.type)
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun append(context: Context, homeworkEntryStudentId: Long, solution: AiSolution) {
        val list = load(context, homeworkEntryStudentId).toMutableList()
        list.add(0, solution)
        context.cachePrefs.save(key(homeworkEntryStudentId) to Gson().toJson(list))
    }
}
