package org.bxkr.octodiary.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.bxkr.octodiary.database.entities.StudyProgressEntity

@Dao
interface StudyProgressDao {
    @Query("SELECT * FROM study_progress ORDER BY lastStudiedAt DESC")
    fun getAllProgress(): Flow<List<StudyProgressEntity>>

    @Query("SELECT * FROM study_progress WHERE subject = :subject ORDER BY topic ASC")
    fun getProgressForSubject(subject: String): Flow<List<StudyProgressEntity>>

    @Query("SELECT * FROM study_progress WHERE subject = :subject AND topic = :topic")
    fun getProgressForTopic(subject: String, topic: String): Flow<StudyProgressEntity?>

    @Query("SELECT * FROM study_progress WHERE masteryLevel < :threshold ORDER BY lastStudiedAt ASC")
    fun getTopicsNeedingWork(threshold: Int = 70): Flow<List<StudyProgressEntity>>

    @Query("SELECT * FROM study_progress WHERE nextRecommendedStudyDate <= :currentDate ORDER BY nextRecommendedStudyDate ASC")
    suspend fun getTopicsDueForReview(currentDate: Long): List<StudyProgressEntity>

    @Query("SELECT AVG(masteryLevel) FROM study_progress WHERE subject = :subject")
    suspend fun getAverageMasteryForSubject(subject: String): Float?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: StudyProgressEntity): Long

    @Update
    suspend fun update(progress: StudyProgressEntity)

    @Delete
    suspend fun delete(progress: StudyProgressEntity)

    @Query("DELETE FROM study_progress WHERE subject = :subject")
    suspend fun deleteProgressForSubject(subject: String)
}
