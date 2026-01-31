package org.bxkr.octodiary.ai


import androidx.compose.material.icons.Icons
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.google.gson.Gson
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import org.bxkr.octodiary.ai.ProcessingStatus

/**
 * Данные о чанке текста
 */
data class TextChunk(
    val text: String,
    val startPage: Int,
    val endPage: Int,
    val tokenCount: Int
)

/**
 * Подсчитать количество токенов в тексте
 * Используем простую оценку: 1 токен ≈ 4 символа для большинства европейских языков
 * Для более точного подсчёта можно использовать специализированные токенизаторы
 */
fun countTokens(text: String): Int {
    return (text.length / 4.0).toInt().coerceAtLeast(1)
}

/**
 * Разбить текст на чанки по максимальному количеству токенов
 * @param text Исходный текст
 * @param maxTokens Максимальное количество токенов в чанке (по умолчанию 32к)
 * @return Список чанков
 */
fun splitTextIntoChunks(text: String, maxTokens: Int = 32_000): List<TextChunk> {
    val chunks = mutableListOf<TextChunk>()
    val words = text.split("\\s+".toRegex())

    var currentChunkWords = mutableListOf<String>()
    var currentTokenCount = 0
    var startIndex = 0

    for ((index, word) in words.withIndex()) {
        val wordTokens = countTokens(word) + 1 // +1 за пробел

        if (currentTokenCount + wordTokens > maxTokens && currentChunkWords.isNotEmpty()) {
            // Создаём чанк из накопленных слов
            val chunkText = currentChunkWords.joinToString(" ")
            val tokenCount = countTokens(chunkText)
            chunks.add(TextChunk(
                text = chunkText,
                startPage = -1, // Неизвестно без информации о страницах
                endPage = -1,
                tokenCount = tokenCount
            ))

            // Начинаем новый чанк
            currentChunkWords = mutableListOf()
            currentTokenCount = 0
            startIndex = index
        }

        currentChunkWords.add(word)
        currentTokenCount += wordTokens
    }

    // Добавляем последний чанк
    if (currentChunkWords.isNotEmpty()) {
        val chunkText = currentChunkWords.joinToString(" ")
        val tokenCount = countTokens(chunkText)
        chunks.add(TextChunk(
            text = chunkText,
            startPage = -1,
            endPage = -1,
            tokenCount = tokenCount
        ))
    }

    return chunks
}

/**
 * Разбить текст PDF на чанки с учётом страниц
 * @param text Полный текст PDF
 * @param maxTokens Максимальное количество токенов в чанке
 * @param pageMarkers Маркеры страниц в тексте (опционально)
 * @return Список чанков с информацией о страницах
 */
fun splitTextIntoChunksWithPages(
    text: String,
    maxTokens: Int = 32_000,
    pageMarkers: List<PageMarker> = emptyList()
): List<TextChunk> {
    // Если нет маркеров страниц, используем простое разбиение
    if (pageMarkers.isEmpty()) {
        return splitTextIntoChunks(text, maxTokens)
    }

    val chunks = mutableListOf<TextChunk>()

    // Сортируем маркеры по позиции
    val sortedMarkers = pageMarkers.sortedBy { it.position }

    var currentStartPage = 1
    var currentText = ""
    var currentTokens = 0

    for (marker in sortedMarkers) {
        val pageText = text.substring(marker.startPosition, marker.endPosition)
        val pageTokens = countTokens(pageText)

        // Если добавление страницы превысит лимит токенов
        if (currentTokens + pageTokens > maxTokens && currentText.isNotEmpty()) {
            chunks.add(TextChunk(
                text = currentText.trim(),
                startPage = currentStartPage,
                endPage = marker.pageNumber - 1,
                tokenCount = currentTokens
            ))

            // Начинаем новый чанк
            currentStartPage = marker.pageNumber
            currentText = ""
            currentTokens = 0
        }

        currentText += pageText
        currentTokens += pageTokens
    }

    // Добавляем последний чанк
    if (currentText.isNotEmpty()) {
        chunks.add(TextChunk(
            text = currentText.trim(),
            startPage = currentStartPage,
            endPage = sortedMarkers.last().pageNumber,
            tokenCount = currentTokens
        ))
    }

    return chunks
}

