package org.bxkr.octodiary.offline

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Локальный дневник мыслей для записей и рефлексии.
 * Помогает отслеживать эмоции, мысли и прогресс обучения.
 */
object ThoughtDiary {

    data class ThoughtEntry(
        val id: String,
        val title: String,
        val content: String,
        val mood: Mood = Mood.NEUTRAL,
        val tags: List<String> = emptyList(),
        val category: Category = Category.GENERAL,
        val date: LocalDate = LocalDate.now(),
        val time: LocalTime = LocalTime.now(),
        val createdAt: LocalDateTime = LocalDateTime.now(),
        val isPrivate: Boolean = false,
        val attachments: List<String> = emptyList(), // пути к файлам/изображениям
        val relatedSubjects: List<String> = emptyList() // связанные предметы
    )

    enum class Mood {
        EXCELLENT, VERY_GOOD, GOOD, NEUTRAL, BAD, VERY_BAD, TERRIBLE
    }

    enum class Category {
        GENERAL,      // Общие мысли
        STUDY,        // Учёба
        EMOTIONS,     // Эмоции
        GOALS,        // Цели
        REFLECTION,   // Рефлексия
        GRATITUDE,    // Благодарность
        WORRIES,      // Беспокойства
        ACHIEVEMENTS  // Достижения
    }

    data class DiaryStats(
        val totalEntries: Int,
        val entriesThisWeek: Int,
        val entriesThisMonth: Int,
        val moodDistribution: Map<Mood, Int>,
        val categoryDistribution: Map<Category, Int>,
        val mostUsedTags: List<Pair<String, Int>>,
        val averageMood: Double,
        val streakDays: Int, // подряд дней с записями
        val favoriteWritingTime: LocalTime?
    )

    data class MoodTrend(
        val period: Period,
        val averageMood: Double,
        val trend: Trend,
        val moodChanges: List<MoodChange>
    )

    enum class Period {
        WEEK, MONTH, QUARTER, YEAR
    }

    enum class Trend {
        IMPROVING, STABLE, DECLINING
    }

    data class MoodChange(
        val date: LocalDate,
        val mood: Mood,
        val notes: String?
    )

    data class GratitudePrompt(
        val prompt: String,
        val category: Category
    )

    /**
     * Создаёт новую запись в дневнике.
     */
    fun createEntry(
        title: String,
        content: String,
        mood: Mood = Mood.NEUTRAL,
        category: Category = Category.GENERAL,
        tags: List<String> = emptyList(),
        isPrivate: Boolean = false,
        relatedSubjects: List<String> = emptyList()
    ): ThoughtEntry {
        return ThoughtEntry(
            id = generateEntryId(),
            title = title,
            content = content,
            mood = mood,
            tags = tags,
            category = category,
            isPrivate = isPrivate,
            relatedSubjects = relatedSubjects
        )
    }

    /**
     * Получает записи за определённый период.
     */
    fun getEntriesForPeriod(
        entries: List<ThoughtEntry>,
        startDate: LocalDate,
        endDate: LocalDate,
        category: Category? = null,
        mood: Mood? = null,
        tags: List<String>? = null
    ): List<ThoughtEntry> {
        return entries.filter { entry ->
            entry.date in startDate..endDate &&
            (category == null || entry.category == category) &&
            (mood == null || entry.mood == mood) &&
            (tags == null || tags.any { entry.tags.contains(it) })
        }.sortedByDescending { it.createdAt }
    }

    /**
     * Анализирует статистику дневника.
     */
    fun analyzeDiary(entries: List<ThoughtEntry>): DiaryStats {
        val now = LocalDate.now()
        val weekAgo = now.minusWeeks(1)
        val monthAgo = now.minusMonths(1)

        val entriesThisWeek = entries.count { it.date >= weekAgo }
        val entriesThisMonth = entries.count { it.date >= monthAgo }

        val moodDistribution = Mood.values().associateWith { mood ->
            entries.count { it.mood == mood }
        }

        val categoryDistribution = Category.values().associateWith { category ->
            entries.count { it.category == category }
        }

        val allTags = entries.flatMap { it.tags }
        val mostUsedTags = allTags.groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .take(10)
            .map { it.key to it.value }

        val averageMood = calculateAverageMood(entries)
        val streakDays = calculateStreak(entries)
        val favoriteWritingTime = findFavoriteWritingTime(entries)

        return DiaryStats(
            totalEntries = entries.size,
            entriesThisWeek = entriesThisWeek,
            entriesThisMonth = entriesThisMonth,
            moodDistribution = moodDistribution,
            categoryDistribution = categoryDistribution,
            mostUsedTags = mostUsedTags,
            averageMood = averageMood,
            streakDays = streakDays,
            favoriteWritingTime = favoriteWritingTime
        )
    }

