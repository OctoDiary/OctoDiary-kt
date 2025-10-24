package org.bxkr.octodiary.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary")
data class VocabularyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val translation: String,
    val language: String,
    val subjectName: String,
    val examples: String? = null, // JSON array
    val createdAt: Long,
    val masteryLevel: Int = 0,
    val lastReviewed: Long? = null
)
