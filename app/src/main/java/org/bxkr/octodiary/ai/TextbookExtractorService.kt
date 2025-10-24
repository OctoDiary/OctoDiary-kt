package org.bxkr.octodiary.ai

import android.content.Context
import java.util.Date
import org.bxkr.octodiary.database.AppDatabase
import org.bxkr.octodiary.database.entity.TextbookExtractEntity
import org.json.JSONArray

/**
 * Сервис для извлечения вопросов, упражнений и заданий из текста учебников с помощью AI
 */
object TextbookExtractorService {

    /**
     * Извлечь элементы из текста параграфа
     */
    suspend fun extractElements(
        context: Context,
        paragraphId: Long,
        text: String,
        subject: String = ""
    ): Result<List<TextbookExtractEntity>> {
        return try {
            val prompt = """
                Проанализируй текст учебника и извлеки все вопросы, упражнения и задания.
                Верни результат в формате JSON массива объектов с полями:
                - type: "question" | "exercise" | "assignment"
                - content: текст элемента
                - context: краткий контекст вокруг элемента (1-2 предложения)
                - difficulty: оценка сложности 1-5 (опционально)
                - estimatedTime: предполагаемое время выполнения в минутах (опционально)
 
                Текст учебника:
                $text
 
                ${if (subject.isNotEmpty()) "Предмет: $subject" else ""}
 
                Верни только JSON массив, без дополнительного текста.
            """.trimIndent()
 
            val result = GeminiService.sendMessage(context, prompt)
            return if (result.isSuccess) {
                val response = result.getOrThrow()
                val extracts = parseExtractsFromResponse(response, paragraphId)
                saveExtracts(context, extracts)
                Result.success(extracts)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Неизвестная ошибка"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Извлечь элементы из нескольких параграфов
     */
    suspend fun extractFromMultipleParagraphs(
        context: Context,
        paragraphs: List<Pair<Long, String>>,
        subject: String = ""
    ): Result<List<TextbookExtractEntity>> {
        val allExtracts = mutableListOf<TextbookExtractEntity>()
 
        for ((paragraphId, text) in paragraphs) {
            val result = extractElements(context, paragraphId, text, subject)
            if (result.isSuccess) {
                allExtracts.addAll(result.getOrThrow())
            } else {
                // Продолжаем с другими параграфами даже если один не удался
                continue
            }
        }
 
        return Result.success(allExtracts)
    }

    /**
     * Обновить статус выполнения элемента
     */
    suspend fun updateCompletionStatus(
        context: Context,
        extractId: Long,
        completed: Boolean
    ) {
        val db = AppDatabase.getDatabase(context)!!
        val dao = db.textbookExtractDao()
 
        val currentTime = Date()
        dao.updateCompletionStatus(
            id = extractId,
            completed = completed,
            completedAt = if (completed) currentTime else null,
            updatedAt = currentTime
        )
    }

    /**
     * Получить все извлечённые элементы для параграфа
     */
    suspend fun getExtractsForParagraph(context: Context, paragraphId: Long): List<TextbookExtractEntity> {
        val db = AppDatabase.getDatabase(context)!!
        val dao = db.textbookExtractDao()
        return dao.getByParagraphId(paragraphId)
    }

    /**
     * Удалить все извлечё��ные элементы для параграфа
     */
    suspend fun deleteExtractsForParagraph(context: Context, paragraphId: Long) {
        val db = AppDatabase.getDatabase(context)!!
        val dao = db.textbookExtractDao()
        dao.deleteByParagraphId(paragraphId)
    }

    /**
     * Разобрать ответ AI в список TextbookExtractEntity
     */
    private fun parseExtractsFromResponse(
        response: String,
        paragraphId: Long
    ): List<TextbookExtractEntity> {
        return try {
            val jsonArray = JSONArray(response)
            val extracts = mutableListOf<TextbookExtractEntity>()
 
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
 
                val type = jsonObject.getString("type")
                val content = jsonObject.getString("content")
                val context = jsonObject.optString("context", "")
                val difficulty = jsonObject.optInt("difficulty", -1).takeIf { it in 1..5 }
                val estimatedTime = jsonObject.optInt("estimatedTime", -1).takeIf { it > 0 }
 
                val extract = TextbookExtractEntity(
                    paragraphId = paragraphId,
                    extractType = type,
                    content = content,
                    context = context,
                    difficulty = difficulty,
                    estimatedTime = estimatedTime
                )
 
                extracts.add(extract)
            }
 
            extracts
        } catch (e: Exception) {
            // Если парсинг JSON не удался, пытаемся извлечь вручную
            emptyList()
        }
    }

    /**
     * Сохранить извлечённые элементы в базу данных
     */
    private suspend fun saveExtracts(context: Context, extracts: List<TextbookExtractEntity>) {
        if (extracts.isNotEmpty()) {
            val db = AppDatabase.getDatabase(context)!!
            val dao = db.textbookExtractDao()
            dao.insertAll(extracts)
        }
    }
}