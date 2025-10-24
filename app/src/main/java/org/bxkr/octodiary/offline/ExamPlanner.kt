package org.bxkr.octodiary.offline

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * Локальный планировщик подготовки к экзаменам.
 * Создаёт персонализированные планы подготовки на основе сложности и доступного времени.
 */
object ExamPlanner {

    data class Exam(
        val id: String,
        val subject: String,
        val examDate: LocalDate,
        val difficulty: Difficulty = Difficulty.MEDIUM,
        val topics: List<String> = emptyList(),
        val estimatedStudyHours: Int = 10,
        val currentGrade: Int? = null,
        val targetGrade: Int = 4,
        val notes: String? = null,
        val createdAt: LocalDateTime = LocalDateTime.now()
    )

    enum class Difficulty {
        EASY, MEDIUM, HARD, VERY_HARD
    }

    data class StudySession(
        val id: String,
        val examId: String,
        val date: LocalDate,
        val startTime: LocalTime,
        val duration: Int, // в минутах
        val topics: List<String>,
        val completed: Boolean = false,
        val notes: String? = null
    )

    data class StudyPlan(
        val exam: Exam,
        val sessions: List<StudySession>,
        val totalHours: Int,
        val dailyHours: Double,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val recommendations: List<String>
    )

    data class StudyProgress(
        val examId: String,
        val completedSessions: Int,
        val totalSessions: Int,
        val completedHours: Int,
        val totalHours: Int,
        val progressPercentage: Double,
        val daysRemaining: Long,
        val onTrack: Boolean
    )

    /**
     * Создаёт план подготовки к экзамену.
     */
    fun createStudyPlan(
        exam: Exam,
        availableHoursPerDay: Int = 2,
        startDate: LocalDate = LocalDate.now(),
        preferences: StudyPreferences = StudyPreferences()
    ): StudyPlan {
        val daysUntilExam = ChronoUnit.DAYS.between(startDate, exam.examDate).toInt()
        require(daysUntilExam > 0) { "Дата экзамена должна быть в будущем" }

        val totalHours = calculateRequiredHours(exam, daysUntilExam)
        val dailyHours = if (daysUntilExam > 0) totalHours.toDouble() / daysUntilExam else totalHours.toDouble()
        val adjustedDailyHours = minOf(dailyHours, availableHoursPerDay.toDouble())

        val sessions = generateStudySessions(
            exam = exam,
            startDate = startDate,
            daysUntilExam = daysUntilExam,
            dailyHours = adjustedDailyHours,
            preferences = preferences
        )

        val recommendations = generateRecommendations(exam, daysUntilExam, totalHours)

        return StudyPlan(
            exam = exam,
            sessions = sessions,
            totalHours = totalHours,
            dailyHours = adjustedDailyHours,
            startDate = startDate,
            endDate = exam.examDate.minusDays(1),
            recommendations = recommendations
        )
    }

    data class StudyPreferences(
        val preferredStudyTimes: List<LocalTime> = listOf(
            LocalTime.of(19, 0), // Вечером
            LocalTime.of(10, 0)  // Утром
        ),
        val breakDuration: Int = 15, // минуты между сессиями
        val maxSessionDuration: Int = 90, // максимальная длительность сессии
        val weekendStudy: Boolean = true,
        val avoidExamDay: Boolean = true
    )

    /**
     * Вычисляет необходимое количество часов подготовки.
     */
    private fun calculateRequiredHours(exam: Exam, daysUntilExam: Int): Int {
        val baseHours = when (exam.difficulty) {
            Difficulty.EASY -> 5
            Difficulty.MEDIUM -> 10
            Difficulty.HARD -> 15
            Difficulty.VERY_HARD -> 25
        }

        // Корректировка на основе текущей оценки и целевой
        val gradeMultiplier = when {
            exam.currentGrade == null -> 1.0
            exam.targetGrade > exam.currentGrade -> 1.2
            exam.targetGrade == exam.currentGrade -> 0.8
            else -> 0.5
        }

        // Корректировка на время подготовки
        val timeMultiplier = when {
            daysUntilExam >= 30 -> 1.0
            daysUntilExam >= 14 -> 1.1
            daysUntilExam >= 7 -> 1.3
            else -> 1.5
        }

        return (baseHours * gradeMultiplier * timeMultiplier).toInt().coerceAtLeast(1)
    }

