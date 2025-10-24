package org.bxkr.octodiary.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
// import org.bxkr.octodiary.database.AppDatabase
// import org.bxkr.octodiary.database.entity.ParagraphEntity
import org.bxkr.octodiary.models.ParagraphRequest
import org.bxkr.octodiary.models.ParagraphResponse
import java.util.Date

class ParagraphRepository(/*private val database: AppDatabase*/) { // Commented out database parameter
 
    // private val dao = database.paragraphDao() // Commented out dao initialization
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Запрашивает параграф по теме. Сначала проверяет кэш, затем делает сетевой запрос при необходимости.
     * @param request Запрос параграфа
     * @return Flow с результатом запроса
     */
    fun requestParagraph(request: ParagraphRequest): Flow<ParagraphResponse> = kotlinx.coroutines.flow.flow {
        /*
        try {
            Log.d(TAG, "Requesting paragraph for topic: ${request.topic}")
 
            // Сначала проверяем кэш
            val cached = kotlinx.coroutines.flow.flowOf(dao.getByRequestId(generateRequestId(request))).firstOrNull()
            if (cached != null) {
                Log.d(TAG, "Found cached paragraph: ${cached.id}")
                emit(cached.toParagraphResponse())
                // Обновляем статистику доступа
                scope.launch {
                    dao.incrementAccessCount(cached.id, Date())
                }
            } else {
                Log.d(TAG, "No cached paragraph found, making network request")
 
                // Делаем сетевой запрос
                val networkResponse = ParagraphService.requestParagraph(request)
 
                if (networkResponse.success && networkResponse.requestId.isNotEmpty()) {
                    // Сохраняем в базу данных
                    val entity = networkResponse.toParagraphEntity()
                    dao.insert(entity)
                    Log.d(TAG, "Saved paragraph to database: ${entity.id}")
                }
 
                emit(networkResponse)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in requestParagraph", e)
            emit(ParagraphResponse(
                requestId = "",
                title = "",
                content = "",
                source = "",
                language = request.language,
                wordCount = 0,
                estimatedReadTime = 0,
                success = false,
                errorMessage = e.localizedMessage ?: "Unknown error"
            ))
        }
        */
        emit(ParagraphResponse(
            requestId = "",
            title = "",
            content = "",
            source = "",
            language = request.language,
            wordCount = 0,
            estimatedReadTime = 0,
            success = false,
            errorMessage = "Room Database is disabled"
        ))
    }

    /**
     * Получает все параграфы из кэша
     */
    fun getAllParagraphs(): Flow<List<Any>> = kotlinx.coroutines.flow.flowOf(emptyList()) // Changed return type to List<Any>
    // fun getAllParagraphs(): Flow<List<ParagraphEntity>> = dao.getAllFlow()

    /**
     * Ищет параграфы по тексту
     */
    fun searchParagraphs(query: String): Flow<List<Any>> = kotlinx.coroutines.flow.flowOf(emptyList()) // Changed return type to List<Any>
    // fun searchParagraphs(query: String): Flow<List<ParagraphEntity>> = dao.searchFlow(query)

    /**
     * Получает параграфы по источнику
     */
    fun getParagraphsBySource(source: String): Flow<List<Any>> = kotlinx.coroutines.flow.flowOf(emptyList()) // Changed return type to List<Any>
    // fun getParagraphsBySource(source: String): Flow<List<ParagraphEntity>> = dao.getBySourceFlow(source)

    /**
     * Получает параграфы по языку
     */
    fun getParagraphsByLanguage(language: String): Flow<List<Any>> = kotlinx.coroutines.flow.flowOf(emptyList()) // Changed return type to List<Any>
    // fun getParagraphsByLanguage(language: String): Flow<List<ParagraphEntity>> = dao.getByLanguageFlow(language)

    /**
     * Получает наиболее часто используемые параграфы
     */
    fun getMostAccessedParagraphs(limit: Int = 10): Flow<List<Any>> = kotlinx.coroutines.flow.flowOf(emptyList()) // Changed return type to List<Any>
    // fun getMostAccessedParagraphs(limit: Int = 10): Flow<List<ParagraphEntity>> = dao.getMostAccessedFlow(limit)

    /**
     * Получает недавно использованные параграфы
     */
    fun getRecentlyAccessedParagraphs(limit: Int = 10): Flow<List<Any>> = kotlinx.coroutines.flow.flowOf(emptyList()) // Changed return type to List<Any>
    // fun getRecentlyAccessedParagraphs(limit: Int = 10): Flow<List<ParagraphEntity>> = dao.getRecentlyAccessedFlow(limit)

    /**
     * Получает параграф по ID
     */
    suspend fun getParagraphById(id: Long): Any? = null // Changed return type to Any?
    // suspend fun getParagraphById(id: Long): ParagraphEntity? = dao.getById(id)

    /**
     * Удаляет параграф по ID
     */
    suspend fun deleteParagraphById(id: Long) { /* dao.deleteById(id) */ } // Commented out dao call

    /**
     * Очищает все параграфы
     */
    suspend fun clearAllParagraphs() { /* dao.deleteAll() */ } // Commented out dao call

    /**
     * Генерирует уникальный ID запроса на основе параметров
     */
    private fun generateRequestId(request: ParagraphRequest): String {
        val base = "${request.topic}_${request.language}"
        return if (request.maxLength != null) {
            "${base}_${request.maxLength}"
        } else {
            base
        }
    }

    companion object {
        private const val TAG = "ParagraphRepository"
    }
}

// Extension functions для конвертации между Entity и Response
private fun ParagraphResponse.toParagraphEntity(): Any { // Changed return type to Any
    /*
    val currentTime = Date()
    return ParagraphEntity(
        id = 0, // auto-generate
        requestId = this.requestId,
        title = this.title,
        content = this.content,
        source = this.source,
        language = this.language,
        createdAt = currentTime,
        updatedAt = currentTime,
        wordCount = this.wordCount,
        estimatedReadTime = this.estimatedReadTime,
        searchableText = "${this.title} ${this.content}",
        tags = this.tags,
        accessCount = 1,
        lastAccessedAt = currentTime,
        isSynced = true,
        syncedAt = currentTime
    )
    */
    return Any() // Return a dummy object for now
}

private fun Any.toParagraphResponse(): ParagraphResponse { // Changed receiver type to Any
    /*
    return ParagraphResponse(
        requestId = this.requestId,
        title = this.title,
        content = this.content,
        source = this.source,
        language = this.language,
        wordCount = this.wordCount,
        estimatedReadTime = this.estimatedReadTime,
        tags = this.tags,
        success = true,
        errorMessage = null
    )
    */
    return ParagraphResponse(
        requestId = "",
        title = "",
        content = "",
        source = "",
        language = "",
        wordCount = 0,
        estimatedReadTime = 0,
        success = false,
        errorMessage = "Room Database is disabled"
    )
}