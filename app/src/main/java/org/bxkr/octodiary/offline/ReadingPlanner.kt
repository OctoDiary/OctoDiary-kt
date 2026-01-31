package org.bxkr.octodiary.offline


import androidx.compose.material.icons.Icons
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Локальный планировщик чтения для организации чтения книг.
 * Помогает планировать чтение, отслеживать прогресс и достигать целей.
 */
object ReadingPlanner {

    data class Book(
        val id: String,
        val title: String,
        val author: String? = null,
        val genre: String? = null,
        val pageCount: Int? = null,
        val language: Language = Language.RUSSIAN,
        val difficulty: Difficulty = Difficulty.MEDIUM,
        val addedDate: LocalDate = LocalDate.now(),
        val targetCompletionDate: LocalDate? = null,
        val isCompleted: Boolean = false,
        val completionDate: LocalDate? = null,
        val rating: Int? = null, // 1-5
        val review: String? = null,
        val tags: List<String> = emptyList()
    )

    enum class Language {
        RUSSIAN, ENGLISH, GERMAN, FRENCH, SPANISH, OTHER
    }

    enum class Difficulty {
        EASY, MEDIUM, HARD, EXPERT
    }

    data class ReadingSession(
        val id: String,
        val bookId: String,
        val date: LocalDate,
        val startPage: Int,
        val endPage: Int,
        val duration: Int, // минуты
        val notes: String? = null,
        val rating: Int? = null, // оценка сессии 1-5
        val createdAt: LocalDateTime = LocalDateTime.now()
    )

    data class ReadingGoal(
        val id: String,
        val type: GoalType,
        val targetValue: Int,
        val period: Period,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val isCompleted: Boolean = false,
        val currentProgress: Int = 0,
        val description: String? = null
    )

    enum class GoalType {
        BOOKS_PER_PERIOD,    // книг за период
        PAGES_PER_PERIOD,    // страниц за период
        HOURS_PER_PERIOD,    // часов за период
        BOOKS_PER_MONTH,     // книг в месяц
        STREAK_DAYS         // дней подряд чтения
    }

    enum class Period {
        WEEK, MONTH, QUARTER, YEAR
    }

    data class ReadingPlan(
        val book: Book,
        val sessions: List<ReadingSession>,
        val dailyGoal: Int, // страниц в день
        val totalDays: Int,
        val startDate: LocalDate,
        val targetDate: LocalDate,
        val progressPercentage: Double
    )

    data class ReadingStats(
        val totalBooks: Int,
        val completedBooks: Int,
        val totalPages: Int,
        val totalReadingTime: Int, // минуты
        val averageRating: Double?,
        val favoriteGenre: String?,
        val readingStreak: Int,
        val booksThisMonth: Int,
        val pagesThisMonth: Int,
        val averageSessionTime: Double,
        val mostProductiveDay: LocalDate?,
        val readingSpeed: Double // страниц в час
    )

    /**
     * Создаёт план чтения для книги.
     */
    fun createReadingPlan(
        book: Book,
        dailyPages: Int = 20,
        startDate: LocalDate = LocalDate.now(),
        maxDays: Int = 30
    ): ReadingPlan {
        val totalPages = book.pageCount ?: 200 // предположение по умолчанию
        val totalDays = minOf((totalPages / dailyPages) + 1, maxDays)
        val targetDate = startDate.plusDays(totalDays.toLong() - 1)

        // Создаём сессии чтения
        val sessions = mutableListOf<ReadingSession>()
        var currentPage = 1

        for (day in 0 until totalDays) {
            val sessionDate = startDate.plusDays(day.toLong())
            val startPage = currentPage
            val endPage = minOf(currentPage + dailyPages - 1, totalPages)

            if (startPage <= totalPages) {
                sessions.add(ReadingSession(
                    id = generateSessionId(),
                    bookId = book.id,
                    date = sessionDate,
                    startPage = startPage,
                    endPage = endPage,
                    duration = 30 // предполагаемая длительность
                ))
                currentPage = endPage + 1
            }
        }

        val progressPercentage = 0.0 // новый план

        return ReadingPlan(
            book = book,
            sessions = sessions,
            dailyGoal = dailyPages,
            totalDays = totalDays,
            startDate = startDate,
            targetDate = targetDate,
            progressPercentage = progressPercentage
        )
    }

