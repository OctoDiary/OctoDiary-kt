package org.bxkr.octodiary.database.entity


import androidx.compose.material.icons.Icons
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "knowledge_base")
data class KnowledgeBaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val subject: String,
    val topic: String,
    val type: String, // solution, note, cheatsheet, formula
    val title: String,
    val content: String,
    val source: String, // ai_solution, manual, pdf_import, wikipedia
    
    val createdAt: Date,
    val updatedAt: Date,
    
    // Связи
    val relatedHomeworkId: Long? = null,
    val relatedLessonId: Long? = null,
    val tags: List<String> = emptyList(),
    
    // Метаданные
    val difficulty: Int? = null, // 1-5
    val importance: Int? = null, // 1-5
    val timesAccessed: Int = 0,
    val lastAccessedAt: Date? = null,
    
    // Для поиска
    val searchableText: String = "", // content + title + tags
    
    // Синхронизация
    val isSynced: Boolean = false,
    val syncedAt: Date? = null
)



