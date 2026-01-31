package org.bxkr.octodiary.database.entity


import androidx.compose.material.icons.Icons
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "paragraphs")
data class ParagraphEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val requestId: String, // Уникальный идентификатор запроса
    val title: String, // Название или тема параграфа
    val content: String, // Полный текст параграфа
    val source: String, // Источник (например, "ai_generated", "wikipedia", etc.)
    val language: String = "ru", // Язык текста

    val createdAt: Date,
    val updatedAt: Date,

    // Метаданные
    val wordCount: Int, // Количество слов в параграфе
    val estimatedReadTime: Int, // Предполагаемое время чтения в минутах

    // Связи с другими сущностями (опционально)
    val relatedSubjectId: Long? = null,
    val relatedTopicId: Long? = null,

    // Для поиска и фильтрации
    val searchableText: String = "", // title + content для полнотекстового поиска
    val tags: List<String> = emptyList(), // Теги для категоризации

    // Статистика использования
    val accessCount: Int = 0,
    val lastAccessedAt: Date? = null,

    // Синхронизация
    val isSynced: Boolean = false,
    val syncedAt: Date? = null
)


