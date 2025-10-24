package org.bxkr.octodiary.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import org.bxkr.octodiary.database.entity.StreakEntity

@Dao
interface StreakDao {
    @Query("SELECT * FROM streaks WHERE type = :type")
    suspend fun getStreakByType(type: String): StreakEntity?
    
    @Query("SELECT * FROM streaks")
    suspend fun getAllStreaks(): List<StreakEntity>
    
    @Insert
    suspend fun insertStreak(streak: StreakEntity): Long
    
    @Update
    suspend fun updateStreak(streak: StreakEntity)
}
