package org.bxkr.octodiary.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.bxkr.octodiary.database.entities.ExamPlanEntity
import java.util.Date

@Dao
interface ExamPlanDao {
    @Query("SELECT * FROM exam_plans WHERE isCompleted = 0 ORDER BY examDate ASC")
    fun getActiveExamPlans(): Flow<List<ExamPlanEntity>>

    @Query("SELECT * FROM exam_plans ORDER BY examDate DESC")
    fun getAllExamPlans(): Flow<List<ExamPlanEntity>>

    @Query("SELECT * FROM exam_plans WHERE subject = :subject ORDER BY examDate ASC")
    fun getExamPlansForSubject(subject: String): Flow<List<ExamPlanEntity>>

    @Query("SELECT * FROM exam_plans WHERE examDate BETWEEN :startDate AND :endDate ORDER BY examDate ASC")
    fun getExamPlansInRange(startDate: Date, endDate: Date): Flow<List<ExamPlanEntity>>

    @Query("SELECT * FROM exam_plans WHERE examDate <= :date AND isCompleted = 0 ORDER BY examDate ASC LIMIT :limit")
    fun getUpcomingExams(date: Date, limit: Int = 10): Flow<List<ExamPlanEntity>>

    @Query("SELECT COUNT(*) FROM exam_plans WHERE examDate >= :date AND isCompleted = 0")
    suspend fun getUpcomingExamCount(date: Date): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(examPlan: ExamPlanEntity): Long

    @Update
    suspend fun update(examPlan: ExamPlanEntity)

    @Delete
    suspend fun delete(examPlan: ExamPlanEntity)

    @Query("UPDATE exam_plans SET isCompleted = 1 WHERE id = :id")
    suspend fun markAsCompleted(id: Long)
}
