package org.bxkr.octodiary.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "exam_plans")
data class ExamPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val subject: String,
    val examType: String, // test, quiz, exam, егэ, огэ, final
    val examDate: Date,
    val examTitle: String,
    
    val createdAt: Date,
    val updatedAt: Date,
    
    // Планирование
    val studyStartDate: Date,
    val daysUntilExam: Int,
    val hoursPerDayPlanned: Float,
    val totalHoursPlanned: Float,
    
    // Прогресс
    val totalHoursStudied: Float = 0f,
    val progressPercent: Int = 0, // 0-100
    val topicsCovered: List<String> = emptyList(),
    val topicsRemaining: List<String> = emptyList(),
    
    // AI помощь
    val aiGeneratedPlan: String? = null, // детальный план подготовки
    val aiRecommendations: String? = null,
    val priorityTopics: List<String> = emptyList(), // темы в порядке важности
    
    // Статистика
    val practiceTestsTaken: Int = 0,
    val averagePracticeScore: Float? = null,
    val strongTopics: List<String> = emptyList(),
    val weakTopics: List<String> = emptyList(),
    
    // Состояние
    val isCompleted: Boolean = false,
    val completedAt: Date? = null,
    val actualExamScore: Float? = null,
    val actualExamGrade: String? = null,
    
    // Напоминания
    val reminderDaysBefore: List<Int> = listOf(7, 3, 1), // за сколько дней напомнить
    val lastReminderSent: Date? = null,
    
    // Связи
    val relatedFlashcardDeckIds: List<Long> = emptyList(),
    val relatedKnowledgeBaseIds: List<Long> = emptyList(),
    val relatedProgressIds: List<Long> = emptyList()
)
