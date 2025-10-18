package org.bxkr.octodiary.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.bxkr.octodiary.database.entities.StudyReminderEntity
import java.util.Date

@Dao
interface StudyReminderDao {
    @Query("SELECT * FROM study_reminders WHERE isActive = 1 AND isCompleted = 0 ORDER BY scheduledTime ASC")
    fun getActiveReminders(): Flow<List<StudyReminderEntity>>

    @Query("""
        SELECT * FROM study_reminders 
        WHERE isActive = 1 
        AND isCompleted = 0 
        AND scheduledTime <= :currentTime
        AND (snoozedUntil IS NULL OR snoozedUntil <= :currentTime)
        ORDER BY priority DESC, scheduledTime ASC
    """)
    suspend fun getDueReminders(currentTime: Date): List<StudyReminderEntity>

    @Query("SELECT * FROM study_reminders WHERE type = :type AND isCompleted = 0 ORDER BY scheduledTime ASC")
    fun getRemindersByType(type: String): Flow<List<StudyReminderEntity>>

    @Query("SELECT * FROM study_reminders WHERE relatedSubject = :subject AND isCompleted = 0 ORDER BY scheduledTime ASC")
    fun getRemindersForSubject(subject: String): Flow<List<StudyReminderEntity>>

    @Query("SELECT * FROM study_reminders WHERE relatedExamPlanId = :examPlanId ORDER BY scheduledTime ASC")
    fun getRemindersForExamPlan(examPlanId: Long): Flow<List<StudyReminderEntity>>

    @Query("SELECT COUNT(*) FROM study_reminders WHERE isActive = 1 AND isCompleted = 0 AND scheduledTime <= :date")
    suspend fun getDueReminderCount(date: Date): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: StudyReminderEntity): Long

    @Update
    suspend fun update(reminder: StudyReminderEntity)

    @Delete
    suspend fun delete(reminder: StudyReminderEntity)

    @Query("UPDATE study_reminders SET isCompleted = 1, completedAt = :completedAt WHERE id = :id")
    suspend fun markAsCompleted(id: Long, completedAt: Date)

    @Query("UPDATE study_reminders SET snoozedUntil = :snoozeUntil WHERE id = :id")
    suspend fun snooze(id: Long, snoozeUntil: Date)

    @Query("DELETE FROM study_reminders WHERE isCompleted = 1 AND completedAt < :beforeDate")
    suspend fun deleteOldCompletedReminders(beforeDate: Date)
}
