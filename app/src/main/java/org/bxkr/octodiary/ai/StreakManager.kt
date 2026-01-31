package org.bxkr.octodiary.ai


import androidx.compose.material.icons.Icons
import android.content.Context
import java.util.Calendar
import java.util.concurrent.TimeUnit
import org.bxkr.octodiary.database.AppDatabase
import org.bxkr.octodiary.database.entity.StreakEntity

/**
 * Менеджер стриков (серий дней)
 */
object StreakManager {
    
    /**
     * Обновить стрик при новой активности
     */
    suspend fun updateStreak(context: Context, type: String) {
        val db = AppDatabase.getDatabase(context) ?: return
        val dao = db.streakDao()
        
        val existing = dao.getStreakByType(type)
        val today = getTodayMillis()
        
        if (existing == null) {
            // Создать новый стрик
            dao.insertStreak(
                StreakEntity(
                    type = type,
                    currentStreak = 1,
                    longestStreak = 1,
                    lastActivityDate = today
                )
            )
        } else {
            val lastDate = existing.lastActivityDate
            val daysDiff = TimeUnit.MILLISECONDS.toDays(today - lastDate)
            
            when {
                daysDiff == 0L -> {
                    // Сегодня уже была активность - ничего не делаем
                }
                daysDiff == 1L -> {
                    // Вчера была активность - продолжаем стрик
                    val newCurrent = existing.currentStreak + 1
                    val newLongest = maxOf(newCurrent, existing.longestStreak)
                    dao.updateStreak(
                        existing.copy(
                            currentStreak = newCurrent,
                            longestStreak = newLongest,
                            lastActivityDate = today
                        )
                    )
                }
                else -> {
                    // Пропущено больше дня - сбросить стрик
                    dao.updateStreak(
                        existing.copy(
                            currentStreak = 1,
                            lastActivityDate = today
                        )
                    )
                }
            }
        }
    }
    
    /**
     * Получить текущий стрик
     */
    suspend fun getCurrentStreak(context: Context, type: String): Int {
        val db = AppDatabase.getDatabase(context) ?: return 0
        val streak = db.streakDao().getStreakByType(type)
        
        if (streak == null) return 0
        
        val today = getTodayMillis()
        val daysDiff = TimeUnit.MILLISECONDS.toDays(today - streak.lastActivityDate)
        
        return if (daysDiff > 1) {
            // Стрик потерян
            0
        } else {
            streak.currentStreak
        }
    }
    
    /**
     * Получить самый длинный стрик
     */
    suspend fun getLongestStreak(context: Context, type: String): Int {
        val db = AppDatabase.getDatabase(context) ?: return 0
        return db.streakDao().getStreakByType(type)?.longestStreak ?: 0
    }
    
    /**
     * Проверить стрик на сегодня
     */
    suspend fun checkStreakToday(context: Context, type: String): Boolean {
        val db = AppDatabase.getDatabase(context) ?: return false
        val streak = db.streakDao().getStreakByType(type) ?: return false
        
        val today = getTodayMillis()
        val daysDiff = TimeUnit.MILLISECONDS.toDays(today - streak.lastActivityDate)
        
        return daysDiff == 0L
    }
    
    private fun getTodayMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}



