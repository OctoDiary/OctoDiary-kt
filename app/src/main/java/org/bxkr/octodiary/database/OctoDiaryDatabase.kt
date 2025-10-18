package org.bxkr.octodiary.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.bxkr.octodiary.database.dao.*
import org.bxkr.octodiary.database.entities.*

@Database(
    entities = [
        AISolutionEntity::class,
        KnowledgeBaseEntity::class,
        FlashcardEntity::class,
        StudyProgressEntity::class,
        ExamPlanEntity::class,
        StudyReminderEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OctoDiaryDatabase : RoomDatabase() {
    abstract fun aiSolutionDao(): AISolutionDao
    abstract fun knowledgeBaseDao(): KnowledgeBaseDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun studyProgressDao(): StudyProgressDao
    abstract fun examPlanDao(): ExamPlanDao
    abstract fun studyReminderDao(): StudyReminderDao

    companion object {
        @Volatile
        private var INSTANCE: OctoDiaryDatabase? = null

        fun getDatabase(context: Context): OctoDiaryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OctoDiaryDatabase::class.java,
                    "octodiary_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
