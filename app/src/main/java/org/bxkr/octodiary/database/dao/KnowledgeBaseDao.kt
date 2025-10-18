package org.bxkr.octodiary.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.bxkr.octodiary.database.entities.KnowledgeBaseEntity

@Dao
interface KnowledgeBaseDao {
    @Query("SELECT * FROM knowledge_base ORDER BY lastAccessedAt DESC, createdAt DESC")
    fun getAllKnowledge(): Flow<List<KnowledgeBaseEntity>>

    @Query("SELECT * FROM knowledge_base WHERE subject = :subject ORDER BY createdAt DESC")
    fun getKnowledgeForSubject(subject: String): Flow<List<KnowledgeBaseEntity>>

    @Query("SELECT * FROM knowledge_base WHERE topic = :topic ORDER BY createdAt DESC")
    fun getKnowledgeForTopic(topic: String): Flow<List<KnowledgeBaseEntity>>

    @Query("SELECT * FROM knowledge_base WHERE type = :type ORDER BY createdAt DESC")
    fun getKnowledgeByType(type: String): Flow<List<KnowledgeBaseEntity>>

    @Query("SELECT * FROM knowledge_base WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavorites(): Flow<List<KnowledgeBaseEntity>>

    @Query("""
        SELECT * FROM knowledge_base 
        WHERE title LIKE '%' || :query || '%' 
        OR content LIKE '%' || :query || '%'
        OR topic LIKE '%' || :query || '%'
        ORDER BY lastAccessedAt DESC
    """)
    fun searchKnowledge(query: String): Flow<List<KnowledgeBaseEntity>>

    @Query("UPDATE knowledge_base SET lastAccessedAt = :accessTime, accessCount = accessCount + 1 WHERE id = :id")
    suspend fun updateAccessInfo(id: Long, accessTime: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(knowledge: KnowledgeBaseEntity): Long

    @Update
    suspend fun update(knowledge: KnowledgeBaseEntity)

    @Delete
    suspend fun delete(knowledge: KnowledgeBaseEntity)

    @Query("DELETE FROM knowledge_base WHERE subject = :subject")
    suspend fun deleteKnowledgeForSubject(subject: String)
}
