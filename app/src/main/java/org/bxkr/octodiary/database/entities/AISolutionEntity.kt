package org.bxkr.octodiary.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * История решений AI для домашних заданий
 */
@Entity(tableName = "ai_solutions")
data class AISolutionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val homeworkId: Long,
    val subject: String,
    val task: String,
    
    // AI данные
    val provider: String, // "openai", "anthropic", "google", "local"
    val model: String,
    val solution: String,
    val reasoning: String? = null,
    val confidence: Float = 0f,
    
    // Метаданные
    val createdAt: Date = Date(),
    val executionTimeMs: Long = 0,
    val tokensUsed: Int = 0,
    
    // Экспорт
    val exportedToPdf: Boolean = false,
    val pdfPath: String? = null,
    
    // Оценка пользователя
    val userRating: Int? = null, // 1-5
    val userFeedback: String? = null
)
