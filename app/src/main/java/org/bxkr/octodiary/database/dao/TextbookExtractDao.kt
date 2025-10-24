package org.bxkr.octodiary.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.bxkr.octodiary.database.entity.TextbookExtractEntity

@Dao
interface TextbookExtractDao {

    @Query("SELECT * FROM textbook_extracts ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<TextbookExtractEntity>>

    @Query("SELECT * FROM textbook_extracts ORDER BY createdAt DESC")
    suspend fun getAll(): List<TextbookExtractEntity>

    @Query("SELECT * FROM textbook_extracts WHERE id = :id")
    suspend fun getById(id: Long): TextbookExtractEntity?

    @Query("SELECT * FROM textbook_extracts WHERE paragraphId = :paragraphId ORDER BY createdAt DESC")
    fun getByParagraphIdFlow(paragraphId: Long): Flow<List<TextbookExtractEntity>>

    @Query("SELECT * FROM textbook_extracts WHERE paragraphId = :paragraphId ORDER BY createdAt DESC")
    suspend fun getByParagraphId(paragraphId: Long): List<TextbookExtractEntity>

    @Query("SELECT * FROM textbook_extracts WHERE extractType = :type ORDER BY createdAt DESC")
    fun getByTypeFlow(type: String): Flow<List<TextbookExtractEntity>>

    @Query("SELECT * FROM textbook_extracts WHERE isCompleted = :completed ORDER BY updatedAt DESC")
    fun getByCompletedStatusFlow(completed: Boolean): Flow<List<TextbookExtractEntity>>

    @Query("SELECT COUNT(*) FROM textbook_extracts WHERE paragraphId = :paragraphId")
    suspend fun countByParagraphId(paragraphId: Long): Int

    @Query("SELECT COUNT(*) FROM textbook_extracts WHERE extractType = :type")
    suspend fun countByType(type: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TextbookExtractEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TextbookExtractEntity>)

    @Update
    suspend fun update(item: TextbookExtractEntity)

    @Delete
    suspend fun delete(item: TextbookExtractEntity)

    @Query("DELETE FROM textbook_extracts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM textbook_extracts WHERE paragraphId = :paragraphId")
    suspend fun deleteByParagraphId(paragraphId: Long)

    @Query("DELETE FROM textbook_extracts")
    suspend fun deleteAll()

    // Обновление статуса выполнения
    @Query("UPDATE textbook_extracts SET isCompleted = :completed, completedAt = :completedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, completed: Boolean, completedAt: java.util.Date?, updatedAt: java.util.Date)
}