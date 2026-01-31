package org.bxkr.octodiary.offline


import androidx.compose.material.icons.Icons
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Локальный архив выполненных домашних заданий.
 * Хранит историю выполненных ДЗ для анализа прогресса и повторения.
 */
object HomeworkArchive {

    data class CompletedHomework(
        val id: String,
        val originalHomeworkId: String? = null, // ID из внешней системы, если есть
        val subject: String,
        val title: String,
        val description: String? = null,
        val taskType: TaskType,
        val difficulty: Difficulty = Difficulty.MEDIUM,
        val completedDate: LocalDate = LocalDate.now(),
        val actualTimeSpent: Int? = null, // реальное время выполнения в минутах
        val gradeReceived: Int? = null, // оценка за задание
        val notes: String? = null, // заметки о выполнении
        val attachments: List<String> = emptyList(), // пути к файлам/фото
        val tags: List<String> = emptyList(),
        val qualityRating: QualityRating = QualityRating.GOOD, // самооценка качества
        val createdAt: LocalDateTime = LocalDateTime.now()
    )

    enum class TaskType {
        READING,
        PROBLEMS,
        WRITING,
        MEMORIZATION,
        RESEARCH,
        PRACTICE,
        REVIEW,
        CREATIVE,
        OTHER
    }

    enum class Difficulty {
        EASY, MEDIUM, HARD, VERY_HARD
    }

    enum class QualityRating {
        POOR, FAIR, GOOD, EXCELLENT
    }

    data class ArchiveStats(
        val totalCompleted: Int,
        val thisWeek: Int,
        val thisMonth: Int,
        val averageTimePerTask: Double? = null,
        val averageGrade: Double? = null,
        val subjectBreakdown: Map<String, Int>,
        val taskTypeBreakdown: Map<TaskType, Int>,
        val qualityDistribution: Map<QualityRating, Int>,
        val mostProductiveDay: LocalDate? = null,
        val longestStreak: Int = 0
    )

    data class SubjectProgress(
        val subject: String,
        val totalCompleted: Int,
        val averageGrade: Double? = null,
        val averageTime: Double? = null,
        val improvementTrend: Trend,
        val recentGrades: List<Pair<LocalDate, Int>>,
        val weakAreas: List<String> = emptyList()
    )

    enum class Trend {
        IMPROVING, STABLE, DECLINING
    }

    /**
     * Добавляет выполненное домашнее задание в архив.
     */
    fun addCompletedHomework(
        originalHomeworkId: String? = null,
        subject: String,
        title: String,
        taskType: TaskType,
        difficulty: Difficulty = Difficulty.MEDIUM,
        actualTimeSpent: Int? = null,
        gradeReceived: Int? = null,
        notes: String? = null,
        attachments: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        qualityRating: QualityRating = QualityRating.GOOD
    ): CompletedHomework {
        return CompletedHomework(
            id = generateHomeworkId(),
            originalHomeworkId = originalHomeworkId,
            subject = subject,
            title = title,
            taskType = taskType,
            difficulty = difficulty,
            actualTimeSpent = actualTimeSpent,
            gradeReceived = gradeReceived,
            notes = notes,
            attachments = attachments,
            tags = tags,
            qualityRating = qualityRating
        )
    }