    /**
     * Вычисляет статистику чтения.
     */
    fun calculateReadingStats(
        books: List<Book>,
        sessions: List<ReadingSession>
    ): ReadingStats {
        val completedBooks = books.count { it.isCompleted }
        val totalPages = sessions.sumOf { it.endPage - it.startPage + 1 }
        val totalReadingTime = sessions.sumOf { it.duration }
        val averageRating = books.mapNotNull { it.rating }.average().takeIf { it.isFinite() }

        val genreCount = books.groupingBy { it.genre }.eachCount()
        val favoriteGenre = genreCount.maxByOrNull { it.value }?.key

        val readingStreak = calculateReadingStreak(sessions)

        val currentMonth = LocalDate.now().month
        val currentYear = LocalDate.now().year
        val thisMonthSessions = sessions.filter { it.date.month == currentMonth && it.date.year == currentYear }
        val booksThisMonth = thisMonthSessions.distinctBy { it.bookId }.size
        val pagesThisMonth = thisMonthSessions.sumOf { it.endPage - it.startPage + 1 }

        val averageSessionTime = sessions.map { it.duration }.average()
        val mostProductiveDay = sessions.groupBy { it.date }
            .mapValues { (_, daySessions) -> daySessions.sumOf { it.endPage - it.startPage + 1 } }
            .maxByOrNull { it.value }?.key

        val readingSpeed = if (totalReadingTime > 0) (totalPages.toDouble() / totalReadingTime) * 60 else 0.0

        return ReadingStats(
            totalBooks = books.size,
            completedBooks = completedBooks,
            totalPages = totalPages,
            totalReadingTime = totalReadingTime,
            averageRating = averageRating,
            favoriteGenre = favoriteGenre,
            readingStreak = readingStreak,
            booksThisMonth = booksThisMonth,
            pagesThisMonth = pagesThisMonth,
            averageSessionTime = averageSessionTime,
            mostProductiveDay = mostProductiveDay,
            readingSpeed = readingSpeed
        )
    }

