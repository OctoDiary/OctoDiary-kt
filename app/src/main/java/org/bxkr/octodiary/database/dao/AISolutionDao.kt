package org.bxkr.octodiary.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.bxkr.octodiary.database.entities.AISolutionEntity

@Dao
interface AISolutionDao {
    @Query("SELECT * FROM ai_solutions WHERE homeworkId = :homeworkId ORDER BY createdAt DESC")
    fun getSolutionsForHomework(homeworkId: Long): Flow<List<AISolutionEntity>>

    @Query("SELECT * FROM ai_solutions WHERE subject = :subject ORDER BY createdAt DESC LIMIT :limit")
    fun getSolutionsForSubject(subject: String, limit: Int = 50): Flow<List<AISolutionEntity>>

    @Query("SELECT * FROM ai_solutions ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentSolutions(limit: Int = 100): Flow<List<AISolutionEntity>>

    @Query("SELECT * FROM ai_solutions WHERE exportedToPdf = 0 ORDER BY createdAt DESC")
    fun getUnexportedSolutions(): Flow<List<AISolutionEntity>>

    @Query("SELECT COUNT(*) FROM ai_solutions WHERE subject = :subject")
    suspend fun getSolutionCountForSubject(subject: String): Int

    @Query("SELECT AVG(userRating) FROM ai_solutions WHERE subject = :subject AND userRating IS NOT NULL")
    suspend fun getAverageRatingForSubject(subject: String): Float?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(solution: AISolutionEntity): Long

    @Update
    suspend fun update(solution: AISolutionEntity)

    @Delete
    suspend fun delete(solution: AISolutionEntity)

    @Query("DELETE FROM ai_solutions WHERE homeworkId = :homeworkId")
    suspend fun deleteSolutionsForHomework(homeworkId: Long)

    @Query("DELETE FROM ai_solutions WHERE createdAt < :beforeDate")
    suspend fun deleteOlderThan(beforeDate: Long)
}