    /**
     * Получает статистику архива.
     */
    fun getArchiveStats(homeworks: List<CompletedHomework>): ArchiveStats {
        val now = LocalDate.now()
        val weekAgo = now.minusWeeks(1)
        val monthAgo = now.minusMonths(1)

        val thisWeek = homeworks.count { it.completedDate >= weekAgo }
        val thisMonth = homeworks.count { it.completedDate >= monthAgo }

        val validTimes = homeworks.mapNotNull { it.actualTimeSpent }
        val averageTimePerTask = validTimes.average().takeIf { it.isFinite() }

        val validGrades = homeworks.mapNotNull { it.gradeReceived }
        val averageGrade = validGrades.average().takeIf { it.isFinite() }

        val subjectBreakdown = homeworks.groupingBy { it.subject }.eachCount()
        val taskTypeBreakdown = homeworks.groupingBy { it.taskType }.eachCount()
        val qualityDistribution = homeworks.groupingBy { it.qualityRating }.eachCount()

        val mostProductiveDay = homeworks
            .groupingBy { it.completedDate }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

        val longestStreak = calculateLongestStreak(homeworks)

        return ArchiveStats(
            totalCompleted = homeworks.size,
            thisWeek = thisWeek,
            thisMonth = thisMonth,
            averageTimePerTask = averageTimePerTask,
            averageGrade = averageGrade,
            subjectBreakdown = subjectBreakdown,
            taskTypeBreakdown = taskTypeBreakdown,
            qualityDistribution = qualityDistribution,
            mostProductiveDay = mostProductiveDay,
            longestStreak = longestStreak
        )
    }

    /**
     * Вычисляет самый длинный стрик выполнения ДЗ.
     */
    private fun calculateLongestStreak(homeworks: List<CompletedHomework>): Int {
        if (homeworks.isEmpty()) return 0

        val datesWithHomework = homeworks.map { it.completedDate }.toSet().sorted()
        var longestStreak = 0
        var currentStreak = 0
        var previousDate: LocalDate? = null

        for (date in datesWithHomework) {
            if (previousDate == null || date == previousDate.plusDays(1)) {
                currentStreak++
                longestStreak = maxOf(longestStreak, currentStreak)
            } else {
                currentStreak = 1
            }
            previousDate = date
        }

        return longestStreak
    }

    /**
     * Анализирует прогресс по предметам.
     */
    fun analyzeSubjectProgress(
        homeworks: List<CompletedHomework>,
        subject: String,
        analysisPeriod: Int = 30 // дней для анализа
    ): SubjectProgress {
        val subjectHomeworks = homeworks.filter {
            it.subject.equals(subject, ignoreCase = true)
        }.sortedBy { it.completedDate }

        val recentHomeworks = subjectHomeworks.filter {
            it.completedDate >= LocalDate.now().minusDays(analysisPeriod.toLong())
        }

        val totalCompleted = subjectHomeworks.size
        val averageGrade = subjectHomeworks.mapNotNull { it.gradeReceived }.average().takeIf { it.isFinite() }
        val averageTime = subjectHomeworks.mapNotNull { it.actualTimeSpent }.average().takeIf { it.isFinite() }

        val recentGrades = subjectHomeworks
            .filter { it.gradeReceived != null }
            .takeLast(10)
            .map { it.completedDate to it.gradeReceived!! }

        val improvementTrend = calculateGradeTrend(recentGrades)

        val weakAreas = identifyWeakAreas(subjectHomeworks)

        return SubjectProgress(
            subject = subject,
            totalCompleted = totalCompleted,
            averageGrade = averageGrade,
            averageTime = averageTime,
            improvementTrend = improvementTrend,
            recentGrades = recentGrades,
            weakAreas = weakAreas
        )
    }

    /**
     * Вычисляет тренд оценок.
     */
    private fun calculateGradeTrend(grades: List<Pair<LocalDate, Int>>): Trend {
        if (grades.size < 3) return Trend.STABLE

        val firstHalf = grades.take(grades.size / 2).map { it.second }.average()
        val secondHalf = grades.takeLast(grades.size / 2).map { it.second }.average()

        val difference = secondHalf - firstHalf
        val threshold = firstHalf * 0.05 // 5% изменение

        return when {
            difference > threshold -> Trend.IMPROVING
            difference < -threshold -> Trend.DECLINING
            else -> Trend.STABLE
        }
    }

