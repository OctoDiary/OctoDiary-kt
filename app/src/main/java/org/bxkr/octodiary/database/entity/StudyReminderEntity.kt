package org.bxkr.octodiary.database.entity


import androidx.compose.material.icons.Icons
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "study_reminders")
data class StudyReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val title: String,
    val description: String,
    val type: String, // homework, exam, flashcard_review, study_session, break, custom
    
    val scheduledTime: Date,
    val createdAt: Date,
    val updatedAt: Date,
    
    // Повторение
    val isRecurring: Boolean = false,
    val recurringPattern: String? = null, // daily, weekly, custom
    val recurringDays: List<Int>? = null, // 1=пн, 2=вт, ..., 7=вс
    
    // Приоритет
    val priority: Int = 2, // 1=низкий, 2=средний, 3=высокий
    val urgency: Int = 2, // 1=может подождать, 2=скоро, 3=срочно
    
    // Статус
    val isCompleted: Boolean = false,
    val completedAt: Date? = null,
    val isSnoozed: Boolean = false,
    val snoozedUntil: Date? = null,
    val isCancelled: Boolean = false,
    
    // AI умные напоминания
    val isAiGenerated: Boolean = false,
    val aiReason: String? = null, // почему AI создал это напоминание
    val aiSuggestions: String? = null,
    
    // Связи
    val relatedHomeworkId: Long? = null,
    val relatedExamPlanId: Long? = null,
    val relatedFlashcardDeckId: Long? = null,
    val relatedSubject: String? = null,
    
    // Уведомления
    val notificationMinutesBefore: List<Int> = listOf(15, 0), // за 15 мин и в момент
    val lastNotificationSent: Date? = null,
    val notificationSound: String? = null,
    val notificationVibrate: Boolean = true,
    
    // Метаданные
    val tags: List<String> = emptyList(),
    val color: String? = null, // hex color для UI
    val icon: String? = null // название Material Icon
)



