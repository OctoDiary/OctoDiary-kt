package org.bxkr.octodiary.database.dao


import androidx.compose.material.icons.Icons
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.bxkr.octodiary.database.entity.FlashcardEntity
import java.util.Date

@Dao
interface FlashcardDao {
    
    @Query("SELECT * FROM flashcards ORDER BY nextReviewDate ASC")
    fun getAllFlow(): Flow<List<FlashcardEntity>>
    
    @Query("SELECT * FROM flashcards WHERE id = :id")
    suspend fun getById(id: Long): FlashcardEntity?
    
    @Query("SELECT * FROM flashcards WHERE deckName = :deckName ORDER BY nextReviewDate ASC")
    fun getByDeckFlow(deckName: String): Flow<List<FlashcardEntity>>
    
    @Query("SELECT * FROM flashcards WHERE subject = :subject ORDER BY nextReviewDate ASC")
    fun getBySubjectFlow(subject: String): Flow<List<FlashcardEntity>>
    
    @Query("SELECT * FROM flashcards WHERE nextReviewDate <= :date AND isSuspended = 0 ORDER BY nextReviewDate ASC")
    fun getDueForReviewFlow(date: Date): Flow<List<FlashcardEntity>>
    
    @Query("SELECT * FROM flashcards WHERE nextReviewDate <= :date AND isSuspended = 0 ORDER BY nextReviewDate ASC")
    suspend fun getDueForReview(date: Date): List<FlashcardEntity>
    
    @Query("SELECT COUNT(*) FROM flashcards WHERE nextReviewDate <= :date AND isSuspended = 0")
    fun getDueCountFlow(date: Date): Flow<Int>
    
    @Query("SELECT * FROM flashcards WHERE repetitions = 0 AND isSuspended = 0 ORDER BY createdAt DESC")
    fun getNewCardsFlow(): Flow<List<FlashcardEntity>>
    
    @Query("SELECT DISTINCT deckName FROM flashcards ORDER BY deckName")
    fun getDistinctDecksFlow(): Flow<List<String>>
    
    @Query("SELECT DISTINCT subject FROM flashcards ORDER BY subject")
    fun getDistinctSubjectsFlow(): Flow<List<String>>
    
    @Query("SELECT COUNT(*) FROM flashcards WHERE deckName = :deckName")
    fun getDeckCardCountFlow(deckName: String): Flow<Int>
    
    @Query("SELECT AVG(CAST(timesCorrect AS FLOAT) / NULLIF(timesReviewed, 0)) FROM flashcards WHERE deckName = :deckName")
    fun getDeckSuccessRateFlow(deckName: String): Flow<Float?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: FlashcardEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<FlashcardEntity>)
    
    @Update
    suspend fun update(card: FlashcardEntity)
    
    @Delete
    suspend fun delete(card: FlashcardEntity)
    
    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM flashcards WHERE deckName = :deckName")
    suspend fun deleteDeck(deckName: String)
    
    @Query("DELETE FROM flashcards")
    suspend fun deleteAll()
}



