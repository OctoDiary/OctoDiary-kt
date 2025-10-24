package org.bxkr.octodiary.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import org.bxkr.octodiary.database.entity.LectureNoteEntity

@Dao
interface LectureNoteDao {
    @Query("SELECT * FROM lecture_notes ORDER BY date DESC")
    suspend fun getAllNotes(): List<LectureNoteEntity>
    
    @Query("SELECT * FROM lecture_notes WHERE subjectName = :subject ORDER BY date DESC")
    suspend fun getNotesBySubject(subject: String): List<LectureNoteEntity>
    
    @Query("SELECT * FROM lecture_notes WHERE id = :id")
    suspend fun getNoteById(id: Long): LectureNoteEntity?
    
    @Insert
    suspend fun insertNote(note: LectureNoteEntity): Long
    
    @Update
    suspend fun updateNote(note: LectureNoteEntity)
    
    @Query("DELETE FROM lecture_notes WHERE id = :id")
    suspend fun deleteNote(id: Long)
}