    /**
     * Генерирует сессии изучения.
     */
    private fun generateStudySessions(
        exam: Exam,
        startDate: LocalDate,
        daysUntilExam: Int,
        dailyHours: Double,
        preferences: StudyPreferences
    ): List<StudySession> {
        val sessions = mutableListOf<StudySession>()
        val topics = exam.topics.ifEmpty { listOf("Общие темы") }

        var currentDate = startDate
        var sessionCount = 0
        val totalSessionsNeeded = (dailyHours * daysUntilExam / 0.5).toInt() // Предполагаем 30-мин сессии

        while (currentDate < exam.examDate && sessions.size < totalSessionsNeeded) {
            // Пропускаем выходные если не разрешено
            if (!preferences.weekendStudy && (currentDate.dayOfWeek.value >= 6)) {
                currentDate = currentDate.plusDays(1)
                continue
            }

            // Пропускаем день экзамена если нужно
            if (preferences.avoidExamDay && currentDate == exam.examDate.minusDays(1)) {
                currentDate = currentDate.plusDays(1)
                continue
            }

            val sessionsForDay = calculateSessionsForDay(dailyHours, preferences.maxSessionDuration)

            for (sessionIndex in 0 until sessionsForDay) {
                if (sessions.size >= totalSessionsNeeded) break

                val sessionTopics = distributeTopicsFairly(topics, sessionCount, totalSessionsNeeded)
                val startTime = preferences.preferredStudyTimes.getOrElse(sessionIndex) { LocalTime.of(19, 0) }
                val duration = minOf(60, preferences.maxSessionDuration) // 30-60 мин сессии

                sessions.add(StudySession(
                    id = generateSessionId(),
                    examId = exam.id,
                    date = currentDate,
                    startTime = startTime.plusMinutes((sessionIndex * (duration + preferences.breakDuration)).toLong()),
                    duration = duration,
                    topics = sessionTopics
                ))

                sessionCount++
            }

            currentDate = currentDate.plusDays(1)
        }

        return sessions
    }

    /**
     * Вычисляет количество сессий в день.
     */
    private fun calculateSessionsForDay(dailyHours: Double, maxSessionDuration: Int): Int {
        val sessionDurationHours = maxSessionDuration / 60.0
        return maxOf(1, (dailyHours / sessionDurationHours).toInt())
    }

    /**
     * Распределяет темы равномерно по сессиям.
     */
    private fun distributeTopicsFairly(topics: List<String>, sessionIndex: Int, totalSessions: Int): List<String> {
        if (topics.isEmpty()) return listOf("Общие темы")

        val topicsPerSession = maxOf(1, topics.size / totalSessions)
        val startIndex = (sessionIndex * topicsPerSession) % topics.size
        val endIndex = minOf(startIndex + topicsPerSession, topics.size)

        return topics.subList(startIndex, endIndex)
    }

    /**
     * Генерирует рекомендации по подготовке.
     */
    private fun generateRecommendations(exam: Exam, daysUntilExam: Int, totalHours: Int): List<String> {
        val recommendations = mutableListOf<String>()

        when {
            daysUntilExam > 30 -> {
                recommendations.add("У вас достаточно времени. Рекомендуется регулярное повторение материала.")
                recommendations.add("Разделите подготовку на этапы: изучение → практика → повторение.")
            }
            daysUntilExam > 14 -> {
                recommendations.add("Осталось ${daysUntilExam} дней. Увеличьте интенсивность подготовки.")
                recommendations.add("Сосредоточьтесь на слабых темах и практике.")
            }
            daysUntilExam > 7 -> {
                recommendations.add("Критический период! Ежедневные занятия по ${totalHours / daysUntilExam} часов.")
                recommendations.add("Решайте как можно больше задач и примеров.")
            }
            else -> {
                recommendations.add("Экстренная подготовка! ${totalHours} часов за ${daysUntilExam} дней.")
                recommendations.add("Повторяйте ключевые темы и формулы ежедневно.")
            }
        }

        when (exam.difficulty) {
            Difficulty.EASY -> recommendations.add("Предмет несложный. Достаточно общего повторения.")
            Difficulty.MEDIUM -> recommendations.add("Стандартная подготовка с акцентом на практику.")
            Difficulty.HARD -> recommendations.add("Требуется глубокое изучение и много практики.")
            Difficulty.VERY_HARD -> recommendations.add("Интенсивная подготовка с консультациями преподавателя.")
        }

        if (exam.targetGrade > (exam.currentGrade ?: 3)) {
            recommendations.add("Целевая оценка выше текущей. Увеличьте время на слабые темы.")
        }

        return recommendations
    }

