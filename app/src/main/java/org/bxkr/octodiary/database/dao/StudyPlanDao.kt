package org.bxkr.octodiary.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.bxkr.octodiary.database.entity.StudyPlanEntity

@Dao
interface StudyPlanDao {
    @Query("SELECT * FROM study_plans WHERE date = :date")
    suspend fun getPlanByDate(date: Long): StudyPlanEntity?
    
    @Query("SELECT * FROM study_plans ORDER BY date DESC LIMIT 7")
    suspend fun getRecentPlans(): List<StudyPlanEntity>
    
    @Insert
    suspend fun insertPlan(plan: StudyPlanEntity): Long
    
    @Query("DELETE FROM study_plans WHERE date < :cutoffDate")
    suspend fun deleteOldPlans(cutoffDate: Long)
}
