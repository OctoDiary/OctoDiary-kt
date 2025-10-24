package org.bxkr.octodiary.models.ai

import java.time.LocalDateTime

/**
 * Сообщение в AI чате
 */
data class AiChatMessage(
    val id: Long = 0,
    val chatId: String, // ID чата (homework ID, lecture ID, etc)
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val attachments: List<String> = emptyList()
)

/**
 * Контекст для AI (что ИИ знает о текущей ситуации)
 */
data class AiContext(
    val homeworkId: Long? = null,
    val subjectName: String? = null,
    val lessonTopic: String? = null,
    val previousLessonTopic: String? = null,
    val nextLessonTopic: String? = null,
    val materials: List<HomeworkMaterial> = emptyList(),
    val attachedFiles: List<String> = emptyList(),
    val studentPerformance: StudentPerformance? = null,
    val lectureNotes: String? = null
)

data class HomeworkMaterial(
    val title: String,
    val type: String,
    val content: String?
)

data class StudentPerformance(
    val averageGrade: Double,
    val subjectGrades: Map<String, Double>,
    val weakSpots: List<String>,
    val strengths: List<String>
)

/**
 * Словарная запись
 */
data class VocabularyEntry(
    val id: Long = 0,
    val word: String,
    val translation: String,
    val language: String, // "en", "de", "fr", etc
    val subjectName: String,
    val examples: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val masteryLevel: Int = 0, // 0-5, как хорошо знает слово
    val lastReviewed: Long? = null
)

/**
 * Конспект урока
 */
data class LectureNote(
    val id: Long = 0,
    val subjectName: String,
    val topic: String,
    val date: Long = System.currentTimeMillis(),
    val content: String, // Текстовый конспект
    val audioPath: String? = null, // Путь к аудиозаписи
    val keyPoints: List<String> = emptyList(),
    val generatedQuestions: List<String> = emptyList(),
    val images: List<String> = emptyList(), // Фото с доски
    val isAiGenerated: Boolean = false
)

/**
 * Тест, сгенерированный из конспекта
 */
data class GeneratedTest(
    val id: Long = 0,
    val lectureNoteId: Long,
    val questions: List<TestQuestion>,
    val createdAt: Long = System.currentTimeMillis()
)

data class TestQuestion(
    val question: String,
    val answers: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

/**
 * Результат прохождения теста
 */
data class TestAttempt(
    val id: Long = 0,
    val testId: Long,
    val score: Int, // Правильных ответов
    val total: Int, // Всего вопросов
    val wrongQuestions: List<Int>, // Индексы неправильных вопросов
    val completedAt: Long = System.currentTimeMillis()
)

/**
 * Стрик (серия дней)
 */
data class Streak(
    val id: Long = 0,
    val type: String, // "homework", "grades", "study"
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActivityDate: Long = System.currentTimeMillis()
)

/**
 * Достижение
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val unlockedAt: Long? = null,
    val progress: Int = 0, // 0-100
    val maxProgress: Int = 100
)

/**
 * Анализ сложности домашнего задания
 */
data class HomeworkDifficulty(
    val homeworkId: Long,
    val difficulty: Int, // 1-5
    val estimatedTimeMinutes: Int,
    val requiredKnowledge: List<String>,
    val tips: List<String>,
    val analyzedAt: Long = System.currentTimeMillis()
)

/**
 * Персональный план учёбы
 */
data class StudyPlan(
    val id: Long = 0,
    val date: Long,
    val tasks: List<StudyTask>,
    val bedTime: String = "21:00", // Время отхода ко сну
    val totalEstimatedMinutes: Int,
    val generatedAt: Long = System.currentTimeMillis()
)

data class StudyTask(
    val homeworkId: Long,
    val subjectName: String,
    val description: String,
    val estimatedMinutes: Int,
    val priority: Int, // 1-5
    val startTime: String, // "14:30"
    val isCompleted: Boolean = false
)

/**
 * Совет по энергии/прокрастинации
 */
data class StudyAdvice(
    val id: Long = 0,
    val type: String, // "energy", "procrastination", "motivation"
    val title: String,
    val description: String,
    val tips: List<String>,
    val createdAt: Long = System.currentTimeMillis()
)
