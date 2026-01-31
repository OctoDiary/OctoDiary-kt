package org.bxkr.octodiary.offline


import androidx.compose.material.icons.Icons
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * Локальный трекер учебных привычек.
 * Отслеживает выполнение различных учебных привычек и анализирует прогресс.
 */
object HabitTracker {

    data class Habit(
        val id: String,
        val name: String,
        val description: String? = null,
        val category: HabitCategory,
        val targetFrequency: Frequency,
        val targetValue: Int = 1, // для количественных привычек (например, 30 минут чтения)
        val unit: String? = null, // единица измерения (минуты, страницы, etc.)
        val color: String = "#4CAF50", // цвет для отображения
        val icon: String? = null,
        val isActive: Boolean = true,
        val createdAt: LocalDate = LocalDate.now(),
        val reminderEnabled: Boolean = false,
        val reminderTime: LocalTime? = null
    )

    enum class HabitCategory {
        STUDY_TIME, // Время занятий
        EXERCISE,   // Физическая активность
        READING,    // Чтение
        HOMEWORK,   // Выполнение ДЗ
        REVIEW,     // Повторение материала
        HEALTH,     // Здоровье (сон, питание)
        SOCIAL,     // Социальная активность
        OTHER       // Другое
    }

    enum class Frequency {
        DAILY,      // Ежедневно
        WEEKLY,     // Еженедельно
        WEEKDAYS,   // По будням
        WEEKENDS    // По выходным
    }

    data class HabitEntry(
        val id: String,
        val habitId: String,
        val date: LocalDate,
        val value: Int = 1, // количество выполнений или значение
        val notes: String? = null,
        val completed: Boolean = true
    )

    data class HabitStats(
        val habit: Habit,
        val currentStreak: Int,
        val longestStreak: Int,
        val totalCompletions: Int,
        val completionRate: Double, // процент выполнения за период
        val averageValue: Double,
        val lastCompleted: LocalDate?,
        val nextDue: LocalDate?
    )

    data class HabitProgress(
        val habitId: String,
        val period: Period,
        val completedDays: Int,
        val totalDays: Int,
        val completionRate: Double,
        val trend: Trend
    )

    enum class Period {
        WEEK, MONTH, QUARTER, YEAR
    }

    enum class Trend {
        IMPROVING, STABLE, DECLINING
    }

    /**
     * Создаёт новую привычку.
     */
    fun createHabit(
        name: String,
        category: HabitCategory,
        frequency: Frequency,
        description: String? = null,
        targetValue: Int = 1,
        unit: String? = null,
        color: String = "#4CAF50"
    ): Habit {
        return Habit(
            id = generateHabitId(),
            name = name,
            description = description,
            category = category,
            targetFrequency = frequency,
            targetValue = targetValue,
            unit = unit,
            color = color
        )
    }

    /**
     * Добавляет запись о выполнении привычки.
     */
    fun addHabitEntry(
        habitId: String,
        date: LocalDate = LocalDate.now(),
        value: Int = 1,
        notes: String? = null
    ): HabitEntry {
        return HabitEntry(
            id = generateEntryId(),
            habitId = habitId,
            date = date,
            value = value,
            notes = notes
        )
    }

