package org.bxkr.octodiary.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Карточки для запоминания (Anki-style)
 */
@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val subject: String,
    val topic: String,
    
    // Контент карточки
    val front: String, // Вопрос
    val back: String, // Ответ
    val hint: String? = null,
    
    // Мультимедиа
    val frontImagePath: String? = null,
    val backImagePath: String? = null,
    val audioPath: String? = null,
    
    // Spaced Repetition (Anki algorithm)
    val easeFactor: Float = 2.5f,
    val interval: Int = 1, // Дней до следующего повторения
    val repetitions: Int = 0,
    val nextReviewDate: Date = Date(),
    val lastReviewDate: Date? = null,
    
    // Статистика
    val timesReviewed: Int = 0,
    val timesCorrect: Int = 0,
    val timesIncorrect: Int = 0,
    val averageReviewTimeMs: Long = 0,
    
    // Метаданные
    val createdAt: Date = Date(),
    val createdBy: String = "user", // "user", "ai", "import"
    val deckName: String? = null,
    
    // Связи
    val relatedKnowledgeId: Long? = null,
    val relatedLessonId: Long? = null,
    
    val tags: List<String> = emptyList()
)
