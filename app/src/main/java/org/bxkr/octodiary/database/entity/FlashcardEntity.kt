package org.bxkr.octodiary.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val subject: String,
    val topic: String,
    val front: String, // вопрос
    val back: String, // ответ
    val hint: String? = null,
    
    val createdAt: Date,
    val updatedAt: Date,
    
    // Spaced Repetition (Anki-style)
    val easeFactor: Float = 2.5f, // начальное значение
    val interval: Int = 0, // дней до следующего повтора
    val repetitions: Int = 0, // сколько раз правильно ответил подряд
    val nextReviewDate: Date = Date(),
    
    // Статистика
    val timesReviewed: Int = 0,
    val timesCorrect: Int = 0,
    val timesWrong: Int = 0,
    val lastReviewDate: Date? = null,
    val lastReviewResult: String? = null, // again, hard, good, easy
    
    // Организация
    val deckName: String = "Default",
    val tags: List<String> = emptyList(),
    val isSuspended: Boolean = false,
    
    // Связь с базой знаний
    val sourceKnowledgeBaseId: Long? = null,
    val sourceAISolutionId: Long? = null
)