    /**
     * Вычисляет статистику привычки.
     */
    fun calculateStats(habit: Habit, entries: List<HabitEntry>): HabitStats {
        val habitEntries = entries.filter { it.habitId == habit.id && it.completed }
        val sortedEntries = habitEntries.sortedBy { it.date }

        val currentStreak = calculateCurrentStreak(habit, sortedEntries)
        val longestStreak = calculateLongestStreak(habit, sortedEntries)
        val totalCompletions = habitEntries.size
        val lastCompleted = sortedEntries.lastOrNull()?.date

        // Вычисляем completion rate за последние 30 дней
        val thirtyDaysAgo = LocalDate.now().minusDays(30)
        val recentEntries = habitEntries.filter { it.date >= thirtyDaysAgo }
        val expectedDays = calculateExpectedDays(habit, thirtyDaysAgo, LocalDate.now())
        val completionRate = if (expectedDays > 0) (recentEntries.size.toDouble() / expectedDays) * 100 else 0.0

        val averageValue = if (habitEntries.isNotEmpty()) habitEntries.map { it.value }.average() else 0.0

        val nextDue = calculateNextDueDate(habit, lastCompleted)

        return HabitStats(
            habit = habit,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalCompletions = totalCompletions,
            completionRate = completionRate,
            averageValue = averageValue,
            lastCompleted = lastCompleted,
            nextDue = nextDue
        )
    }

    /**
     * Вычисляет текущий стрик (серию подряд выполненных дней).
     */
    private fun calculateCurrentStreak(habit: Habit, entries: List<HabitEntry>): Int {
        if (entries.isEmpty()) return 0

        val today = LocalDate.now()
        var streak = 0
        var checkDate = today

        while (true) {
            val hasEntryOnDate = entries.any { it.date == checkDate }
            if (!hasEntryOnDate && shouldHaveEntry(habit, checkDate)) {
                break // Прерываем стрик если должен был быть entry но его нет
            } else if (hasEntryOnDate) {
                streak++
            }

            checkDate = checkDate.minusDays(1)

            // Ограничиваем проверку последними 365 днями
            if (ChronoUnit.DAYS.between(checkDate, today) > 365) break
        }

        return streak
    }

    /**
     * Вычисляет самый длинный стрик.
     */
    private fun calculateLongestStreak(habit: Habit, entries: List<HabitEntry>): Int {
        if (entries.isEmpty()) return 0

        var longestStreak = 0
        var currentStreak = 0
        var lastDate: LocalDate? = null

        for (entry in entries.sortedBy { it.date }) {
            if (lastDate == null || entry.date == lastDate.plusDays(1)) {
                currentStreak++
            } else if (shouldHaveEntry(habit, entry.date.minusDays(1))) {
                // Проверяем был ли пропуск
                currentStreak = 1
            } else {
                currentStreak++
            }

            longestStreak = maxOf(longestStreak, currentStreak)
            lastDate = entry.date
        }

        return longestStreak
    }

    /**
     * Проверяет, должен ли быть entry в указанную дату согласно частоте.
     */
    private fun shouldHaveEntry(habit: Habit, date: LocalDate): Boolean {
        return when (habit.targetFrequency) {
            Frequency.DAILY -> true
            Frequency.WEEKLY -> true // Еженедельно - проверяем отдельно
            Frequency.WEEKDAYS -> date.dayOfWeek.value <= 5 // Пн-Пт
            Frequency.WEEKENDS -> date.dayOfWeek.value >= 6 // Сб-Вс
        }
    }

    /**
     * Вычисляет ожидаемое количество дней для привычки в период.
     */
    private fun calculateExpectedDays(habit: Habit, startDate: LocalDate, endDate: LocalDate): Int {
        return (startDate..endDate).count { date -> shouldHaveEntry(habit, date) }
    }

    /**
     * Вычисляет следующую дату выполнения.
     */
    private fun calculateNextDueDate(habit: Habit, lastCompleted: LocalDate?): LocalDate? {
        if (lastCompleted == null) return LocalDate.now()

        return when (habit.targetFrequency) {
            Frequency.DAILY -> lastCompleted.plusDays(1)
            Frequency.WEEKLY -> lastCompleted.plusWeeks(1)
            Frequency.WEEKDAYS -> {
                val next = lastCompleted.plusDays(1)
                if (next.dayOfWeek.value <= 5) next else next.plusDays(8 - next.dayOfWeek.value.toLong())
            }
            Frequency.WEEKENDS -> {
                val next = lastCompleted.plusDays(1)
                if (next.dayOfWeek.value >= 6) next else next.plusDays(6 - next.dayOfWeek.value.toLong())
            }
        }
    }