    /**
     * Вычисляет стрик чтения (подряд дней с сессиями).
     */
    private fun calculateReadingStreak(sessions: List<ReadingSession>): Int {
        if (sessions.isEmpty()) return 0

        val readingDays = sessions.map { it.date }.distinct().sorted().reversed()
        var streak = 0
        var expectedDate = LocalDate.now()

        for (date in readingDays) {
            if (date == expectedDate || date == expectedDate.minusDays(1)) {
                streak++
                expectedDate = date.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }

    /**
     * Создаёт цель чтения.
     */
    fun createReadingGoal(
        type: GoalType,
        targetValue: Int,
        period: Period,
        description: String? = null
    ): ReadingGoal {
        val startDate = LocalDate.now()
        val endDate = when (period) {
            Period.WEEK -> startDate.plusWeeks(1)
            Period.MONTH -> startDate.plusMonths(1)
            Period.QUARTER -> startDate.plusMonths(3)
            Period.YEAR -> startDate.plusYears(1)
        }

        return ReadingGoal(
            id = generateGoalId(),
            type = type,
            targetValue = targetValue,
            period = period,
            startDate = startDate,
            endDate = endDate,
            description = description
        )
    }

    /**
     * Обновляет прогресс цели.
     */
    fun updateGoalProgress(
        goal: ReadingGoal,
        books: List<Book>,
        sessions: List<ReadingSession>
    ): ReadingGoal {
        val periodSessions = sessions.filter { it.date in goal.startDate..goal.endDate }
        val periodBooks = books.filter { it.addedDate in goal.startDate..goal.endDate }

        val progress = when (goal.type) {
            GoalType.BOOKS_PER_PERIOD -> periodBooks.count { it.isCompleted }
            GoalType.PAGES_PER_PERIOD -> periodSessions.sumOf { it.endPage - it.startPage + 1 }
            GoalType.HOURS_PER_PERIOD -> periodSessions.sumOf { it.duration } / 60
            GoalType.BOOKS_PER_MONTH -> {
                val monthsInPeriod = when (goal.period) {
                    Period.WEEK -> 0.25
                    Period.MONTH -> 1.0
                    Period.QUARTER -> 3.0
                    Period.YEAR -> 12.0
                }
                (periodBooks.count { it.isCompleted } / monthsInPeriod).toInt()
            }
            GoalType.STREAK_DAYS -> calculateReadingStreak(periodSessions)
        }

        val isCompleted = progress >= goal.targetValue

        return goal.copy(
            currentProgress = progress,
            isCompleted = isCompleted
        )
    }

    /**
     * Генерирует рекомендации по чтению.
     */
    fun generateReadingRecommendations(
        stats: ReadingStats,
        currentBooks: List<Book>,
        goals: List<ReadingGoal>
    ): List<String> {
        val recommendations = mutableListOf<String>()

        // Рекомендации по скорости чтения
        when {
            stats.readingSpeed < 20 -> recommendations.add("Попробуйте улучшить скорость чтения - читайте регулярно")
            stats.readingSpeed > 80 -> recommendations.add("Отличная скорость чтения! Не торопитесь с пониманием текста")
        }

        // Рекомендации по разнообразию
        if (stats.favoriteGenre != null && currentBooks.count { it.genre == stats.favoriteGenre } > currentBooks.size * 0.7) {
            recommendations.add("Попробуйте читать книги разных жанров для разнообразия")
        }

        // Рекомендации по целям
        val activeGoals = goals.filter { !it.isCompleted && LocalDate.now() <= it.endDate }
        if (activeGoals.isEmpty()) {
            recommendations.add("Установите цель чтения для мотивации")
        } else {
            val onTrackGoals = activeGoals.count { it.currentProgress >= it.targetValue * 0.8 }
            if (onTrackGoals < activeGoals.size) {
                recommendations.add("Некоторые цели чтения требуют больше внимания")
            }
        }

        // Рекомендации по привычке
        when {
            stats.readingStreak == 0 -> recommendations.add("Начните ежедневную привычку чтения")
            stats.readingStreak < 7 -> recommendations.add("Поддерживайте стрик чтения - читайте каждый день")
            stats.readingStreak >= 30 -> recommendations.add("Отличный стрик! Продолжайте в том же духе")
        }

        // Рекомендации по нагрузке
        val averageDailyPages = if (stats.booksThisMonth > 0) stats.pagesThisMonth / stats.booksThisMonth else 0
        when {
            averageDailyPages < 10 -> recommendations.add("Попробуйте читать больше страниц ежедневно")
            averageDailyPages > 100 -> recommendations.add("Не перегружайте себя - чередуйте чтение с отдыхом")
        }

        return recommendations
    }

    /**
     * Находит книгу для чтения на основе предпочтений.
     */
    fun suggestBook(
        availableBooks: List<Book>,
        userPreferences: ReadingPreferences,
        excludeCompleted: Boolean = true
    ): Book? {
        val candidates = availableBooks.filter { book ->
            (!excludeCompleted || !book.isCompleted) &&
            (userPreferences.preferredGenres.isEmpty() || userPreferences.preferredGenres.contains(book.genre)) &&
            (userPreferences.preferredLanguages.isEmpty() || userPreferences.preferredLanguages.contains(book.language)) &&
            book.difficulty.ordinal <= userPreferences.maxDifficulty.ordinal
        }

        if (candidates.isEmpty()) return null

        // Сортировка по релевантности
        return candidates.sortedByDescending { book ->
            var score = 0.0

            // Бонус за предпочтительные жанры
            if (userPreferences.preferredGenres.contains(book.genre)) score += 2.0

            // Бонус за предпочтительные языки
            if (userPreferences.preferredLanguages.contains(book.language)) score += 1.0

            // Штраф за высокую сложность
            score -= (book.difficulty.ordinal - userPreferences.maxDifficulty.ordinal) * 0.5

            // Бонус за книги с установленной датой завершения
            if (book.targetCompletionDate != null) score += 0.5

            score
        }.firstOrNull()
    }

    data class ReadingPreferences(
        val preferredGenres: List<String> = emptyList(),
        val preferredLanguages: List<Language> = emptyList(),
        val maxDifficulty: Difficulty = Difficulty.HARD,
        val preferredPageCount: IntRange = 100..500
    )

    /**
     * Создаёт отчёт о чтении за период.
     */
    fun generateReadingReport(
        books: List<Book>,
        sessions: List<ReadingSession>,
        goals: List<ReadingGoal>,
        period: ClosedRange<LocalDate> = LocalDate.now().minusDays(30)..LocalDate.now()
    ): ReadingReport {
        val periodSessions = sessions.filter { it.date in period }
        val periodBooks = books.filter { it.addedDate in period }
        val periodGoals = goals.filter { it.startDate in period || it.endDate in period }

        val stats = calculateReadingStats(books, sessions)
        val recommendations = generateReadingRecommendations(stats, books, goals)

        val completedGoals = periodGoals.count { it.isCompleted }
        val totalGoals = periodGoals.size

        val topGenres = books.groupingBy { it.genre }.eachCount()
            .entries.sortedByDescending { it.value }
            .take(3)
            .map { it.key to it.value }

        val readingPace = if (periodSessions.isNotEmpty()) {
            val days = java.time.temporal.ChronoUnit.DAYS.between(period.start, period.endInclusive) + 1
            periodSessions.size.toDouble() / days
        } else 0.0

        return ReadingReport(
            period = period,
            booksRead = periodBooks.count { it.isCompleted },
            pagesRead = periodSessions.sumOf { it.endPage - it.startPage + 1 },
            readingTime = periodSessions.sumOf { it.duration },
            goalsAchieved = completedGoals,
            totalGoals = totalGoals,
            topGenres = topGenres,
            readingPace = readingPace,
            averageRating = periodBooks.mapNotNull { it.rating }.average().takeIf { it.isFinite() },
            recommendations = recommendations,
            stats = stats
        )
    }

    data class ReadingReport(
        val period: ClosedRange<LocalDate>,
        val booksRead: Int,
        val pagesRead: Int,
        val readingTime: Int,
        val goalsAchieved: Int,
        val totalGoals: Int,
        val topGenres: List<Pair<String?, Int>>,
        val readingPace: Double, // сессий в день
        val averageRating: Double?,
        val recommendations: List<String>,
        val stats: ReadingStats
    )

    /**
     * Создаёт коллекцию книг из списка.
     */
    fun createBookCollection(
        bookData: List<Map<String, Any>>,
        defaultLanguage: Language = Language.RUSSIAN
    ): List<Book> {
        return bookData.map { data ->
            Book(
                id = data["id"] as? String ?: generateBookId(),
                title = data["title"] as? String ?: "Неизвестная книга",
                author = data["author"] as? String,
                genre = data["genre"] as? String,
                pageCount = (data["pageCount"] as? Number)?.toInt(),
                language = data["language"] as? Language ?: defaultLanguage,
                difficulty = data["difficulty"] as? Difficulty ?: Difficulty.MEDIUM,
                tags = (data["tags"] as? List<String>) ?: emptyList()
            )
        }
    }

    /**
     * Экспортирует данные о чтении.
     */
    fun exportReadingData(
        books: List<Book>,
        sessions: List<ReadingSession>,
        goals: List<ReadingGoal>
    ): String {
        return buildString {
            appendLine("Данные чтения - Экспорт от ${LocalDateTime.now()}")
            appendLine()

            appendLine("КНИГИ (${books.size}):")
            books.forEach { book ->
                appendLine("- ${book.title} (${book.author ?: "Автор неизвестен"})")
                appendLine("  Статус: ${if (book.isCompleted) "Прочитана" else "Читается"}")
                appendLine("  Страниц: ${book.pageCount ?: "Не указано"}")
                appendLine("  Рейтинг: ${book.rating ?: "Не оценена"}")
                if (book.review != null) appendLine("  Отзыв: ${book.review}")
                appendLine()
            }

            appendLine("СЕССИИ ЧТЕНИЯ (${sessions.size}):")
            sessions.sortedByDescending { it.date }.forEach { session ->
                appendLine("- ${session.date}: стр. ${session.startPage}-${session.endPage} (${session.duration} мин)")
                if (session.notes != null) appendLine("  Заметки: ${session.notes}")
            }
            appendLine()

            appendLine("ЦЕЛИ ЧТЕНИЯ (${goals.size}):")
            goals.forEach { goal ->
                val progress = "${goal.currentProgress}/${goal.targetValue}"
                val status = if (goal.isCompleted) "Достигнута" else "В процессе"
                appendLine("- ${goal.description ?: "Цель"}: $progress ($status)")
            }
        }
    }

    /**
     * Генерирует уникальный ID книги.
     */
    private fun generateBookId(): String {
        return "book_${System.currentTimeMillis()}_${(0..9999).random()}"
    }

    /**
     * Генерирует уникальный ID сессии.
     */
    private fun generateSessionId(): String {
        return "reading_${System.currentTimeMillis()}_${(0..9999).random()}"
    }

    /**
     * Генерирует уникальный ID цели.
     */
    private fun generateGoalId(): String {
        return "goal_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
}