/**
 * Маркер страницы в тексте
 */
data class PageMarker(
    val pageNumber: Int,
    val startPosition: Int,
    val endPosition: Int,
    val position: Int = startPosition
)

/**
 * Структурированный параграф текста
 */
data class StructuredParagraph(
    val title: String,
    val content: String,
    val pageNumber: Int,
    val importance: Int, // 1-5, где 5 - наиболее важный
    val keywords: List<String>
)

/**
 * Результат структурирования текста
 */
data class StructuredText(
    val paragraphs: List<StructuredParagraph>,
    val summary: String,
    val totalPages: Int,
    val totalTokens: Int
)


/**
 * Состояние обработки PDF
 */
data class PdfProcessingState(
    val uri: String,
    val totalPages: Int,
    val processedPages: MutableMap<Int, ProcessedPage> = mutableMapOf(),
    var isPaused: Boolean = false,
    var lastProcessedPage: Int = 0,
    val startTime: Long = System.currentTimeMillis()
)

/**
 * Обработанная страница
 */
data class ProcessedPage(
    val pageNumber: Int,
    val title: String,
    val text: String,
    val processedAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)

/**
 * Сервис для извлечения текста из PDF файлов
 */
object PdfTextExtractor {

    private const val TAG = "PdfTextExtractor"

    // Для расчета ETA
    private var processingStartTime: Long = 0
    private var pagesProcessed: Int = 0

    // Состояние обработки
    private var currentProcessingState: PdfProcessingState? = null
    private const val MAX_RETRIES = 7
    private const val RETRY_DELAY_MS = 10_000L // 10 секунд

    init {
        // Инициализация iText7 для Android
        try {
            android.util.Log.d(TAG, "iText7 инициализирован успешно")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Ошибка инициализации: ${e.message}")
        }
    }

