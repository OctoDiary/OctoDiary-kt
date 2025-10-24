package org.bxkr.octodiary.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lecture_notes")
data class LectureNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectName: String,
    val topic: String,
    val date: Long,
    val content: String,
    val audioPath: String? = null,
    val keyPoints: String? = null, // JSON array
    val generatedQuestions: String? = null, // JSON array
    val images: String? = null, // JSON array
    val isAiGenerated: Boolean = false
)
