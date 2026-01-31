package org.bxkr.octodiary.database.entity


import androidx.compose.material.icons.Icons
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_plans")
data class StudyPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val tasks: String, // JSON array of StudyTask
    val bedTime: String,
    val totalEstimatedMinutes: Int,
    val generatedAt: Long
)



