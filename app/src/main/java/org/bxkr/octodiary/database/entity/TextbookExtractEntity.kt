package org.bxkr.octodiary.database.entity


import androidx.compose.material.icons.Icons
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "textbook_extracts")
data class TextbookExtractEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val paragraphId: Long, // Ссылка на ParagraphEntity
    val extractType: String, // "question", "exercise", "assignment"
    val content: String, // Текст извлечённого элемента
    val context: String, // Контекст вокруг извлечения для понимания

    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),

    // Метаданные
    val difficulty: Int? = null, // Сложность (1-5, если определено AI)
    val estimatedTime: Int? = null, // Предполагаемое время выполнения в минутах

    // Связи
    val relatedSubjectId: Long? = null,
    val relatedTopicId: Long? = null,

    // Статус
    val isCompleted: Boolean = false,
    val completedAt: Date? = null
)


