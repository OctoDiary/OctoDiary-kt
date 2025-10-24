package org.bxkr.octodiary.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.bxkr.octodiary.database.entity.StudyProgressEntity

@Dao
interface StudyProgressDao {
    
    @Query("SELECT * FROM study_progress ORDER BY lastUpdatedAt DESC")
    fun getAllFlow(): Flow<List<StudyProgressEntity>>
    
    @Query("SELECT * FROM study_progress WHERE id = :id")
    suspend fun getById(id: Long): StudyProgressEntity?
    
    @Query("SELECT * FROM study_progress WHERE subject = :subject ORDER BY lastUpdatedAt DESC")
    fun getBySubjectFlow(subject: String): Flow<List<StudyProgressEntity>>
    
    @Query("SELECT * FROM study_progress WHERE status = :status ORDER BY lastUpdatedAt DESC")
    fun getByStatusFlow(status: String): Flow<List<StudyProgressEntity>>
    
    @Query("SELECT * FROM study_progress WHERE status = 'struggling' OR needsAttention = 1 ORDER BY lastUpdatedAt DESC")
    fun getNeedsAttentionFlow(): Flow<List<StudyProgressEntity>>
    
    @Query("SELECT * FROM study_progress WHERE masteryLevel >= :minLevel ORDER BY masteryLevel DESC")
    fun getMasteredFlow(minLevel: Int = 4): Flow<List<StudyProgressEntity>>
    
    @Query("SELECT * FROM study_progress WHERE completedAt IS NULL ORDER BY progressPercent ASC")
    fun getInProgressFlow(): Flow<List<StudyProgressEntity>>
    
    @Query("SELECT * FROM study_progress WHERE trendDirection = 'declining' ORDER BY lastUpdatedAt DESC")
    fun getDecliningFlow(): Flow<List<StudyProgressEntity>>
    
    @Query("SELECT DISTINCT subject FROM study_progress ORDER BY subject")
    fun getDistinctSubjectsFlow(): Flow<List<String>>
    
    @Query("SELECT AVG(masteryLevel) FROM study_progress WHERE subject = :subject")
    fun getAverageMasteryBySubjectFlow(subject: String): Flow<Float?>
    
    @Query("SELECT COUNT(*) FROM study_progress WHERE needsAttention = 1")
    fun getNeedsAttentionCountFlow(): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: StudyProgressEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(progressList: List<StudyProgressEntity>)
    
    @Update
    suspend fun update(progress: StudyProgressEntity)
    
    @Delete
    suspend fun delete(progress: StudyProgressEntity)
    
    @Query("DELETE FROM study_progress WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM study_progress")
    suspend fun deleteAll()
}
