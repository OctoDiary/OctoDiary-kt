package org.bxkr.octodiary.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Прогресс обучения по предметам и темам
 */
@Entity(tableName = "study_progress")
data class StudyProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val subject: String,
    val topic: String,
    
    // Прогресс (0-100)
    val masteryLevel: Int = 0, // Уровень владения темой
    
    // Статистика
    val totalStudyTimeMinutes: Int = 0,
    val totalExercisesSolved: Int = 0,
    val totalExercisesCorrect: Int = 0,
    
    // Оценки по теме
    val marks: List<Long> = emptyList(), // IDs оценок
    val averageMark: Float = 0f,
    
    // AI анализ
    val aiAnalysis: String? = null,
    val weakPoints: List<String> = emptyList(),
    val strengths: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    
    // Временные метки
    val firstStudiedAt: Date = Date(),
    val lastStudiedAt: Date = Date(),
    val nextRecommendedStudyDate: Date? = null,
    
    // Целевые показатели
    val targetMasteryLevel: Int = 100,
    val targetDate: Date? = null
)