    /**
     * Вычисляет среднее настроение.
     */
    private fun calculateAverageMood(entries: List<ThoughtEntry>): Double {
        if (entries.isEmpty()) return 3.5 // neutral

        val moodValues = entries.map { entry ->
            when (entry.mood) {
                Mood.EXCELLENT -> 7.0
                Mood.VERY_GOOD -> 6.0
                Mood.GOOD -> 5.0
                Mood.NEUTRAL -> 4.0
                Mood.BAD -> 3.0
                Mood.VERY_BAD -> 2.0
                Mood.TERRIBLE -> 1.0
            }
        }

        return moodValues.average()
    }

    /**
     * Вычисляет стрик (подряд дней с записями).
     */
    private fun calculateStreak(entries: List<ThoughtEntry>): Int {
        if (entries.isEmpty()) return 0

        val datesWithEntries = entries.map { it.date }.toSet()
        val today = LocalDate.now()
        var streak = 0
        var checkDate = today

        while (datesWithEntries.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        return streak
    }

    /**
     * Находит любимое время для написания записей.
     */
    private fun findFavoriteWritingTime(entries: List<ThoughtEntry>): LocalTime? {
        if (entries.isEmpty()) return null

        val timeGroups = entries.groupBy { it.time.hour }
        val mostFrequentHour = timeGroups.maxByOrNull { it.value.size }?.key ?: return null

        val timesInHour = timeGroups[mostFrequentHour] ?: return null
        val averageMinute = timesInHour.map { it.time.minute }.average().toInt()

        return LocalTime.of(mostFrequentHour, averageMinute)
    }

    /**
     * Анализирует тренд настроения за период.
     */
    fun analyzeMoodTrend(entries: List<ThoughtEntry>, period: Period): MoodTrend {
        val endDate = LocalDate.now()
        val startDate = when (period) {
            Period.WEEK -> endDate.minusWeeks(1)
            Period.MONTH -> endDate.minusMonths(1)
            Period.QUARTER -> endDate.minusMonths(3)
            Period.YEAR -> endDate.minusYears(1)
        }

        val periodEntries = entries.filter { it.date in startDate..endDate }
            .sortedBy { it.date }

        val averageMood = calculateAverageMood(periodEntries)

        val moodChanges = periodEntries.map { entry ->
            MoodChange(
                date = entry.date,
                mood = entry.mood,
                notes = if (entry.content.length > 50) entry.content.take(50) + "..." else entry.content
            )
        }

        val trend = calculateMoodTrend(periodEntries)

        return MoodTrend(
            period = period,
            averageMood = averageMood,
            trend = trend,
            moodChanges = moodChanges
        )
    }

    /**
     * Вычисляет тренд настроения.
     */
    private fun calculateMoodTrend(entries: List<ThoughtEntry>): Trend {
        if (entries.size < 2) return Trend.STABLE

        val sortedEntries = entries.sortedBy { it.date }
        val firstHalf = sortedEntries.take(sortedEntries.size / 2)
        val secondHalf = sortedEntries.takeLast(sortedEntries.size / 2)

        val firstHalfAvg = calculateAverageMood(firstHalf)
        val secondHalfAvg = calculateAverageMood(secondHalf)

        val difference = secondHalfAvg - firstHalfAvg
        val threshold = firstHalfAvg * 0.1 // 10% изменение

        return when {
            difference > threshold -> Trend.IMPROVING
            difference < -threshold -> Trend.DECLINING
            else -> Trend.STABLE
        }
    }

    /**
     * Генерирует подсказки для благодарности.
     */
    fun generateGratitudePrompts(): List<GratitudePrompt> {
        return listOf(
            GratitudePrompt("За что ты благодарен сегодня в учёбе?", Category.GRATITUDE),
            GratitudePrompt("Кто помог тебе сегодня?", Category.GRATITUDE),
            GratitudePrompt("Какой успех ты отметил недавно?", Category.ACHIEVEMENTS),
            GratitudePrompt("Что хорошего произошло на уроке?", Category.STUDY),
            GratitudePrompt("Какие у тебя есть возможности для роста?", Category.GOALS)
        )
    }

    /**
     * Генерирует подсказки для рефлексии.
     */
    fun generateReflectionPrompts(): List<String> {
        return listOf(
            "Что было самым сложным сегодня?",
            "Чему ты научился нового?",
            "Что бы ты сделал по-другому?",
            "Какие эмоции ты испытывал во время учёбы?",
            "Что помогает тебе лучше сосредотачиваться?",
            "Как ты можешь улучшить свою подготовку?",
            "Что мотивирует тебя продолжать учиться?",
            "Какие цели ты ставишь на следующую неделю?"
        )
    }

    /**
     * Создаёт шаблоны записей для разных ситуаций.
     */
    fun createEntryTemplate(category: Category): ThoughtEntry {
        val template = when (category) {
            Category.GRATITUDE -> ThoughtEntry(
                id = generateEntryId(),
                title = "Благодарность за день",
                content = "Сегодня я благодарен за...\n\nЭто помогло мне...",
                category = category,
                mood = Mood.GOOD
            )
            Category.GOALS -> ThoughtEntry(
                id = generateEntryId(),
                title = "Мои цели",
                content = "Краткосрочные цели:\n1. \n2. \n3. \n\nДолгосрочные цели:\n1. \n2. \n3.",
                category = category,
                mood = Mood.NEUTRAL
            )
            Category.REFLECTION -> ThoughtEntry(
                id = generateEntryId(),
                title = "Рефлексия дня",
                content = "Что прошло хорошо:\n\nЧто можно улучшить:\n\nУроки на будущее:",
                category = category,
                mood = Mood.NEUTRAL
            )
            Category.ACHIEVEMENTS -> ThoughtEntry(
                id = generateEntryId(),
                title = "Мои достижения",
                content = "Сегодня я:\n\nЭто важно для меня потому что:\n\nКак я отпраздную это:",
                category = category,
                mood = Mood.VERY_GOOD
            )
            Category.WORRIES -> ThoughtEntry(
                id = generateEntryId(),
                title = "Мои беспокойства",
                content = "Меня беспокоит:\n\nПочему это важно:\n\nЧто я могу сделать:",
                category = category,
                mood = Mood.BAD
            )
            else -> ThoughtEntry(
                id = generateEntryId(),
                title = "Новая запись",
                content = "Напишите свои мысли здесь...",
                category = category,
                mood = Mood.NEUTRAL
            )
        }

        return template
    }

    /**
     * Ищет записи по ключевым словам.
     */
    fun searchEntries(entries: List<ThoughtEntry>, query: String): List<ThoughtEntry> {
        val queryLower = query.lowercase()

        return entries.filter { entry ->
            entry.title.lowercase().contains(queryLower) ||
            entry.content.lowercase().contains(queryLower) ||
            entry.tags.any { it.lowercase().contains(queryLower) } ||
            entry.relatedSubjects.any { it.lowercase().contains(queryLower) }
        }.sortedByDescending { it.createdAt }
    }

    /**
     * Получает записи связанные с определённым предметом.
     */
    fun getEntriesBySubject(entries: List<ThoughtEntry>, subject: String): List<ThoughtEntry> {
        return entries.filter { entry ->
            entry.relatedSubjects.any { it.equals(subject, ignoreCase = true) } ||
            entry.tags.any { it.equals(subject, ignoreCase = true) } ||
            entry.content.contains(subject, ignoreCase = true)
        }.sortedByDescending { it.createdAt }
    }

    /**
     * Создаёт резервную копию дневника в формате JSON.
     */
    fun exportDiary(entries: List<ThoughtEntry>): String {
        // Упрощённая версия экспорта (в реальном приложении использовать JSON)
        return buildString {
            appendLine("Дневник мыслей - Экспорт")
            appendLine("Всего записей: ${entries.size}")
            appendLine("Экспортировано: ${LocalDateTime.now()}")
            appendLine()

            entries.sortedByDescending { it.createdAt }.forEach { entry ->
                appendLine("--- Запись от ${entry.date} ${entry.time} ---")
                appendLine("Заголовок: ${entry.title}")
                appendLine("Настроение: ${getMoodDescription(entry.mood)}")
                appendLine("Категория: ${getCategoryDescription(entry.category)}")
                if (entry.tags.isNotEmpty()) {
                    appendLine("Теги: ${entry.tags.joinToString(", ")}")
                }
                if (entry.relatedSubjects.isNotEmpty()) {
                    appendLine("Предметы: ${entry.relatedSubjects.joinToString(", ")}")
                }
                appendLine()
                appendLine(entry.content)
                appendLine()
                appendLine("=".repeat(50))
                appendLine()
            }
        }
    }

    /**
     * Получает текстовое описание настроения.
     */
    private fun getMoodDescription(mood: Mood): String {
        return when (mood) {
            Mood.EXCELLENT -> "Отлично"
            Mood.VERY_GOOD -> "Очень хорошо"
            Mood.GOOD -> "Хорошо"
            Mood.NEUTRAL -> "Нейтрально"
            Mood.BAD -> "Плохо"
            Mood.VERY_BAD -> "Очень плохо"
            Mood.TERRIBLE -> "Ужасно"
        }
    }

    /**
     * Получает текстовое описание категории.
     */
    private fun getCategoryDescription(category: Category): String {
        return when (category) {
            Category.GENERAL -> "Общее"
            Category.STUDY -> "Учёба"
            Category.EMOTIONS -> "Эмоции"
            Category.GOALS -> "Цели"
            Category.REFLECTION -> "Рефлексия"
            Category.GRATITUDE -> "Благодарность"
            Category.WORRIES -> "Беспокойства"
            Category.ACHIEVEMENTS -> "Достижения"
        }
    }

    /**
     * Генерирует уникальный ID записи.
     */
    private fun generateEntryId(): String {
        return "thought_${System.currentTimeMillis()}_${(0..9999).random()}"
    }

    // Оператор range для LocalDate
    private operator fun LocalDate.rangeTo(other: LocalDate) = generateSequence(this) { it.plusDays(1) }.takeWhile { it <= other }
}