    /**
     * Анализирует прогресс привычки за период.
     */
    fun analyzeProgress(habit: Habit, entries: List<HabitEntry>, period: Period): HabitProgress {
        val endDate = LocalDate.now()
        val startDate = when (period) {
            Period.WEEK -> endDate.minusWeeks(1)
            Period.MONTH -> endDate.minusMonths(1)
            Period.QUARTER -> endDate.minusMonths(3)
            Period.YEAR -> endDate.minusYears(1)
        }

        val periodEntries = entries.filter { it.date >= startDate && it.date <= endDate && it.habitId == habit.id }
        val completedDays = periodEntries.count { it.completed }
        val totalDays = calculateExpectedDays(habit, startDate, endDate)
        val completionRate = if (totalDays > 0) (completedDays.toDouble() / totalDays) * 100 else 0.0

        val trend = calculateTrend(habit, entries, period)

        return HabitProgress(
            habitId = habit.id,
            period = period,
            completedDays = completedDays,
            totalDays = totalDays,
            completionRate = completionRate,
            trend = trend
        )
    }

    /**
     * Вычисляет тренд привычки.
     */
    private fun calculateTrend(habit: Habit, entries: List<HabitEntry>, period: Period): Trend {
        val periods = 4 // Сравниваем последние 4 периода
        val periodLength = when (period) {
            Period.WEEK -> 7
            Period.MONTH -> 30
            Period.QUARTER -> 90
            Period.YEAR -> 365
        }

        val recentPeriods = (0 until periods).map { i ->
            val endDate = LocalDate.now().minusDays(i * periodLength.toLong())
            val startDate = endDate.minusDays(periodLength.toLong() - 1)
            val periodEntries = entries.filter { it.date >= startDate && it.date <= endDate && it.habitId == habit.id }
            periodEntries.size.toDouble()
        }

        if (recentPeriods.size < 2) return Trend.STABLE

        val firstHalf = recentPeriods.take(periods / 2).average()
        val secondHalf = recentPeriods.takeLast(periods / 2).average()

        val difference = secondHalf - firstHalf
        val threshold = firstHalf * 0.1 // 10% изменение

        return when {
            difference > threshold -> Trend.IMPROVING
            difference < -threshold -> Trend.DECLINING
            else -> Trend.STABLE
        }
    }

    /**
     * Получает шаблоны привычек для разных категорий.
     */
    fun getHabitTemplates(): Map<HabitCategory, List<HabitTemplate>> {
        return mapOf(
            HabitCategory.STUDY_TIME to listOf(
                HabitTemplate("Уроки по 30 минут", "Ежедневные занятия по 30 минут", 30, "минут", Frequency.DAILY),
                HabitTemplate("Подготовка к экзаменам", "Ежедневная подготовка", 60, "минут", Frequency.WEEKDAYS),
                HabitTemplate("Повторение материала", "Еженедельное повторение", 90, "минут", Frequency.WEEKLY)
            ),
            HabitCategory.READING to listOf(
                HabitTemplate("Чтение книг", "Ежедневное чтение", 20, "страниц", Frequency.DAILY),
                HabitTemplate("Учебная литература", "Чтение учебников", 30, "минут", Frequency.WEEKDAYS)
            ),
            HabitCategory.EXERCISE to listOf(
                HabitTemplate("Физическая активность", "Ежедневная активность", 30, "минут", Frequency.DAILY),
                HabitTemplate("Спорт", "Занятия спортом", 60, "минут", Frequency.WEEKENDS)
            ),
            HabitCategory.HEALTH to listOf(
                HabitTemplate("Здоровый сон", "7-8 часов сна", 8, "часов", Frequency.DAILY),
                HabitTemplate("Питание", "Здоровое питание", 3, "приёма пищи", Frequency.DAILY)
            )
        )
    }