    /**
     * Отслеживает прогресс подготовки.
     */
    fun calculateProgress(exam: Exam, sessions: List<StudySession>): StudyProgress {
        val completedSessions = sessions.count { it.completed }
        val totalSessions = sessions.size
        val completedHours = sessions.filter { it.completed }.sumOf { it.duration } / 60
        val totalHours = sessions.sumOf { it.duration } / 60
        val progressPercentage = if (totalSessions > 0) (completedSessions.toDouble() / totalSessions) * 100 else 0.0

        val daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), exam.examDate).coerceAtLeast(0)
        val expectedProgress = calculateExpectedProgress(exam, daysRemaining)
        val onTrack = progressPercentage >= expectedProgress * 0.9 // 90% от ожидаемого

        return StudyProgress(
            examId = exam.id,
            completedSessions = completedSessions,
            totalSessions = totalSessions,
            completedHours = completedHours,
            totalHours = totalHours,
            progressPercentage = progressPercentage,
            daysRemaining = daysRemaining,
            onTrack = onTrack
        )
    }

    /**
     * Вычисляет ожидаемый прогресс на текущий момент.
     */
    private fun calculateExpectedProgress(exam: Exam, daysRemaining: Long): Double {
        val totalDays = ChronoUnit.DAYS.between(exam.createdAt.toLocalDate(), exam.examDate)
        val daysPassed = totalDays - daysRemaining

        return if (totalDays > 0) (daysPassed.toDouble() / totalDays) * 100 else 0.0
    }

    /**
     * Получает экзамены, требующие внимания.
     */
    fun getExamsNeedingAttention(exams: List<Exam>, sessions: Map<String, List<StudySession>>): List<Exam> {
        val now = LocalDate.now()

        return exams.filter { exam ->
            val daysUntilExam = ChronoUnit.DAYS.between(now, exam.examDate)

            // Экзамены в ближайшие 7 дней
            if (daysUntilExam <= 7 && daysUntilExam >= 0) {
                val progress = calculateProgress(exam, sessions[exam.id] ?: emptyList())
                !progress.onTrack // Не успеваем по плану
            } else {
                false
            }
        }
    }

    /**
     * Обновляет план подготовки на основе прогресса.
     */
    fun adjustStudyPlan(
        originalPlan: StudyPlan,
        completedSessions: List<String>,
        newAvailableHours: Int? = null,
        extendedDeadline: LocalDate? = null
    ): StudyPlan {
        val adjustedExam = if (extendedDeadline != null) {
            originalPlan.exam.copy(examDate = extendedDeadline)
        } else {
            originalPlan.exam
        }

        val adjustedSessions = originalPlan.sessions.map { session ->
            session.copy(completed = session.id in completedSessions)
        }

        val remainingSessions = adjustedSessions.filter { !it.completed }
        val remainingHours = remainingSessions.sumOf { it.duration } / 60
        val daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), adjustedExam.examDate).toInt()

        val newDailyHours = if (newAvailableHours != null) {
            minOf(newAvailableHours.toDouble(), originalPlan.dailyHours)
        } else if (daysRemaining > 0) {
            remainingHours.toDouble() / daysRemaining
        } else {
            originalPlan.dailyHours
        }

        return originalPlan.copy(
            exam = adjustedExam,
            sessions = adjustedSessions,
            dailyHours = newDailyHours
        )
    }

    /**
     * Генерирует уникальный ID сессии.
     */
    private fun generateSessionId(): String {
        return "session_${System.currentTimeMillis()}_${(0..9999).random()}"
    }

    /**
     * Создаёт шаблоны планов для разных типов экзаменов.
     */
    fun createExamTemplate(examType: ExamType, subject: String, examDate: LocalDate): Exam {
        val (topics, difficulty, hours) = when (examType) {
            ExamType.MATH -> Triple(listOf("Алгебра", "Геометрия", "Тригонометрия"), Difficulty.HARD, 20)
            ExamType.LANGUAGE -> Triple(listOf("Грамматика", "Лексика", "Чтение", "Аудирование"), Difficulty.MEDIUM, 15)
            ExamType.SCIENCE -> Triple(listOf("Теория", "Эксперименты", "Формулы"), Difficulty.HARD, 18)
            ExamType.HISTORY -> Triple(listOf("Даты", "События", "Персоналии", "Причины"), Difficulty.MEDIUM, 12)
            ExamType.LITERATURE -> Triple(listOf("Произведения", "Авторы", "Темы", "Анализ"), Difficulty.MEDIUM, 14)
        }

        return Exam(
            id = generateExamId(),
            subject = subject,
            examDate = examDate,
            difficulty = difficulty,
            topics = topics,
            estimatedStudyHours = hours
        )
    }

    enum class ExamType {
        MATH, LANGUAGE, SCIENCE, HISTORY, LITERATURE
    }

    /**
     * Генерирует уникальный ID экзамена.
     */
    private fun generateExamId(): String {
        return "exam_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
}