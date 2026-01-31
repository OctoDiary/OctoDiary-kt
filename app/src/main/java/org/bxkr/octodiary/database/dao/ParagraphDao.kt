package org.bxkr.octodiary.database.dao


import androidx.compose.material.icons.Icons
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.bxkr.octodiary.database.entity.ParagraphEntity
import java.util.Date

@Dao
interface ParagraphDao {

    @Query("SELECT * FROM paragraphs ORDER BY updatedAt DESC")
    fun getAllFlow(): Flow<List<ParagraphEntity>>

    @Query("SELECT * FROM paragraphs ORDER BY updatedAt DESC")
    suspend fun getAll(): List<ParagraphEntity>

    @Query("SELECT * FROM paragraphs WHERE id = :id")
    suspend fun getById(id: Long): ParagraphEntity?

    @Query("SELECT * FROM paragraphs WHERE requestId = :requestId")
    suspend fun getByRequestId(requestId: String): ParagraphEntity?

    @Query("SELECT * FROM paragraphs WHERE title LIKE '%' || :query || '%' OR searchableText LIKE '%' || :query || '%' ORDER BY accessCount DESC, updatedAt DESC")
    fun searchFlow(query: String): Flow<List<ParagraphEntity>>

    @Query("SELECT * FROM paragraphs WHERE source = :source ORDER BY updatedAt DESC")
    fun getBySourceFlow(source: String): Flow<List<ParagraphEntity>>

    @Query("SELECT * FROM paragraphs WHERE language = :language ORDER BY updatedAt DESC")
    fun getByLanguageFlow(language: String): Flow<List<ParagraphEntity>>

    @Query("SELECT * FROM paragraphs ORDER BY accessCount DESC LIMIT :limit")
    fun getMostAccessedFlow(limit: Int = 10): Flow<List<ParagraphEntity>>

    @Query("SELECT * FROM paragraphs ORDER BY lastAccessedAt DESC LIMIT :limit")
    fun getRecentlyAccessedFlow(limit: Int = 10): Flow<List<ParagraphEntity>>

    @Query("SELECT COUNT(*) FROM paragraphs")
    fun getCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM paragraphs WHERE requestId = :requestId")
    suspend fun countByRequestId(requestId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ParagraphEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ParagraphEntity>)

    @Update
    suspend fun update(item: ParagraphEntity)

    @Delete
    suspend fun delete(item: ParagraphEntity)

    @Query("DELETE FROM paragraphs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM paragraphs WHERE requestId = :requestId")
    suspend fun deleteByRequestId(requestId: String)

    @Query("DELETE FROM paragraphs")
    suspend fun deleteAll()

    // Обновление статистики доступа
    @Query("UPDATE paragraphs SET accessCount = accessCount + 1, lastAccessedAt = :accessedAt WHERE id = :id")
    suspend fun incrementAccessCount(id: Long, accessedAt: java.util.Date)
}


