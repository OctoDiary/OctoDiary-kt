package org.bxkr.octodiary.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.bxkr.octodiary.database.entity.ExamPlanEntity
import java.util.Date

@Dao
interface ExamPlanDao {
    
    @Query("SELECT * FROM exam_plans ORDER BY examDate ASC")
    fun getAllFlow(): Flow<List<ExamPlanEntity>>
    
    @Query("SELECT * FROM exam_plans WHERE id = :id")
    suspend fun getById(id: Long): ExamPlanEntity?
    
    @Query("SELECT * FROM exam_plans WHERE subject = :subject ORDER BY examDate ASC")
    fun getBySubjectFlow(subject: String): Flow<List<ExamPlanEntity>>
    
    @Query("SELECT * FROM exam_plans WHERE isCompleted = 0 ORDER BY examDate ASC")
    fun getUpcomingFlow(): Flow<List<ExamPlanEntity>>
    
    @Query("SELECT * FROM exam_plans WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedFlow(): Flow<List<ExamPlanEntity>>
    
    @Query("SELECT * FROM exam_plans WHERE examDate BETWEEN :startDate AND :endDate ORDER BY examDate ASC")
    fun getByDateRangeFlow(startDate: Date, endDate: Date): Flow<List<ExamPlanEntity>>
    
    @Query("SELECT * FROM exam_plans WHERE daysUntilExam <= :days AND isCompleted = 0 ORDER BY daysUntilExam ASC")
    fun getUpcomingSoonFlow(days: Int = 7): Flow<List<ExamPlanEntity>>
    
    @Query("SELECT * FROM exam_plans WHERE progressPercent < 50 AND daysUntilExam <= 7 AND isCompleted = 0")
    fun getBehindScheduleFlow(): Flow<List<ExamPlanEntity>>
    
    @Query("SELECT DISTINCT subject FROM exam_plans ORDER BY subject")
    fun getDistinctSubjectsFlow(): Flow<List<String>>
    
    @Query("SELECT COUNT(*) FROM exam_plans WHERE isCompleted = 0")
    fun getUpcomingCountFlow(): Flow<Int>
    
    @Query("SELECT AVG(actualExamScore) FROM exam_plans WHERE actualExamScore IS NOT NULL AND subject = :subject")
    fun getAverageScoreBySubjectFlow(subject: String): Flow<Float?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: ExamPlanEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plans: List<ExamPlanEntity>)
    
    @Update
    suspend fun update(plan: ExamPlanEntity)
    
    @Delete
    suspend fun delete(plan: ExamPlanEntity)
    
    @Query("DELETE FROM exam_plans WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM exam_plans")
    suspend fun deleteAll()
}
