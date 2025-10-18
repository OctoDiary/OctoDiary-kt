package org.bxkr.octodiary.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.bxkr.octodiary.database.entities.FlashcardEntity
import java.util.Date

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY nextReviewDate ASC")
    fun getAllFlashcards(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE subject = :subject ORDER BY nextReviewDate ASC")
    fun getFlashcardsForSubject(subject: String): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE deckName = :deckName ORDER BY nextReviewDate ASC")
    fun getFlashcardsForDeck(deckName: String): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE nextReviewDate <= :date ORDER BY nextReviewDate ASC")
    fun getFlashcardsDueForReview(date: Date): Flow<List<FlashcardEntity>>

    @Query("SELECT COUNT(*) FROM flashcards WHERE nextReviewDate <= :date")
    suspend fun getDueCount(date: Date): Int

    @Query("""
        SELECT * FROM flashcards 
        WHERE nextReviewDate <= :date AND subject = :subject
        ORDER BY nextReviewDate ASC
    """)
    fun getDueFlashcardsForSubject(date: Date, subject: String): Flow<List<FlashcardEntity>>

    @Query("SELECT DISTINCT deckName FROM flashcards WHERE deckName IS NOT NULL")
    fun getAllDeckNames(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM flashcards WHERE deckName = :deckName")
    suspend fun getDeckSize(deckName: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flashcard: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(flashcards: List<FlashcardEntity>)

    @Update
    suspend fun update(flashcard: FlashcardEntity)

    @Delete
    suspend fun delete(flashcard: FlashcardEntity)

    @Query("DELETE FROM flashcards WHERE deckName = :deckName")
    suspend fun deleteDeck(deckName: String)
}
