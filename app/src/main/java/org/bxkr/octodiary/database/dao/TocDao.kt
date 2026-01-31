package org.bxkr.octodiary.database.dao


import androidx.compose.material.icons.Icons
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.bxkr.octodiary.models.TocEntry

/**
 * DAO для работы с оглавлением учебников
 */
@Dao
interface TocDao {

    /**
     * Получить все элементы оглавления для учебника
     */
    @Query("SELECT * FROM toc_entries WHERE textbookId = :textbookId ORDER BY orderIndex ASC, level ASC")
    fun getTocEntriesForTextbook(textbookId: String): Flow<List<TocEntry>>

    /**
     * Получить все элементы оглавления для учебника (синхронно)
     */
    @Query("SELECT * FROM toc_entries WHERE textbookId = :textbookId ORDER BY orderIndex ASC, level ASC")
    suspend fun getTocEntriesForTextbookSync(textbookId: String): List<TocEntry>

    /**
     * Получить элемент оглавления по ID
     */
    @Query("SELECT * FROM toc_entries WHERE id = :id")
    suspend fun getTocEntryById(id: String): TocEntry?

    /**
     * Поиск элементов оглавления по заголовку или ключевым словам
     */
    @Query("""
        SELECT * FROM toc_entries
        WHERE textbookId = :textbookId
        AND (title LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%')
        ORDER BY orderIndex ASC
    """)
    suspend fun searchTocEntries(textbookId: String, query: String): List<TocEntry>

    /**
     * Вставить элемент оглавления
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTocEntry(tocEntry: TocEntry)

    /**
     * Вставить несколько элементов оглавления
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTocEntries(entries: List<TocEntry>)

    /**
     * Обновить элемент оглавления
     */
    @Update
    suspend fun updateTocEntry(tocEntry: TocEntry)

    /**
     * Удалить элемент оглавления
     */
    @Delete
    suspend fun deleteTocEntry(tocEntry: TocEntry)

    /**
     * Удалить все элементы оглавления для учебника
     */
    @Query("DELETE FROM toc_entries WHERE textbookId = :textbookId")
    suspend fun deleteTocEntriesForTextbook(textbookId: String)

    /**
     * Удалить элемент оглавления по ID
     */
    @Query("DELETE FROM toc_entries WHERE id = :id")
    suspend fun deleteTocEntryById(id: String)

    /**
     * Проверить, существует ли оглавление для учебника
     */
    @Query("SELECT COUNT(*) FROM toc_entries WHERE textbookId = :textbookId")
    suspend fun hasTocForTextbook(textbookId: String): Int
}