    /**
     * Определяет слабые места по предмету.
     */
    private fun identifyWeakAreas(homeworks: List<CompletedHomework>): List<String> {
        val qualityIssues = homeworks.filter { it.qualityRating == QualityRating.POOR }
        val lowGrades = homeworks.filter { (it.gradeReceived ?: 5) < 3 }

        val problemAreas = mutableListOf<String>()

        if (qualityIssues.size > homeworks.size * 0.3) {
            problemAreas.add("Качество выполнения ниже среднего")
        }

        if (lowGrades.size > homeworks.size * 0.2) {
            problemAreas.add("Низкие оценки за задания")
        }

        // Анализ по типам задач
        val taskTypePerformance = homeworks.groupBy { it.taskType }.mapValues { (_, tasks) ->
            tasks.mapNotNull { it.gradeReceived }.average()
        }

        taskTypePerformance.forEach { (taskType, avgGrade) ->
            if (avgGrade < 3.5) {
                problemAreas.add("Слабые результаты в ${getTaskTypeName(taskType)}")
            }
        }

        return problemAreas
    }

    /**
     * Получает выполненные ДЗ за период.
     */
    fun getCompletedInPeriod(
        homeworks: List<CompletedHomework>,
        startDate: LocalDate,
        endDate: LocalDate,
        subject: String? = null,
        taskType: TaskType? = null
    ): List<CompletedHomework> {
        return homeworks.filter { homework ->
            homework.completedDate in startDate..endDate &&
            (subject == null || homework.subject.equals(subject, ignoreCase = true)) &&
            (taskType == null || homework.taskType == taskType)
        }.sortedByDescending { it.completedDate }
    }

    /**
     * Ищет выполненные ДЗ по ключевым словам.
     */
    fun searchCompletedHomework(
        homeworks: List<CompletedHomework>,
        query: String
    ): List<CompletedHomework> {
        val queryLower = query.lowercase()

        return homeworks.filter { homework ->
            homework.title.lowercase().contains(queryLower) ||
            homework.description?.lowercase()?.contains(queryLower) == true ||
            homework.subject.lowercase().contains(queryLower) ||
            homework.notes?.lowercase()?.contains(queryLower) == true ||
            homework.tags.any { it.lowercase().contains(queryLower) }
        }.sortedByDescending { it.completedDate }
    }

    /**
     * Генерирует отчёт о продуктивности.
     */
    fun generateProductivityReport(
        homeworks: List<CompletedHomework>,
        period: Period = Period.MONTH
    ): ProductivityReport {
        val endDate = LocalDate.now()
        val startDate = when (period) {
            Period.WEEK -> endDate.minusWeeks(1)
            Period.MONTH -> endDate.minusMonths(1)
            Period.QUARTER -> endDate.minusMonths(3)
            Period.YEAR -> endDate.minusYears(1)
        }

        val periodHomeworks = getCompletedInPeriod(homeworks, startDate, endDate)

        val totalTasks = periodHomeworks.size
        val totalTime = periodHomeworks.sumOf { it.actualTimeSpent ?: 0 }
        val averageGrade = periodHomeworks.mapNotNull { it.gradeReceived }.average().takeIf { it.isFinite() }
        val completionRate = calculateCompletionRate(homeworks, startDate, endDate)

        val dailyStats = (startDate..endDate).map { date ->
            val dayTasks = periodHomeworks.filter { it.completedDate == date }
            DailyStats(
                date = date,
                tasksCompleted = dayTasks.size,
                timeSpent = dayTasks.sumOf { it.actualTimeSpent ?: 0 },
                averageGrade = dayTasks.mapNotNull { it.gradeReceived }.average().takeIf { it.isFinite() }
            )
        }.toList()

        val bestDay = dailyStats.maxByOrNull { it.tasksCompleted }
        val mostEfficientDay = dailyStats.filter { it.timeSpent > 0 }.maxByOrNull { it.tasksCompleted.toDouble() / it.timeSpent }

        val subjectPerformance = periodHomeworks.groupBy { it.subject }.mapValues { (_, tasks) ->
            SubjectPerformance(
                tasksCompleted = tasks.size,
                averageGrade = tasks.mapNotNull { it.gradeReceived }.average().takeIf { it.isFinite() },
                averageTime = tasks.mapNotNull { it.actualTimeSpent }.average().takeIf { it.isFinite() }
            )
        }

        return ProductivityReport(
            period = object : ClosedRange<LocalDate> {
                override val start: LocalDate = startDate
                override val endInclusive: LocalDate = endDate
            },
            totalTasks = totalTasks,
            totalTime = totalTime,
            averageGrade = averageGrade,
            completionRate = completionRate,
            dailyStats = dailyStats,
            bestDay = bestDay,
            mostEfficientDay = mostEfficientDay,
            subjectPerformance = subjectPerformance
        )
    }