    /**
     * Извлечь текст из PDF файла
     * @param context Контекст приложения
     * @param uri URI файла PDF
     * @return Результат с извлеченным текстом или ошибкой
     */
    suspend fun extractText(
        context: Context,
        uri: Uri
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("PdfTextExtractor", "Начало извлечения текста из PDF")
            android.util.Log.d("PdfTextExtractor", "URI: $uri")

            // Открываем поток для чтения файла
            android.util.Log.d("PdfTextExtractor", "Открываем InputStream...")
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Не удалось открыть файл"))
            android.util.Log.d("PdfTextExtractor", "InputStream открыт успешно")

            inputStream.use { stream ->
                android.util.Log.d(TAG, "PDF поток открыт, загружаем документ")

                try {
                    // Загружаем PDF документ с помощью iText7
                    android.util.Log.d(TAG, "Вызываем PdfReader()...")
                    val reader = PdfReader(stream)
                    val document = PdfDocument(reader)
                    android.util.Log.d(TAG, "PDF документ загружен успешно, страниц: ${document.numberOfPages}")

                    // Извлекаем текст из всех страниц
                    val textBuilder = StringBuilder()
                    for (pageNum in 1..document.numberOfPages) {
                        val page = document.getPage(pageNum)
                        val text = PdfTextExtractor.getTextFromPage(page)
                        textBuilder.append(text).append("\n\n")
                    }
                    val text = textBuilder.toString().trim()
                    android.util.Log.d(TAG, "Текст извлечен, длина: ${text.length} символов")

                    document.close()
                    reader.close()
                    Result.success(text)
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Ошибка при загрузке документа: ${e.javaClass.simpleName}: ${e.message}", e)
                    throw e
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PdfTextExtractor", "Ошибка при извлечении текста из PDF: ${e.javaClass.simpleName}: ${e.message}", e)
            android.util.Log.e("PdfTextExtractor", "StackTrace:", e)
            Result.failure(e)
        }
    }

    /**
     * Извлечь текст из PDF файла с указанием диапазона страниц
     * @param context Контекст приложения
     * @param uri URI файла PDF
     * @param startPage Начальная страница (1-based)
     * @param endPage Конечная страница (1-based, включительно)
     * @return Результат с извлеченным текстом или ошибкой
     */
    suspend fun extractTextFromPages(
        context: Context,
        uri: Uri,
        startPage: Int = 1,
        endPage: Int = Int.MAX_VALUE
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("PdfTextExtractor", "Извлечение текста из страниц $startPage-$endPage")

            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Не удалось открыть файл"))

            inputStream.use { stream ->
                val reader = PdfReader(stream)
                val document = PdfDocument(reader)

                // Устанавливаем диапазон страниц
                val actualEndPage = minOf(endPage, document.numberOfPages)
                val actualStartPage = maxOf(1, startPage)

                val textBuilder = StringBuilder()
                for (pageNum in actualStartPage..actualEndPage) {
                    val page = document.getPage(pageNum)
                    val text = PdfTextExtractor.getTextFromPage(page)
                    textBuilder.append(text).append("\n\n")
                }
                val text = textBuilder.toString().trim()

                android.util.Log.d(TAG, "Текст извлечен из страниц $actualStartPage-$actualEndPage, длина: ${text.length}")

                document.close()
                reader.close()
                Result.success(text)
            }
        } catch (e: Exception) {
            android.util.Log.e("PdfTextExtractor", "Ошибка при извлечении текста из страниц: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Извлечь текст из PDF файла с разбиением на чанки по 32к токенов
     * @param context Контекст приложения
     * @param uri URI файла PDF
     * @param maxTokens Максимальное количество токенов в чанке (по умолчанию 32к)
     * @param onProgress Коллбек для отслеживания прогресса
     * @return Результат со списком чанков или ошибкой
     */
    suspend fun extractTextInChunks(
        context: Context,
        uri: Uri,
        maxTokens: Int = 32_000,
        onProgress: ((PdfProcessingProgress) -> Unit)? = null
    ): Result<List<TextChunk>> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d(TAG, "Извлечение текста в чанки по $maxTokens токенов")

            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Не удалось открыть файл"))

            inputStream.use { stream ->
                val reader = PdfReader(stream)
                val document = PdfDocument(reader)
                val chunks = mutableListOf<TextChunk>()

                // Сначала пробуем извлечь обычный текст
                var hasTextContent = false
                for (pageIndex in 0 until minOf(document.numberOfPages, 3)) { // Проверяем первые 3 страницы
                    val page = document.getPage(pageIndex + 1)
                    val pageText = PdfTextExtractor.getTextFromPage(page)
                    if (pageText.isNotBlank()) {
                        hasTextContent = true
                        break
                    }
                }

                if (hasTextContent) {
                    // Обычное извлечение текста из PDF
                    android.util.Log.d(TAG, "PDF содержит текст, извлекаем обычным способом")
                    for (pageIndex in 0 until document.numberOfPages) {
                        val page = document.getPage(pageIndex + 1)
                        val pageText = PdfTextExtractor.getTextFromPage(page)

                        if (pageText.isNotBlank()) {
                            val pageTokens = countTokens(pageText)

                            // Если текущий чанк + новая страница превысят лимит, создаем новый чанк
                            if (chunks.isNotEmpty() && chunks.last().tokenCount + pageTokens > maxTokens) {
                                chunks.add(TextChunk(
                                    text = pageText.trim(),
                                    startPage = pageIndex + 1,
                                    endPage = pageIndex + 1,
                                    tokenCount = pageTokens
                                ))
                            } else if (chunks.isEmpty()) {
                                chunks.add(TextChunk(
                                    text = pageText.trim(),
                                    startPage = pageIndex + 1,
                                    endPage = pageIndex + 1,
                                    tokenCount = pageTokens
                                ))
                            } else {
                                val lastChunk = chunks.last()
                                chunks[chunks.lastIndex] = lastChunk.copy(
                                    text = lastChunk.text + "\n\n" + pageText.trim(),
                                    endPage = pageIndex + 1,
                                    tokenCount = lastChunk.tokenCount + pageTokens
                                )
                            }
                        }
                    }
                } else {
                    // PDF содержит только изображения, отправляем напрямую в Gemini API
                    android.util.Log.d(TAG, "PDF содержит только изображения, отправляем в Gemini API")
                    val geminiText = extractTextWithGemini(context, uri, onProgress)
                    if (geminiText.isSuccess) {
                        val text = geminiText.getOrNull() ?: ""
                        chunks.addAll(splitTextIntoChunks(text, maxTokens).map { chunk ->
                            chunk.copy(startPage = -1, endPage = -1) // Gemini не дает информацию о страницах
                        })
                    } else {
                        return@withContext Result.failure(geminiText.exceptionOrNull() ?: Exception("Gemini API failed"))
                    }
                }

                document.close()
                reader.close()
                android.util.Log.d(TAG, "Создано ${chunks.size} чанков")
                Result.success(chunks)
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Ошибка при извлечении чанков: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Извлечь текст из PDF с помощью Gemini API (для PDF с изображениями)
     */
    private suspend fun extractTextWithGemini(
        context: Context,
        uri: Uri,
        onProgress: ((PdfProcessingProgress) -> Unit)? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d(TAG, "Начинаем обработку PDF через Gemini API")

            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
                ?: return@withContext Result.failure(Exception("Не удалось открыть файл для обработки"))

            parcelFileDescriptor.use { pfd ->
                val pdfRenderer = PdfRenderer(pfd)
                val textBuilder = StringBuilder()

                // Инициализируем состояние обработки
                val state = PdfProcessingState(uri.toString(), pdfRenderer.pageCount)
                currentProcessingState = state

                // Обрабатываем все страницы PDF
                for (pageIndex in 0 until pdfRenderer.pageCount) {
                    // Проверяем, не приостановлена ли обработка
                    if (state.isPaused) {
                        onProgress?.invoke(PdfProcessingProgress(
                            progress = (pageIndex.toFloat() / pdfRenderer.pageCount.toFloat()),
                            currentPage = pageIndex + 1,
                            totalPages = pdfRenderer.pageCount,
                            message = "Обработка приостановлена",
                            canResume = true,
                            isPaused = true,
                            currentPageTitle = "Страница ${pageIndex + 1}",
                            status = ProcessingStatus.PAUSED
                        ))
                        break
                    }

                    // Проверяем, не обработана ли уже эта страница
                    if (state.processedPages.containsKey(pageIndex + 1)) {
                        val processedPage = state.processedPages[pageIndex + 1]!!
                        textBuilder.append(processedPage.text).append("\n\n")
                        continue
                    }

                    val page = pdfRenderer.openPage(pageIndex)

                    // Создаем bitmap для страницы
                    val bitmap = Bitmap.createBitmap(
                        page.width,
                        page.height,
                        Bitmap.Config.ARGB_8888
                    )

                    // Рендерим страницу в bitmap
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    // Обрабатываем страницу с повторными попытками
                    val processedPage = processPageWithRetries(context, pageIndex + 1, bitmap, onProgress)
                    if (processedPage != null) {
                        textBuilder.append(processedPage.text).append("\n\n")
                        state.processedPages[pageIndex + 1] = processedPage
                        state.lastProcessedPage = pageIndex + 1
                    }

                    bitmap.recycle()
                }

                pdfRenderer.close()

                // Сохраняем состояние для возможности возобновления
                saveProcessingState(context, state)

                val finalText = textBuilder.toString().trim()
                android.util.Log.d(TAG, "Gemini обработка завершена, извлечено ${finalText.length} символов")

                // Финальный прогресс
                onProgress?.invoke(PdfProcessingProgress(
                    progress = 1.0f,
                    currentPage = pdfRenderer.pageCount,
                    totalPages = pdfRenderer.pageCount,
                    message = "Текст извлечен",
                    canResume = false,
                    isPaused = false,
                    currentPageTitle = "Завершено",
                    status = ProcessingStatus.COMPLETED
                ))

                Result.success(finalText)
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Ошибка при обработке через Gemini API: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Обработать страницу с повторными попытками
     */
    private suspend fun processPageWithRetries(
        context: Context,
        pageNumber: Int,
        bitmap: Bitmap,
        onProgress: ((PdfProcessingProgress) -> Unit)? = null
    ): ProcessedPage? {
        var retryCount = 0

        while (retryCount < MAX_RETRIES) {
            try {
                // Получаем название страницы через Gemini
                val titlePrompt = "Посмотри на эту страницу учебника и дай ей краткое название (2-5 слов). Верни только название, без дополнительных комментариев."
                val titleResult = GeminiService.sendImageMessage(context, titlePrompt, bitmap)
                val pageTitle = if (titleResult.isSuccess) {
                    titleResult.getOrNull()?.trim() ?: "Страница $pageNumber"
                } else {
                    "Страница $pageNumber"
                }

                // Отправляем изображение в Gemini API для извлечения текста
                val textPrompt = "Извлеки весь текст с этого изображения учебника. Верни только текст, без дополнительных комментариев."
                val result = GeminiService.sendImageMessage(context, textPrompt, bitmap)

                if (result.isSuccess) {
                    val pageText = result.getOrNull() ?: ""
                    android.util.Log.d(TAG, "Gemini извлек текст со страницы $pageNumber: ${pageText.take(100)}...")
                    return ProcessedPage(pageNumber, pageTitle, pageText, retryCount = retryCount)
                } else {
                    val error = result.exceptionOrNull()
                    val errorMessage = error?.message ?: "Неизвестная ошибка"

                    // Проверяем, является ли ошибка связанной с лимитами API
                    val isQuotaError = errorMessage.contains("429") || errorMessage.contains("quota") || errorMessage.contains("limit")

                    if (isQuotaError) {
                        android.util.Log.w(TAG, "Ошибка API лимита на странице $pageNumber (попытка ${retryCount + 1}/${MAX_RETRIES}): $errorMessage")

                        // При ошибке лимита API приостанавливаем обработку только после исчерпания всех попыток
                        if (retryCount >= MAX_RETRIES - 1) {
                            currentProcessingState?.isPaused = true
                            onProgress?.invoke(PdfProcessingProgress(
                                progress = (pageNumber.toFloat() / (currentProcessingState?.totalPages ?: 1).toFloat()),
                                currentPage = pageNumber,
                                totalPages = currentProcessingState?.totalPages ?: 0,
                                message = "Приостановлено из-за лимита API",
                                canResume = true,
                                isPaused = true,
                                currentPageTitle = pageTitle,
                                status = ProcessingStatus.PAUSED
                            ))
                            return null
                        }
                    }

                    android.util.Log.w(TAG, "Не удалось обработать страницу $pageNumber (попытка ${retryCount + 1}/${MAX_RETRIES}): $errorMessage")
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Исключение при обработке страницы $pageNumber (попытка ${retryCount + 1}/${MAX_RETRIES}): ${e.message}", e)
            }

            retryCount++
            if (retryCount < MAX_RETRIES) {
                android.util.Log.d(TAG, "Ждем $RETRY_DELAY_MS мс перед повторной попыткой...")
                kotlinx.coroutines.delay(RETRY_DELAY_MS)
            }
        }

        android.util.Log.e(TAG, "Не удалось обработать страницу $pageNumber после $MAX_RETRIES попыток")
        return null
    }

    /**
     * Сохранить состояние обработки
     */
    private suspend fun saveProcessingState(context: Context, state: PdfProcessingState) {
        try {
            val prefs = context.getSharedPreferences("pdf_processing", Context.MODE_PRIVATE)
            val json = Gson().toJson(state)
            prefs.edit().putString("processing_state_${state.uri.hashCode()}", json).apply()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Ошибка сохранения состояния обработки: ${e.message}")
        }
    }

    /**
     * Загрузить состояние обработки
     */
    private suspend fun loadProcessingState(context: Context, uri: String): PdfProcessingState? {
        return try {
            val prefs = context.getSharedPreferences("pdf_processing", Context.MODE_PRIVATE)
            val json = prefs.getString("processing_state_${uri.hashCode()}", null)
            if (json != null) {
                Gson().fromJson(json, PdfProcessingState::class.java)
            } else null
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Ошибка загрузки состояния обработки: ${e.message}")
            null
        }
    }

    /**
     * Приостановить обработку
     */
    fun pauseProcessing() {
        currentProcessingState?.isPaused = true
        android.util.Log.d(TAG, "Обработка PDF приостановлена")
    }

    /**
     * Возобновить обработку
     */
    fun resumeProcessing() {
        currentProcessingState?.isPaused = false
        android.util.Log.d(TAG, "Обработка PDF возобновлена")
    }

    /**
     * Проверить, можно ли возобновить обработку
     */
    fun canResumeProcessing(uri: String): Boolean {
        return currentProcessingState?.let { it.uri == uri && it.isPaused } ?: false
    }

    /**
     * Извлечь чанки из определённого диапазона страниц с учётом лимита токенов
     * @param context Контекст приложения
     * @param uri URI файла PDF
     * @param startPage Начальная страница
     * @param endPage Конечная страница
     * @param maxTokens Максимальное количество токенов в чанке
     * @return Результат со списком чанков или ошибкой
     */
    suspend fun extractTextInChunksFromPages(
        context: Context,
        uri: Uri,
        startPage: Int = 1,
        endPage: Int = Int.MAX_VALUE,
        maxTokens: Int = 32_000
    ): Result<List<TextChunk>> = withContext(Dispatchers.IO) {
        Result.failure(Exception("PDF обработка временно отключена - отсутствует зависимость"))
    }

    /**
     * Структурировать текст PDF с помощью AI, разбивая на параграфы
     * @param context Контекст приложения
     * @param uri URI файла PDF
     * @param maxTokens Максимальное количество токенов в чанке для AI
     * @param onProgress Коллбек для отслеживания прогресса
     * @param resumeFromState Восстановить состояние обработки
     * @return Результат со структурированным текстом или ошибкой
     */
    suspend fun structureTextWithAI(
        context: Context,
        uri: Uri,
        maxTokens: Int = 32_000,
        onProgress: ((PdfProcessingProgress) -> Unit)? = null,
        resumeFromState: Boolean = true
    ): Result<StructuredText> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("PdfTextExtractor", "Начало структурирования текста PDF с AI")

            // Инициализируем отсчет времени
            processingStartTime = System.currentTimeMillis()
            pagesProcessed = 0

            // Проверяем, есть ли сохраненное состояние для возобновления
            val savedState = if (resumeFromState) loadProcessingState(context, uri.toString()) else null
            val isResuming = savedState != null && savedState.processedPages.isNotEmpty()

            if (isResuming) {
                android.util.Log.d("PdfTextExtractor", "Возобновляем обработку с состояния: ${savedState!!.processedPages.size} обработанных страниц")
                currentProcessingState = savedState
            }

            // Получаем информацию о страницах
            android.util.Log.d("PdfTextExtractor", "Получаем информацию о страницах...")
            val totalPages = getTotalPages(context, uri)
            android.util.Log.d("PdfTextExtractor", "Всего страниц: $totalPages")

            // Сначала извлекаем чанки текста
            android.util.Log.d("PdfTextExtractor", "Извлекаем чанки текста...")
            onProgress?.invoke(PdfProcessingProgress(
                progress = if (isResuming) (savedState!!.processedPages.size.toFloat() / totalPages.toFloat()) else 0.0f,
                currentPage = savedState?.lastProcessedPage ?: 0,
                totalPages = totalPages,
                message = if (isResuming) "Возобновление обработки" else "Извлечение текста",
                canResume = isResuming,
                isPaused = false,
                currentPageTitle = "Подготовка",
                status = ProcessingStatus.PROCESSING
            ))

            val chunksResult = extractTextInChunks(context, uri, maxTokens, onProgress)
            if (chunksResult.isFailure) {
                android.util.Log.e("PdfTextExtractor", "Ошибка при извлечении чанков: ${chunksResult.exceptionOrNull()?.message}")
                return@withContext Result.failure(chunksResult.exceptionOrNull()!!)
            }

            val chunks = chunksResult.getOrNull()!!
            android.util.Log.d("PdfTextExtractor", "Извлечено ${chunks.size} чанков")
            if (chunks.isEmpty()) {
                android.util.Log.w("PdfTextExtractor", "Чанки пустые")
                return@withContext Result.failure(Exception("Не удалось извлечь текст из PDF"))
            }

            // Структурируем каждый чанк через AI
            val structuredParagraphs = mutableListOf<StructuredParagraph>()
            android.util.Log.d("PdfTextExtractor", "Начинаем структурирование ${chunks.size} чанков через AI...")

            for ((index, chunk) in chunks.withIndex()) {
                val progress = (index.toFloat() / chunks.size.toFloat()) * 0.8f + 0.1f // 10%-90%

                // Расчет ETA
                val elapsedTime = System.currentTimeMillis() - processingStartTime
                val avgTimePerChunk = if (index > 0) elapsedTime / index else 0L
                val remainingChunks = chunks.size - index - 1
                val estimatedTimeRemaining = avgTimePerChunk * remainingChunks

                onProgress?.invoke(PdfProcessingProgress(
                    progress = progress,
                    currentPage = chunk.startPage,
                    totalPages = totalPages,
                    message = "Структурирование текста",
                    canResume = true,
                    isPaused = false,
                    currentPageTitle = "Обработка чанка ${index + 1}/${chunks.size}",
                    status = ProcessingStatus.PROCESSING
                ))

                android.util.Log.d("PdfTextExtractor", "Структурируем чанк ${index + 1}/${chunks.size} (токенов: ${chunk.tokenCount})")
                val structuredResult = structureChunkWithAI(context, chunk)
                if (structuredResult.isSuccess) {
                    val paragraphs = structuredResult.getOrNull()!!
                    android.util.Log.d("PdfTextExtractor", "Чанк структурирован успешно, параграфов: ${paragraphs.size}")
                    structuredParagraphs.addAll(paragraphs)
                } else {
                    // Если не удалось структурировать чанк, пропускаем его
                    val error = structuredResult.exceptionOrNull()
                    android.util.Log.w("PdfTextExtractor", "Не удалось структурировать чанк ${index + 1}: ${error?.message ?: "Неизвестная ошибка"}")
                    android.util.Log.w("PdfTextExtractor", "Текст чанка: ${chunk.text.take(200)}...")
                }
            }

            android.util.Log.d("PdfTextExtractor", "Всего структурировано параграфов: ${structuredParagraphs.size}")
            if (structuredParagraphs.isEmpty()) {
                android.util.Log.w("PdfTextExtractor", "Ни один чанк не был структурирован")
                return@withContext Result.failure(Exception("Не удалось структурировать ни один чанк"))
            }

            // Создаём общее резюме
            onProgress?.invoke(PdfProcessingProgress(
                progress = 0.95f,
                currentPage = totalPages,
                totalPages = totalPages,
                message = "Генерация итогового резюме",
                canResume = false,
                isPaused = false,
                currentPageTitle = "Создание резюме",
                status = ProcessingStatus.PROCESSING
            ))
            android.util.Log.d("PdfTextExtractor", "Создаем резюме через AI...")
            val summary = createSummaryWithAI(context, structuredParagraphs)
            android.util.Log.d("PdfTextExtractor", "Резюме создано: ${summary.getOrNull()?.take(100) ?: "Ошибка"}")

            onProgress?.invoke(PdfProcessingProgress(
                progress = 1.0f,
                currentPage = totalPages,
                totalPages = totalPages,
                message = "Обработка завершена",
                canResume = false,
                isPaused = false,
                currentPageTitle = "Завершение",
                status = ProcessingStatus.COMPLETED
            ))

            // Сортируем параграфы по номерам страниц и названиям
            val sortedParagraphs = structuredParagraphs.sortedWith(compareBy({ it.pageNumber }, { it.title }))

            val result = StructuredText(
                paragraphs = sortedParagraphs,
                summary = summary.getOrNull() ?: "Резюме недоступно",
                totalPages = totalPages,
                totalTokens = chunks.sumOf { it.tokenCount }
            )

            // Очищаем состояние после успешного завершения
            currentProcessingState = null

            android.util.Log.d("PdfTextExtractor", "Структурирование завершено успешно")
            Result.success(result)

        } catch (e: Exception) {
            android.util.Log.e("PdfTextExtractor", "Ошибка при структурировании текста: ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Получить общее количество страниц в PDF
     */
    private suspend fun getTotalPages(context: Context, uri: Uri): Int = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext 0

            inputStream.use { stream ->
                val reader = PdfReader(stream)
                val document = PdfDocument(reader)
                val pages = document.numberOfPages
                document.close()
                reader.close()
                pages
            }
        } catch (e: Exception) {
            android.util.Log.e("PdfTextExtractor", "Ошибка при получении количества страниц: ${e.message}")
            0
        }
    }

    /**
     * Структурировать один чанк текста через AI
     */
    private suspend fun structureChunkWithAI(
        context: Context,
        chunk: TextChunk
    ): Result<List<StructuredParagraph>> {
        val prompt = """
            Проанализируй предоставленный текст учебника и структурируй его по параграфам.
            Верни результат ТОЛЬКО в формате JSON массива с параграфами, отсортированными по номерам страниц:

            [
              {
                "title": "Название параграфа (с номером страницы)",
                "content": "Содержимое параграфа",
                "pageNumber": 5,
                "importance": 3,
                "keywords": ["ключ1", "ключ2"]
              }
            ]

            Правила:
            1. Раздели текст на логические параграфы учебника
            2. Дай каждому параграфу описательный заголовок, включающий номер страницы
            3. Укажи точный номер страницы для каждого параграфа
            4. Оцени важность от 1 до 5 (5 - наиболее важный для учебного материала)
            5. Выдели 3-5 ключевых слов для каждого параграфа
            6. Отсортируй параграфы по номерам страниц
            7. Верни ТОЛЬКО валидный JSON массив, без дополнительного текста

            Текст для анализа (страницы ${chunk.startPage}-${chunk.endPage}):
            ${chunk.text}
        """.trimIndent()

        return try {
            val model = GeminiService.getModel(context)
            val result = GeminiService.sendMessage(context, prompt)
            result.map { response ->
                // Извлекаем JSON из ответа
                val jsonText = when {
                    response.contains("[") && response.contains("]") -> {
                        val start = response.indexOf("[")
                        val end = response.lastIndexOf("]") + 1
                        response.substring(start, end)
                    }
                    else -> "[]"
                }

                val jsonArray = JSONArray(jsonText)
                val paragraphs = mutableListOf<StructuredParagraph>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    paragraphs.add(
                        StructuredParagraph(
                            title = obj.getString("title"),
                            content = obj.getString("content"),
                            pageNumber = obj.optInt("pageNumber", chunk.startPage),
                            importance = obj.optInt("importance", 3),
                            keywords = obj.optJSONArray("keywords")?.let { arr ->
                                (0 until arr.length()).map { arr.getString(it) }
                            } ?: emptyList()
                        )
                    )
                }

                // Сортируем по номерам страниц
                paragraphs.sortedBy { it.pageNumber }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Создать резюме всего текста через AI
     */
    private suspend fun createSummaryWithAI(
        context: Context,
        paragraphs: List<StructuredParagraph>
    ): Result<String> {
        val titlesAndKeywords = paragraphs.joinToString("\n") { para ->
            "${para.title}: ${para.keywords.joinToString(", ")}"
        }

        val prompt = """
            На основе заголовков параграфов и ключевых слов создай краткое резюме всего текста.
            Будь лаконичен, выдели основные темы и идеи.

            Параграфы:
            $titlesAndKeywords

            Верни только резюме, без дополнительного текста.
        """.trimIndent()

        return try {
            val model = GeminiService.getModel(context)
            GeminiService.sendMessage(context, prompt)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


