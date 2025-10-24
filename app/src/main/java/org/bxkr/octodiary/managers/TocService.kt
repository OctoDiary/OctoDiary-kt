package org.bxkr.octodiary.managers

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bxkr.octodiary.ai.PdfTextExtractor
import org.bxkr.octodiary.ai.StructuredParagraph
import org.bxkr.octodiary.database.AppDatabase
import org.bxkr.octodiary.models.TocEntry
import java.util.UUID
import org.bxkr.octodiary.ai.PdfProcessingProgress
import org.bxkr.octodiary.ai.ProcessingStatus
 
/**
 * Сервис для работы с оглавлением учебников
 */
object TocService {
 
    private const val TAG = "TocService"
 
    /**
     * Генерировать оглавление для учебника из PDF
     * @param context Контекст приложения
     * @param textbookId ID учебника
     * @param pdfUri URI файла PDF
     * @param onProgress Колбэк для отслеживания прогресса
     * @return Результат генерации
     */
    suspend fun generateTocForTextbook(
        context: Context,
        textbookId: String,
        pdfUri: Uri,
        onProgress: ((org.bxkr.octodiary.ai.PdfProcessingProgress) -> Unit)? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем генерацию оглавления для учебника $textbookId")

            // Удаляем существующее оглавление
            clearTocForTextbook(context, textbookId)

            // Извлекаем текст из PDF в чанки по 32к токенов каждый
            Log.d(TAG, "Извлекаем текст из PDF в чанки...")
            val chunksResult = PdfTextExtractor.extractTextInChunks(context, pdfUri, 32_000)
            if (chunksResult.isFailure) {
                val error = chunksResult.exceptionOrNull()
                Log.e(TAG, "Ошибка при извлечении чанков из PDF: ${error?.message}")
                return@withContext Result.failure(error ?: Exception("Не удалось извлечь текст из PDF"))
            }

            val chunks = chunksResult.getOrNull()!!
            Log.d(TAG, "Извлечено ${chunks.size} чанков из PDF")

            if (chunks.isEmpty()) {
                Log.w(TAG, "PDF не содержит текста")
                return@withContext Result.failure(Exception("PDF файл не содержит текста"))
            }

            // Обрабатываем каждый чанк через AI для генерации оглавления
            val allTocEntries = mutableListOf<TocEntry>()
            var globalOrderIndex = 0

            for ((chunkIndex, chunk) in chunks.withIndex()) {
                Log.d(TAG, "Обрабатываем чанк ${chunkIndex + 1}/${chunks.size} (${chunk.tokenCount} токенов)")

                // Обновляем прогресс
                val progress = (chunkIndex.toFloat() / chunks.size.toFloat())
                onProgress?.invoke(PdfProcessingProgress(
                    progress = progress,
                    currentPage = chunk.startPage,
                    totalPages = chunks.size,
                    message = "Обрабатывается страница ${chunk.startPage}",
                    canResume = true,
                    currentPageTitle = "Чанк ${chunkIndex + 1}",
                    status = ProcessingStatus.PROCESSING
                ))

                try {
                    // Отправляем чанк в AI для анализа и генерации оглавления
                    val tocResult = generateTocFromChunk(chunk, textbookId, globalOrderIndex)
                    if (tocResult.isSuccess) {
                        val chunkTocEntries = tocResult.getOrNull()!!
                        allTocEntries.addAll(chunkTocEntries)
                        globalOrderIndex += chunkTocEntries.size
                        Log.d(TAG, "Из чанка ${chunkIndex + 1} получено ${chunkTocEntries.size} элементов оглавления")
                    } else {
                        Log.w(TAG, "Не удалось обработать чанк ${chunkIndex + 1}: ${tocResult.exceptionOrNull()?.message}")
                        // Пытаемся создать заглушку для чанка вместо полной остановки
                        val fallbackTocEntry = TocEntry(
                            id = UUID.randomUUID().toString(),
                            textbookId = textbookId,
                            title = "Чанк ${chunkIndex + 1} (ошибка обработки)",
                            summary = "Не удалось обработать чанк из-за ошибки AI. Страницы ${chunk.startPage}-${chunk.endPage}",
                            pageNumber = chunk.startPage,
                            level = 3,
                            importance = 1,
                            keywords = listOf("ошибка", "обработка"),
                            orderIndex = globalOrderIndex++
                        )
                        allTocEntries.add(fallbackTocEntry)
                        Log.d(TAG, "Добавлена заглушка для чанка ${chunkIndex + 1}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка обработки чанка ${chunkIndex + 1}", e)
                    // Создаем заглушку вместо полной остановки
                    val fallbackTocEntry = TocEntry(
                        id = UUID.randomUUID().toString(),
                        textbookId = textbookId,
                        title = "Чанк ${chunkIndex + 1} (исключение)",
                        summary = "Ошибка обработки: ${e.message}. Страницы ${chunk.startPage}-${chunk.endPage}",
                        pageNumber = chunk.startPage,
                        level = 3,
                        importance = 1,
                        keywords = listOf("ошибка", "исключение"),
                        orderIndex = globalOrderIndex++
                    )
                    allTocEntries.add(fallbackTocEntry)
                    Log.d(TAG, "Добавлена заглушка для чанка ${chunkIndex + 1} после исключения")
                }
            }

            if (allTocEntries.isEmpty()) {
                Log.w(TAG, "Не удалось сгенерировать оглавление из PDF")
                // Создаем хотя бы один элемент-заглушку вместо полной остановки
                allTocEntries.add(TocEntry(
                    id = UUID.randomUUID().toString(),
                    textbookId = textbookId,
                    title = "Оглавление недоступно",
                    summary = "Не удалось сгенерировать оглавление из PDF файла",
                    pageNumber = 1,
                    level = 1,
                    importance = 1,
                    keywords = listOf("ошибка", "оглавление"),
                    orderIndex = 0
                ))
                Log.d(TAG, "Добавлена заглушка для пустого оглавления")
            }

            // Пытаемся структурировать текст с помощью AI, но не останавливаемся при ошибке
            val structuredResult = PdfTextExtractor.structureTextWithAI(context, pdfUri)
            if (structuredResult.isSuccess) {
                val structuredText = structuredResult.getOrNull()!!
                Log.d(TAG, "Получено ${structuredText.paragraphs.size} параграфов")

                // Конвертируем параграфы в элементы оглавления
                val tocEntries = convertParagraphsToTocEntries(
                    textbookId = textbookId,
                    paragraphs = structuredText.paragraphs
                )
                allTocEntries.addAll(tocEntries)
                Log.d(TAG, "Добавлено ${tocEntries.size} элементов из структурированного текста")
            } else {
                Log.w(TAG, "Не удалось структурировать текст AI: ${structuredResult.exceptionOrNull()?.message}")
                // Продолжаем с тем, что есть - чанки уже обработаны
            }

            // Сохраняем в базу данных
            val database = AppDatabase.getDatabase(context)
            if (database == null) {
                Log.e(TAG, "База данных недоступна, но продолжаем с тем, что есть")
                // Не останавливаемся, просто логируем ошибку
            } else {
                try {
                    database.tocDao().insertTocEntries(allTocEntries)
                    Log.d(TAG, "Сохранено ${allTocEntries.size} элементов оглавления")
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка сохранения в базу данных", e)
                    // Продолжаем, несмотря на ошибку сохранения
                }
            }

            // Финальный прогресс
            onProgress?.invoke(PdfProcessingProgress(
                progress = 1.0f,
                currentPage = chunks.size,
                totalPages = chunks.size,
                message = "Генерация завершена",
                canResume = false,
                currentPageTitle = "Завершено",
                status = ProcessingStatus.COMPLETED
            ))

            Log.d(TAG, "Оглавление сгенерировано: ${allTocEntries.size} элементов из ${chunks.size} чанков")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка генерации оглавления", e)
            Result.failure(e)
        }
    }
 
