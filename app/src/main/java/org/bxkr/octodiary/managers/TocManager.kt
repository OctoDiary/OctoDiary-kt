package org.bxkr.octodiary.managers

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import org.bxkr.octodiary.database.AppDatabase
import org.bxkr.octodiary.models.Textbook
import org.bxkr.octodiary.models.TocEntry
 
/**
 * Менеджер для управления оглавлением учебников - высо��оуровневый интерфейс
 */
object TocManager {
 
    /**
     * Генерировать оглавление для учебника
     */
    suspend fun generateTocForTextbook(
        context: Context,
        textbook: Textbook
    ): Result<Unit> {
        val fileUri = Uri.parse("file://${textbook.filePath}")
        return TocService.generateTocForTextbook(context, textbook.id, fileUri)
    }
 
    /**
     * Получить оглавление для учебника
     */
    suspend fun getTocForTextbook(
        context: Context,
        textbookId: String
    ): List<TocEntry> {
        return TocService.getTocForTextbook(context, textbookId)
    }
 
    /**
     * Получить оглавление для учебника как Flow
     */
    fun getTocForTextbookFlow(
        context: Context,
        textbookId: String
    ): Flow<List<TocEntry>> {
        val database = AppDatabase.getDatabase(context)
        return database?.tocDao()?.getTocEntriesForTextbook(textbookId)
            ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }
 
    /**
     * Проверить, существует ли оглавление для учебника
     */
    suspend fun hasTocForTextbook(
        context: Context,
        textbookId: String
    ): Boolean {
        return TocService.hasTocForTextbook(context, textbookId)
    }
 
    /**
     * Поиск в оглавлении учебника
     */
    suspend fun searchInToc(
        context: Context,
        textbookId: String,
        query: String
    ): List<TocEntry> {
        return TocService.searchInToc(context, textbookId, query)
    }
 
    /**
     * Удалить оглавление для учебника
     */
    suspend fun clearTocForTextbook(
        context: Context,
        textbookId: String
    ): Boolean {
        return TocService.clearTocForTextbook(context, textbookId)
    }
 
    /**
     * Получить контекст оглавления для AI (список заголовков)
     */
    fun getTocContextForAI(context: Context, textbook: Textbook): String {
        // Этот метод будет вызываться асинхронно в корутине
        // Пока возвращаем базовую информацию
        return buildString {
            appendLine("**Оглавление учебника:** ${textbook.title}")
            appendLine("Предмет: ${textbook.subjectName}")
            if (textbook.author != null) {
                appendLine("Автор: ${textbook.author}")
            }
            // Оглавление будет добавлено при генерации
        }
    }
}