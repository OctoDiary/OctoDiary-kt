package org.bxkr.octodiary.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streaks")
data class StreakEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActivityDate: Long
)
