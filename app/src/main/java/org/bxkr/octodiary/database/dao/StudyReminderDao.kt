package org.bxkr.octodiary.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.bxkr.octodiary.database.entity.StudyReminderEntity
import java.util.Date

@Dao
interface StudyReminderDao {
    
    @Query("SELECT * FROM study_reminders ORDER BY scheduledTime ASC")
    fun getAllFlow(): Flow<List<StudyReminderEntity>>
    
    @Query("SELECT * FROM study_reminders WHERE id = :id")
    suspend fun getById(id: Long): StudyReminderEntity?
    
    @Query("SELECT * FROM study_reminders WHERE isCompleted = 0 AND isCancelled = 0 ORDER BY scheduledTime ASC")
    fun getActiveFlow(): Flow<List<StudyReminderEntity>>
    
    @Query("SELECT * FROM study_reminders WHERE isCompleted = 0 AND isCancelled = 0 AND scheduledTime <= :date ORDER BY scheduledTime ASC")
    fun getUpcomingFlow(date: Date): Flow<List<StudyReminderEntity>>
    
    @Query("SELECT * FROM study_reminders WHERE type = :type AND isCompleted = 0 AND isCancelled = 0 ORDER BY scheduledTime ASC")
    fun getByTypeFlow(type: String): Flow<List<StudyReminderEntity>>
    
    @Query("SELECT * FROM study_reminders WHERE priority = :priority AND isCompleted = 0 AND isCancelled = 0 ORDER BY scheduledTime ASC")
    fun getByPriorityFlow(priority: Int): Flow<List<StudyReminderEntity>>
    
    @Query("SELECT * FROM study_reminders WHERE urgency = 3 AND isCompleted = 0 AND isCancelled = 0 ORDER BY scheduledTime ASC")
    fun getUrgentFlow(): Flow<List<StudyReminderEntity>>
    
    @Query("SELECT * FROM study_reminders WHERE isRecurring = 1 AND isCompleted = 0 AND isCancelled = 0 ORDER BY scheduledTime ASC")
    fun getRecurringFlow(): Flow<List<StudyReminderEntity>>
    
    @Query("SELECT * FROM study_reminders WHERE isSnoozed = 1 AND snoozedUntil > :now ORDER BY snoozedUntil ASC")
    fun getSnoozedFlow(now: Date): Flow<List<StudyReminderEntity>>
    
    @Query("SELECT * FROM study_reminders WHERE relatedSubject = :subject AND isCompleted = 0 AND isCancelled = 0 ORDER BY scheduledTime ASC")
    fun getBySubjectFlow(subject: String): Flow<List<StudyReminderEntity>>
    
    @Query("SELECT COUNT(*) FROM study_reminders WHERE isCompleted = 0 AND isCancelled = 0 AND scheduledTime <= :date")
    fun getUpcomingCountFlow(date: Date): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM study_reminders WHERE urgency = 3 AND isCompleted = 0 AND isCancelled = 0")
    fun getUrgentCountFlow(): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: StudyReminderEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reminders: List<StudyReminderEntity>)
    
    @Update
    suspend fun update(reminder: StudyReminderEntity)
    
    @Delete
    suspend fun delete(reminder: StudyReminderEntity)
    
    @Query("DELETE FROM study_reminders WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM study_reminders WHERE isCompleted = 1 AND completedAt < :beforeDate")
    suspend fun deleteCompletedBefore(beforeDate: Date)
    
    @Query("DELETE FROM study_reminders")
    suspend fun deleteAll()
}
