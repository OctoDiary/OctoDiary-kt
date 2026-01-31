package org.bxkr.octodiary.database.dao


import androidx.compose.material.icons.Icons
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.bxkr.octodiary.database.entity.KnowledgeBaseEntity

@Dao
interface KnowledgeBaseDao {
    
    @Query("SELECT * FROM knowledge_base ORDER BY updatedAt DESC")
    fun getAllFlow(): Flow<List<KnowledgeBaseEntity>>
    
    @Query("SELECT * FROM knowledge_base ORDER BY updatedAt DESC")
    suspend fun getAll(): List<KnowledgeBaseEntity>
    
    @Query("SELECT * FROM knowledge_base WHERE id = :id")
    suspend fun getById(id: Long): KnowledgeBaseEntity?
    
    @Query("SELECT * FROM knowledge_base WHERE subject = :subject ORDER BY updatedAt DESC")
    fun getBySubjectFlow(subject: String): Flow<List<KnowledgeBaseEntity>>
    
    @Query("SELECT * FROM knowledge_base WHERE type = :type ORDER BY updatedAt DESC")
    fun getByTypeFlow(type: String): Flow<List<KnowledgeBaseEntity>>
    
    @Query("SELECT * FROM knowledge_base WHERE subject = :subject AND topic = :topic ORDER BY updatedAt DESC")
    fun getBySubjectAndTopicFlow(subject: String, topic: String): Flow<List<KnowledgeBaseEntity>>
    
    @Query("""
        SELECT * FROM knowledge_base
        WHERE searchableText LIKE '%' || :query || '%'
        OR title LIKE '%' || :query || '%'
        ORDER BY importance DESC, updatedAt DESC
    """)
    fun searchFlow(query: String): Flow<List<KnowledgeBaseEntity>>
    
    @Query("SELECT DISTINCT subject FROM knowledge_base ORDER BY subject")
    fun getDistinctSubjectsFlow(): Flow<List<String>>
    
    @Query("SELECT DISTINCT topic FROM knowledge_base WHERE subject = :subject ORDER BY topic")
    fun getDistinctTopicsFlow(subject: String): Flow<List<String>>
    
    @Query("SELECT * FROM knowledge_base ORDER BY timesAccessed DESC LIMIT :limit")
    fun getMostAccessedFlow(limit: Int = 10): Flow<List<KnowledgeBaseEntity>>
    
    @Query("SELECT * FROM knowledge_base WHERE importance >= :minImportance ORDER BY importance DESC, updatedAt DESC")
    fun getImportantFlow(minImportance: Int = 4): Flow<List<KnowledgeBaseEntity>>
    
    @Query("SELECT COUNT(*) FROM knowledge_base")
    fun getCountFlow(): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: KnowledgeBaseEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<KnowledgeBaseEntity>)
    
    @Update
    suspend fun update(item: KnowledgeBaseEntity)
    
    @Delete
    suspend fun delete(item: KnowledgeBaseEntity)
    
    @Query("DELETE FROM knowledge_base WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM knowledge_base")
    suspend fun deleteAll()
}