    data class HabitTemplate(
        val name: String,
        val description: String,
        val targetValue: Int,
        val unit: String,
        val frequency: Frequency
    )

    /**
     * Создаёт привычку из шаблона.
     */
    fun createHabitFromTemplate(template: HabitTemplate, category: HabitCategory): Habit {
        return createHabit(
            name = template.name,
            category = category,
            frequency = template.frequency,
            description = template.description,
            targetValue = template.targetValue,
            unit = template.unit
        )
    }

    /**
     * Получает привычки, требующие внимания.
     */
    fun getHabitsNeedingAttention(habits: List<Habit>, allEntries: List<HabitEntry>): List<Habit> {
        return habits.filter { habit ->
            val stats = calculateStats(habit, allEntries)
            val daysSinceLast = if (stats.lastCompleted != null) {
                ChronoUnit.DAYS.between(stats.lastCompleted, LocalDate.now())
            } else {
                ChronoUnit.DAYS.between(habit.createdAt, LocalDate.now())
            }

            // Привычки, которые не выполнялись более 3 дней (для ежедневных)
            when (habit.targetFrequency) {
                Frequency.DAILY -> daysSinceLast > 3
                Frequency.WEEKLY -> daysSinceLast > 10
                Frequency.WEEKDAYS -> {
                    val today = LocalDate.now()
                    if (today.dayOfWeek.value <= 5) daysSinceLast > 3 else false
                }
                Frequency.WEEKENDS -> {
                    val today = LocalDate.now()
                    if (today.dayOfWeek.value >= 6) daysSinceLast > 3 else false
                }
            }
        }
    }

    /**
     * Генерирует отчёт по всем привычкам.
     */
    fun generateHabitsReport(habits: List<Habit>, entries: List<HabitEntry>): HabitsReport {
        val habitStats = habits.map { habit ->
            calculateStats(habit, entries)
        }

        val totalHabits = habits.size
        val activeHabits = habits.count { it.isActive }
        val averageCompletionRate = habitStats.map { it.completionRate }.average()
        val bestStreak = habitStats.maxOfOrNull { it.longestStreak } ?: 0
        val mostConsistentHabit = habitStats.maxByOrNull { it.completionRate }

        val categoryStats = habitStats.groupBy { it.habit.category }.mapValues { (_, stats) ->
            CategoryStats(
                habitCount = stats.size,
                averageCompletionRate = stats.map { it.completionRate }.average(),
                bestStreak = stats.maxOf { it.longestStreak }
            )
        }

        return HabitsReport(
            totalHabits = totalHabits,
            activeHabits = activeHabits,
            averageCompletionRate = averageCompletionRate,
            bestStreak = bestStreak,
            mostConsistentHabit = mostConsistentHabit?.habit?.name,
            categoryStats = categoryStats,
            habitStats = habitStats
        )
    }

    data class CategoryStats(
        val habitCount: Int,
        val averageCompletionRate: Double,
        val bestStreak: Int
    )

    data class HabitsReport(
        val totalHabits: Int,
        val activeHabits: Int,
        val averageCompletionRate: Double,
        val bestStreak: Int,
        val mostConsistentHabit: String?,
        val categoryStats: Map<HabitCategory, CategoryStats>,
        val habitStats: List<HabitStats>
    )

    /**
     * Генерирует уникальный ID привычки.
     */
    private fun generateHabitId(): String {
        return "habit_${System.currentTimeMillis()}_${(0..9999).random()}"
    }

    /**
     * Генерирует уникальный ID записи.
     */
    private fun generateEntryId(): String {
        return "entry_${System.currentTimeMillis()}_${(0..9999).random()}"
    }

    // Оператор range для LocalDate
    private operator fun LocalDate.rangeTo(other: LocalDate) = generateSequence(this) { it.plusDays(1) }.takeWhile { it <= other }
}


