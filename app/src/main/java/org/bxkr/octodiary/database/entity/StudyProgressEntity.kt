package org.bxkr.octodiary.database.entity


import androidx.compose.material.icons.Icons
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "study_progress")
data class StudyProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val subject: String,
    val topic: String,
    val lessonTitle: String,
    
    val startedAt: Date,
    val lastUpdatedAt: Date,
    val completedAt: Date? = null,
    
    // Прогресс
    val progressPercent: Int = 0, // 0-100
    val status: String, // not_started, in_progress, struggling, mastered, needs_review
    val masteryLevel: Int = 0, // 0-5
    
    // Статистика
    val timeSpentMinutes: Int = 0,
    val tasksCompleted: Int = 0,
    val tasksTotal: Int = 0,
    val averageScore: Float? = null,
    
    // AI анализ
    val aiAnalysis: String? = null, // рекомендации от AI
    val weakPoints: List<String> = emptyList(), // слабые места
    val strengths: List<String> = emptyList(), // сильные стороны
    val suggestedResources: List<String> = emptyList(), // ссылки на материалы
    
    // Тренды
    val trendDirection: String? = null, // improving, declining, stable
    val trendConfidence: Float? = null,
    
    // Связи
    val relatedHomeworkIds: List<Long> = emptyList(),
    val relatedKnowledgeBaseIds: List<Long> = emptyList(),
    val relatedFlashcardIds: List<Long> = emptyList(),
    
    // Уведомления
    val needsAttention: Boolean = false,
    val attentionReason: String? = null
)



