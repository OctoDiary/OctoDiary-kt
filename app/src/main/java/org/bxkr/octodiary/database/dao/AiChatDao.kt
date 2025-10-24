package org.bxkr.octodiary.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.bxkr.octodiary.database.entity.AiChatMessageEntity

@Dao
interface AiChatDao {
    @Query("SELECT * FROM ai_chat_messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    suspend fun getMessagesByChatId(chatId: String): List<AiChatMessageEntity>
    
    @Insert
    suspend fun insertMessage(message: AiChatMessageEntity): Long
    
    @Query("DELETE FROM ai_chat_messages WHERE chatId = :chatId")
    suspend fun clearChat(chatId: String)
    
    @Query("SELECT DISTINCT chatId FROM ai_chat_messages")
    suspend fun getAllChatIds(): List<String>
}