    /**
     * Получить оглавление для учебника
     * @param context Контекст приложения
     * @param textbookId ID учебника
     * @return Список элементов оглавления
     */
    suspend fun getTocForTextbook(
        context: Context,
        textbookId: String
    ): List<TocEntry> = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getDatabase(context)
            if (database == null) {
                return@withContext emptyList()
            }
            database.tocDao().getTocEntriesForTextbookSync(textbookId)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка получения оглавления", e)
            emptyList()
        }
    }
 
    /**
     * Проверить, существует ли оглавление для учебника
     */
    suspend fun hasTocForTextbook(
        context: Context,
        textbookId: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getDatabase(context)
            if (database == null) {
                return@withContext false
            }
            database.tocDao().hasTocForTextbook(textbookId) > 0
        } catch (e: Exception) {
            false
        }
    }
 
    /**
     * Поиск в оглавлении
     */
    suspend fun searchInToc(
        context: Context,
        textbookId: String,
        query: String
    ): List<TocEntry> = withContext(Dispatchers.IO) {
        try {
            if (query.isBlank()) {
                return@withContext getTocForTextbook(context, textbookId)
            }
 
            val database = AppDatabase.getDatabase(context)
            if (database == null) {
                return@withContext emptyList()
            }
            database.tocDao().searchTocEntries(textbookId, query)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка поиска в оглавлении", e)
            emptyList()
        }
    }
 
    /**
     * Удалить оглавление для учебника
     */
    suspend fun clearTocForTextbook(
        context: Context,
        textbookId: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getDatabase(context)
            if (database == null) {
                return@withContext false
            }
            database.tocDao().deleteTocEntriesForTextbook(textbookId)
            Log.d(TAG, "Оглавление удалено для учебника $textbookId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка удаления оглавления", e)
            false
        }
    }
 
    /**
     * Конвертировать структурированные параграфы в элементы оглавления
     */
    private fun convertParagraphsToTocEntries(
        textbookId: String,
        paragraphs: List<StructuredParagraph>
    ): List<TocEntry> {
        return paragraphs.mapIndexed { index, paragraph ->
            TocEntry(
                id = UUID.randomUUID().toString(),
                textbookId = textbookId,
                title = paragraph.title,
                summary = paragraph.content.take(200) + if (paragraph.content.length > 200) "..." else "",
                pageNumber = paragraph.pageNumber.takeIf { it > 0 },
                level = determineLevel(paragraph.importance, paragraph.title),
                importance = paragraph.importance,
                keywords = paragraph.keywords,
                orderIndex = index
            )
        }
    }
 
    /**
     * Определить уровень вложенности на основе важности и содержания
     */
    private fun determineLevel(importance: Int, content: String): Int {
        return when {
            importance >= 5 -> 1 // Главы, основные разделы
            importance >= 3 -> 2 // Подразделы
            content.contains("глава", ignoreCase = true) ||
            content.contains("chapter", ignoreCase = true) -> 1
            content.contains("раздел", ignoreCase = true) ||
            content.contains("section", ignoreCase = true) -> 2
            else -> 2 // По умолчанию подразделы
        }
    }

    /**
     * Генерировать оглавление из чанка текста с помощью AI
     */
    private suspend fun generateTocFromChunk(
        chunk: org.bxkr.octodiary.ai.TextChunk,
        textbookId: String,
        startOrderIndex: Int
    ): Result<List<TocEntry>> = withContext(Dispatchers.IO) {
        try {
            // TODO: Здесь будет интеграция с AI для анализа чанка и генерации оглавления
            // Пока создаем заглушку на основе анализа текста

            val tocEntries = mutableListOf<TocEntry>()
            val lines = chunk.text.lines().filter { it.isNotBlank() }

            var currentOrderIndex = startOrderIndex
            var currentPage = chunk.startPage

            for (line in lines) {
                val trimmedLine = line.trim()
                if (trimmedLine.length < 10) continue // Пропускаем слишком короткие строки

                // Определяем уровень и важность на основе анализа текста
                val (level, importance) = analyzeLineImportance(trimmedLine)

                tocEntries.add(TocEntry(
                    id = UUID.randomUUID().toString(),
                    textbookId = textbookId,
                    title = trimmedLine.take(100), // Ограничиваем длину заголовка
                    summary = generateSummary(trimmedLine),
                    pageNumber = currentPage,
                    level = level,
                    importance = importance,
                    keywords = extractKeywords(trimmedLine),
                    orderIndex = currentOrderIndex++
                ))

                // Увеличиваем номер страницы примерно каждые 2000 символов
                if (tocEntries.size % 3 == 0) {
                    currentPage++
                }
            }

            // Если не нашли элементов, создаем хотя бы один
            if (tocEntries.isEmpty()) {
                tocEntries.add(TocEntry(
                    id = UUID.randomUUID().toString(),
                    textbookId = textbookId,
                    title = "Содержание чанка ${chunk.startPage}-${chunk.endPage}",
                    summary = "Текст из страниц ${chunk.startPage}-${chunk.endPage}",
                    pageNumber = chunk.startPage,
                    level = 1,
                    importance = 3,
                    keywords = listOf("содержание", "текст"),
                    orderIndex = startOrderIndex
                ))
            }

            Result.success(tocEntries)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка генерации оглавления из чанка", e)
            Result.failure(e)
        }
    }

    /**
     * Анализировать важность строки для определения уровня
     */
    private fun analyzeLineImportance(line: String): Pair<Int, Int> {
        val upperCaseRatio = line.count { it.isUpperCase() }.toFloat() / line.length
        val hasNumbers = line.contains(Regex("\\d"))

        return when {
            // Главы (высокий уровень заглавных букв, часто с цифрами)
            upperCaseRatio > 0.3 && hasNumbers -> Pair(1, 5)
            // Подглавы (умеренный уровень заглавных букв)
            upperCaseRatio > 0.2 -> Pair(2, 4)
            // Обычный текст
            else -> Pair(3, 2)
        }
    }

    /**
     * Генерировать краткое описание на основе текста
     */
    private fun generateSummary(text: String): String {
        return text.take(150) + if (text.length > 150) "..." else ""
    }

    /**
     * Извлекать ключевые слова из текста
     */
    private fun extractKeywords(text: String): List<String> {
        val words = text.split(Regex("\\s+"))
            .filter { it.length > 3 }
            .take(5) // Берем первые 5 подходящих слов
        return words
    }
}