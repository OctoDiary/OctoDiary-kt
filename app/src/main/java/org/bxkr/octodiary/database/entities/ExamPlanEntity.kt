package org.bxkr.octodiary.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Планировщик подготовки к экзаменам
 */
@Entity(tableName = "exam_plans")
data class ExamPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val subject: String,
    val examType: String, // "ОГЭ", "ЕГЭ", "Контрольная", "Итоговая"
    val examDate: Date,
    val examName: String,
    
    // План подготовки
    val topics: List<String> = emptyList(),
    val topicsCompleted: List<String> = emptyList(),
    val totalHoursPlanned: Int = 0,
    val hoursSpent: Int = 0,
    
    // Расписание
    val studySchedule: List<String> = emptyList(), // JSON [{date, topic, hours}]
    val dailyHoursTarget: Int = 2,
    
    // AI рекомендации
    val aiGeneratedPlan: Boolean = false,
    val priorityTopics: List<String> = emptyList(),
    val difficulty: String = "medium", // easy, medium, hard
    
    // Прогресс
    val overallProgress: Int = 0, // 0-100
    val confidenceLevel: Int = 50, // 0-100
    
    // Напоминания
    val reminderEnabled: Boolean = true,
    val reminderDaysBeforeExam: List<Int> = listOf(30, 14, 7, 3, 1),
    
    // Ресурсы
    val materials: List<String> = emptyList(), // Ссылки на материалы
    val practiceTests: List<Long> = emptyList(), // IDs тестов
    
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isCompleted: Boolean = false
)
