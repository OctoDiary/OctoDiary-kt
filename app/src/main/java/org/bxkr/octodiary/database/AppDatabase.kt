package org.bxkr.octodiary.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.*
import org.bxkr.octodiary.database.dao.*
import org.bxkr.octodiary.database.entity.*
import org.bxkr.octodiary.models.TocEntry

@Database(
    entities = [
        KnowledgeBaseEntity::class,
        FlashcardEntity::class,
        StudyProgressEntity::class,
        ExamPlanEntity::class,
        StudyReminderEntity::class,
        AiChatMessageEntity::class,
        VocabularyEntity::class,
        LectureNoteEntity::class,
        StreakEntity::class,
        StudyPlanEntity::class,
        TocEntry::class,
        ParagraphEntity::class,
        TextbookExtractEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun knowledgeBaseDao(): KnowledgeBaseDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun studyProgressDao(): StudyProgressDao
    abstract fun examPlanDao(): ExamPlanDao
    abstract fun studyReminderDao(): StudyReminderDao
    abstract fun aiChatDao(): AiChatDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun lectureNoteDao(): LectureNoteDao
    abstract fun streakDao(): StreakDao
    abstract fun studyPlanDao(): StudyPlanDao
    abstract fun tocDao(): TocDao
    abstract fun paragraphDao(): ParagraphDao
    abstract fun textbookExtractDao(): TextbookExtractDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase? {
            return getInstance(context)
        }
        
        fun getInstance(context: Context): AppDatabase? {
            return try {
                android.util.Log.d("AppDatabase", "Попытка получить экземпляр базы данных")
                INSTANCE ?: synchronized(this) {
                    android.util.Log.d("AppDatabase", "Создание нового экземпляра базы данных")
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "octodiary_database"
                    )
                        .fallbackToDestructiveMigration()
                        // Оптимизация: добавляем кэширование и оптимизацию запросов
                        .setQueryExecutor { runnable ->
                            // Оптимизация: используем IO диспетчер для выполнения запросов
                            CoroutineScope(Dispatchers.IO).launch {
                                runnable.run()
                            }
                        }
                        .setTransactionExecutor { runnable ->
                            // Оптимизация: отдельный исполнитель для транзакций
                            CoroutineScope(Dispatchers.IO).launch {
                                runnable.run()
                            }
                        }
                        .build()
                    INSTANCE = instance
                    android.util.Log.d("AppDatabase", "Экземпляр базы данных успешно создан")
                    instance
                }
            } catch (e: Exception) {
                android.util.Log.e("AppDatabase", "Room недоступна: ${e.message}")
                android.util.Log.e("AppDatabase", "Полный стек ошибки: ", e)
                null
            }
        }
    }
}
