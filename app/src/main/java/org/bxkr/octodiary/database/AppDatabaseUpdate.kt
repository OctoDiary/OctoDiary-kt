package org.bxkr.octodiary.database

/**
 * ВАЖНО: Обновление AppDatabase.kt
 * 
 * Добавить новые DAOs и entities в AppDatabase:
 */

/*

import org.bxkr.octodiary.database.dao.*
import org.bxkr.octodiary.database.entity.*

@Database(
    entities = [
        // ... существующие entities ...
        
        // Новые AI entities
        AiChatMessageEntity::class,
        VocabularyEntity::class,
        LectureNoteEntity::class,
        StreakEntity::class,
        StudyPlanEntity::class
    ],
    version = 2, // УВЕЛИЧИТЬ ВЕРСИЮ
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    // ... существующие DAOs ...
    
    // Новые AI DAOs
    abstract fun aiChatDao(): AiChatDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun lectureNoteDao(): LectureNoteDao
    abstract fun streakDao(): StreakDao
    abstract fun studyPlanDao(): StudyPlanDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "octodiary_database"
                )
                    .fallbackToDestructiveMigration() // Для разработки
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

*/

/**
 * Миграция с версии 1 на 2:
 * 
 * val MIGRATION_1_2 = object : Migration(1, 2) {
 *     override fun migrate(database: SupportSQLiteDatabase) {
 *         // AI Chat Messages
 *         database.execSQL("""
 *             CREATE TABLE IF NOT EXISTS ai_chat_messages (
 *                 id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
 *                 chatId TEXT NOT NULL,
 *                 content TEXT NOT NULL,
 *                 isUser INTEGER NOT NULL,
 *                 timestamp INTEGER NOT NULL,
 *                 imageUri TEXT,
 *                 attachments TEXT
 *             )
 *         """)
 *         
 *         // Vocabulary
 *         database.execSQL("""
 *             CREATE TABLE IF NOT EXISTS vocabulary (
 *                 id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
 *                 word TEXT NOT NULL,
 *                 translation TEXT NOT NULL,
 *                 language TEXT NOT NULL,
 *                 subjectName TEXT NOT NULL,
 *                 examples TEXT,
 *                 createdAt INTEGER NOT NULL,
 *                 masteryLevel INTEGER NOT NULL DEFAULT 0,
 *                 lastReviewed INTEGER
 *             )
 *         """)
 *         
 *         // Lecture Notes
 *         database.execSQL("""
 *             CREATE TABLE IF NOT EXISTS lecture_notes (
 *                 id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
 *                 subjectName TEXT NOT NULL,
 *                 topic TEXT NOT NULL,
 *                 date INTEGER NOT NULL,
 *                 content TEXT NOT NULL,
 *                 audioPath TEXT,
 *                 keyPoints TEXT,
 *                 generatedQuestions TEXT,
 *                 images TEXT,
 *                 isAiGenerated INTEGER NOT NULL DEFAULT 0
 *             )
 *         """)
 *         
 *         // Streaks
 *         database.execSQL("""
 *             CREATE TABLE IF NOT EXISTS streaks (
 *                 id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
 *                 type TEXT NOT NULL,
 *                 currentStreak INTEGER NOT NULL,
 *                 longestStreak INTEGER NOT NULL,
 *                 lastActivityDate INTEGER NOT NULL
 *             )
 *         """)
 *         
 *         // Study Plans
 *         database.execSQL("""
 *             CREATE TABLE IF NOT EXISTS study_plans (
 *                 id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
 *                 date INTEGER NOT NULL,
 *                 tasks TEXT NOT NULL,
 *                 bedTime TEXT NOT NULL,
 *                 totalEstimatedMinutes INTEGER NOT NULL,
 *                 generatedAt INTEGER NOT NULL
 *             )
 *         """)
 *     }
 * }
 */
