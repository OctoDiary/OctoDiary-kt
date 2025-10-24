package org.bxkr.octodiary.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import org.bxkr.octodiary.database.entity.VocabularyEntity

@Dao
interface VocabularyDao {
    @Query("SELECT * FROM vocabulary ORDER BY createdAt DESC")
    suspend fun getAllWords(): List<VocabularyEntity>
    
    @Query("SELECT * FROM vocabulary WHERE subjectName = :subject ORDER BY createdAt DESC")
    suspend fun getWordsBySubject(subject: String): List<VocabularyEntity>
    
    @Query("SELECT * FROM vocabulary WHERE language = :lang ORDER BY createdAt DESC")
    suspend fun getWordsByLanguage(lang: String): List<VocabularyEntity>
    
    @Query("SELECT * FROM vocabulary WHERE masteryLevel < :level ORDER BY lastReviewed ASC")
    suspend fun getWordsToReview(level: Int = 5): List<VocabularyEntity>
    
    @Insert
    suspend fun insertWord(word: VocabularyEntity): Long
    
    @Insert
    suspend fun insertWords(words: List<VocabularyEntity>)
    
    @Update
    suspend fun updateWord(word: VocabularyEntity)
    
    @Query("DELETE FROM vocabulary WHERE id = :id")
    suspend fun deleteWord(id: Long)
}
