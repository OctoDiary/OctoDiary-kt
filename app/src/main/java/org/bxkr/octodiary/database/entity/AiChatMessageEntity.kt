package org.bxkr.octodiary.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_chat_messages")
data class AiChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chatId: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long,
    val imageUri: String? = null,
    val attachments: String? = null // JSON array
)
