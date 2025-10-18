package org.bxkr.octodiary.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * База знаний - сохранённые решения, конспекты, шпаргалки
 */
@Entity(tableName = "knowledge_base")
data class KnowledgeBaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val subject: String,
    val topic: String,
    val title: String,
    val content: String,
    
    // Тип контента
    val type: String, // "solution", "note", "cheatsheet", "summary"
    
    // Связи
    val relatedLessonId: Long? = null,
    val relatedHomeworkId: Long? = null,
    
    // Источник
    val source: String? = null, // "ai_generated", "user_created", "audio_transcript", "pdf_extract"
    val sourceDetails: String? = null,
    
    // Метаданные
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val lastAccessedAt: Date? = null,
    val accessCount: Int = 0,
    
    // Теги для поиска
    val tags: List<String> = emptyList(),
    
    // Избранное
    val isFavorite: Boolean = false,
    
    // Аттачменты
    val attachments: List<String> = emptyList() // Пути к файлам (фото, PDF и т.д.)
)