    enum class Period {
        WEEK, MONTH, QUARTER, YEAR
    }

    data class DailyStats(
        val date: LocalDate,
        val tasksCompleted: Int,
        val timeSpent: Int,
        val averageGrade: Double?
    )

    data class SubjectPerformance(
        val tasksCompleted: Int,
        val averageGrade: Double?,
        val averageTime: Double?
    )

    data class ProductivityReport(
        val period: kotlin.ranges.ClosedRange<LocalDate>,
        val totalTasks: Int,
        val totalTime: Int,
        val averageGrade: Double?,
        val completionRate: Double,
        val dailyStats: List<DailyStats>,
        val bestDay: DailyStats?,
        val mostEfficientDay: DailyStats?,
        val subjectPerformance: Map<String, SubjectPerformance>
    )

    /**
     * Вычисляет процент выполнения запланированных заданий.
     */
    private fun calculateCompletionRate(homeworks: List<CompletedHomework>, startDate: LocalDate, endDate: LocalDate): Double {
        // Упрощённый расчёт - в реальном приложении нужно сравнивать с запланированными заданиями
        val daysInPeriod = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
        val expectedDailyTasks = 2.0 // предположение: 2 задания в день
        val expectedTotalTasks = (daysInPeriod * expectedDailyTasks).toInt()

        return if (expectedTotalTasks > 0) {
            minOf(homeworks.size.toDouble() / expectedTotalTasks, 1.0) * 100
        } else 0.0
    }

    /**
     * Экспортирует архив в читаемый формат.
     */
    fun exportArchive(homeworks: List<CompletedHomework>): String {
        return buildString {
            appendLine("Архив выполненных домашних заданий")
            appendLine("Экспорт от ${LocalDateTime.now()}")
            appendLine("Всего заданий: ${homeworks.size}")
            appendLine()

            homeworks.sortedByDescending { it.completedDate }.forEach { homework ->
                appendLine("--- ${homework.subject}: ${homework.title} ---")
                appendLine("Дата выполнения: ${homework.completedDate}")
                appendLine("Тип задания: ${getTaskTypeName(homework.taskType)}")
                appendLine("Сложность: ${homework.difficulty}")
                homework.actualTimeSpent?.let { appendLine("Затраченное время: $it мин") }
                homework.gradeReceived?.let { appendLine("Полученная оценка: $it") }
                appendLine("Качество выполнения: ${homework.qualityRating}")
                if (homework.tags.isNotEmpty()) {
                    appendLine("Теги: ${homework.tags.joinToString(", ")}")
                }
                homework.notes?.let { appendLine("Заметки: $it") }
                appendLine()
            }
        }
    }

    /**
     * Получает название типа задания на русском.
     */
    private fun getTaskTypeName(taskType: TaskType): String {
        return when (taskType) {
            TaskType.READING -> "Чтение"
            TaskType.PROBLEMS -> "Задачи"
            TaskType.WRITING -> "Письменная работа"
            TaskType.MEMORIZATION -> "Заучивание"
            TaskType.RESEARCH -> "Исследование"
            TaskType.PRACTICE -> "Практика"
            TaskType.REVIEW -> "Повторение"
            TaskType.CREATIVE -> "Творческое задание"
            TaskType.OTHER -> "Другое"
        }
    }

    /**
     * Генерирует уникальный ID выполненного ДЗ.
     */
    private fun generateHomeworkId(): String {
        return "completed_${System.currentTimeMillis()}_${(0..9999).random()}"
    }

    // Оператор range для LocalDate
    private operator fun LocalDate.rangeTo(other: LocalDate) = generateSequence(this) { it.plusDays(1) }.takeWhile { it <= other }
}


