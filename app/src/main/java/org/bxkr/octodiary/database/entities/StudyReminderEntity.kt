package org.bxkr.octodiary.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Умные напоминания о подготовке
 */
@Entity(tableName = "study_reminders")
data class StudyReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val title: String,
    val message: String,
    val type: String, // "exam", "homework", "review", "practice"
    
    // Привязка
    val relatedSubject: String? = null,
    val relatedExamPlanId: Long? = null,
    val relatedHomeworkId: Long? = null,
    
    // Время
    val scheduledTime: Date,
    val isRecurring: Boolean = false,
    val recurringPattern: String? = null, // "daily", "weekly", "custom"
    
    // Умная логика
    val aiGenerated: Boolean = false,
    val basedOnProgress: Boolean = false,
    val priority: String = "normal", // low, normal, high, urgent
    
    // Статус
    val isActive: Boolean = true,
    val isCompleted: Boolean = false,
    val completedAt: Date? = null,
    val snoozedUntil: Date? = null,
    
    // Действия
    val actionType: String? = null, // "open_homework", "start_practice", "review_cards"
    val actionData: String? = null,
    
    val createdAt: Date = Date()
)
