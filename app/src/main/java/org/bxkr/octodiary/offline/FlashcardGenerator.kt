package org.bxkr.octodiary.offline

/**
 * Генератор флеш-карт для повторения учебного материала.
 * Создаёт интерактивные карточки для эффективного запоминания.
 */
object FlashcardGenerator {

    data class Flashcard(
        val id: String,
        val front: String,
        val back: String,
        val subject: String,
        val topic: String? = null,
        val difficulty: Difficulty = Difficulty.MEDIUM,
        val cardType: CardType = CardType.BASIC,
        val tags: List<String> = emptyList(),
        val hints: List<String> = emptyList(),
        val examples: List<String> = emptyList(),
        val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
        var lastReviewed: java.time.LocalDateTime? = null,
        var reviewCount: Int = 0,
        var easeFactor: Double = 2.5, // коэффициент легкости (Supermemo)
        var interval: Int = 1 // интервал повторения в днях
    )

    enum class Difficulty {
        EASY, MEDIUM, HARD, EXPERT
    }

    enum class CardType {
        BASIC,         // Основная: вопрос -> ответ
        CLOZE,         // Cloze: текст с пропусками
        IMAGE,         // С картинкой
        MULTIPLE_CHOICE, // С вариантами ответа
        REVERSE        // Обратная карточка
    }

    data class StudySession(
        val id: String,
        val cards: List<Flashcard>,
        val sessionType: SessionType,
        val startTime: java.time.LocalDateTime = java.time.LocalDateTime.now(),
        var endTime: java.time.LocalDateTime? = null,
        val stats: StudyStats = StudyStats()
    )

    enum class SessionType {
        NEW_CARDS,     // Только новые карточки
        REVIEW,        // Повторение
        MIXED,         // Смешанная сессия
        DIFFICULT      // Сложные карточки
    }

    data class StudyStats(
        val cardsStudied: Int = 0,
        val correctAnswers: Int = 0,
        val incorrectAnswers: Int = 0,
        val timeSpent: Int = 0, // в секундах
        val averageResponseTime: Double = 0.0
    )

    data class Deck(
        val id: String,
        val name: String,
        val description: String? = null,
        val subject: String,
        val cards: List<Flashcard>,
        val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
        val lastStudied: java.time.LocalDateTime? = null,
        val stats: DeckStats = DeckStats()
    )

    data class DeckStats(
        val totalCards: Int = 0,
        val newCards: Int = 0,
        val reviewCards: Int = 0,
        val matureCards: Int = 0, // карточки с интервалом >21 дня
        val averageEase: Double = 2.5,
        val studyStreak: Int = 0
    )

    /**
     * Создаёт базовую флеш-карту.
     */
    fun createBasicCard(
        front: String,
        back: String,
        subject: String,
        topic: String? = null,
        difficulty: Difficulty = Difficulty.MEDIUM,
        tags: List<String> = emptyList()
    ): Flashcard {
        return Flashcard(
            id = generateCardId(),
            front = front,
            back = back,
            subject = subject,
            topic = topic,
            difficulty = difficulty,
            tags = tags
        )
    }

    /**
     * Создаёт cloze-карту (с пропусками).
     */
    fun createClozeCard(
        fullText: String,
        clozeText: String,
        subject: String,
        topic: String? = null,
        hints: List<String> = emptyList()
    ): Flashcard {
        // Cloze формат: текст с [...] для пропусков
        val front = fullText.replace(clozeText, "[...]")
        val back = fullText

        return Flashcard(
            id = generateCardId(),
            front = front,
            back = back,
            subject = subject,
            topic = topic,
            cardType = CardType.CLOZE,
            hints = hints
        )
    }

    /**
     * Создаёт карту с вариантами ответа.
     */
    fun createMultipleChoiceCard(
        question: String,
        correctAnswer: String,
        wrongAnswers: List<String>,
        subject: String,
        topic: String? = null
    ): Flashcard {
        val allOptions = (wrongAnswers + correctAnswer).shuffled()
        val optionsText = allOptions.mapIndexed { index, option ->
            "${'A' + index}) $option"
        }.joinToString("\n")

        val front = "$question\n\n$optionsText"
        val back = "Правильный ответ: ${allOptions.indexOf(correctAnswer) + 'A'.toInt()}"

        return Flashcard(
            id = generateCardId(),
            front = front,
            back = back,
            subject = subject,
            topic = topic,
            cardType = CardType.MULTIPLE_CHOICE
        )
    }

    /**
     * Создаёт колоду карточек из текста.
     */
    fun createDeckFromText(
        text: String,
        subject: String,
        topic: String? = null,
        cardType: CardType = CardType.BASIC
    ): Deck {
        val cards = mutableListOf<Flashcard>()

        // Разбиваем текст на предложения или абзацы
        val sentences = text.split(Regex("[.!?]+\\s+")).filter { it.isNotBlank() }

        for ((index, sentence) in sentences.withIndex()) {
            if (sentence.length < 10) continue // Пропускаем слишком короткие предложения

            when (cardType) {
                CardType.BASIC -> {
                    // Создаём вопрос из ключевых слов
                    val words = sentence.split("\\s+".toRegex()).filter { it.length > 3 }
                    if (words.size >= 3) {
                        val keyWord = words[words.size / 2]
                        val front = "Что означает: \"$keyWord\"?"
                        val back = sentence
                        cards.add(createBasicCard(front, back, subject, topic))
                    }
                }
                CardType.CLOZE -> {
                    // Создаём cloze из длинных предложений
                    if (sentence.length > 50) {
                        val words = sentence.split("\\s+".toRegex())
                        if (words.size >= 5) {
                            val clozeStart = words.size / 3
                            val clozeEnd = clozeStart + 2
                            val clozeWords = words.subList(clozeStart, minOf(clozeEnd, words.size)).joinToString(" ")
                            cards.add(createClozeCard(sentence, clozeWords, subject, topic))
                        }
                    }
                }
                else -> {
                    // Для других типов используем базовый
                    cards.add(createBasicCard("Определение", sentence, subject, topic))
                }
            }

            if (cards.size >= 20) break // Ограничиваем количество карточек
        }

        return createDeck("Колода из текста", subject, cards, "Сгенерировано из текста")
    }

    /**
     * Создаёт колоду карточек.
     */
    fun createDeck(
        name: String,
        subject: String,
        cards: List<Flashcard>,
        description: String? = null
    ): Deck {
        val stats = calculateDeckStats(cards)
        return Deck(
            id = generateDeckId(),
            name = name,
            description = description,
            subject = subject,
            cards = cards,
            stats = stats
        )
    }

    /**
     * Вычисляет статистику колоды.
     */
    private fun calculateDeckStats(cards: List<Flashcard>): DeckStats {
        val totalCards = cards.size
        val newCards = cards.count { it.reviewCount == 0 }
        val reviewCards = cards.count { shouldReview(it) }
        val matureCards = cards.count { it.interval > 21 }
        val averageEase = cards.map { it.easeFactor }.average()

        return DeckStats(
            totalCards = totalCards,
            newCards = newCards,
            reviewCards = reviewCards,
            matureCards = matureCards,
            averageEase = averageEase
        )
    }

    /**
     * Проверяет, нужно ли повторять карточку.
     */
    private fun shouldReview(card: Flashcard): Boolean {
        if (card.lastReviewed == null) return true

        val daysSinceReview = java.time.Duration.between(card.lastReviewed, java.time.LocalDateTime.now()).toDays()
        return daysSinceReview >= card.interval
    }

    /**
     * Создаёт сессию изучения.
     */
    fun createStudySession(
        deck: Deck,
        sessionType: SessionType,
        maxCards: Int = 20
    ): StudySession {
        val cardsToStudy = when (sessionType) {
            SessionType.NEW_CARDS -> deck.cards.filter { it.reviewCount == 0 }
            SessionType.REVIEW -> deck.cards.filter { shouldReview(it) }
            SessionType.DIFFICULT -> deck.cards.sortedBy { it.easeFactor }.take(maxCards)
            SessionType.MIXED -> {
                val newCards = deck.cards.filter { it.reviewCount == 0 }.take(maxCards / 2)
                val reviewCards = deck.cards.filter { shouldReview(it) }.take(maxCards / 2)
                (newCards + reviewCards).shuffled()
            }
        }.take(maxCards)

        return StudySession(
            id = generateSessionId(),
            cards = cardsToStudy,
            sessionType = sessionType
        )
    }

    /**
     * Обрабатывает ответ пользователя (алгоритм Supermemo 2).
     */
    fun processAnswer(card: Flashcard, quality: Int): Flashcard {
        // quality: 0-5 (0=полностью забыл, 5=легко вспомнил)
        require(quality in 0..5) { "Quality must be between 0 and 5" }

        val newCard = card.copy(
            lastReviewed = java.time.LocalDateTime.now(),
            reviewCount = card.reviewCount + 1
        )

        if (quality < 3) {
            // Ответ неправильный - сбрасываем интервал
            return newCard.copy(
                interval = 1,
                easeFactor = maxOf(1.3, card.easeFactor - 0.2)
            )
        } else {
            // Ответ правильный - увеличиваем интервал
            val newEaseFactor = maxOf(1.3, card.easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)))
            val newInterval = when (card.interval) {
                1 -> 6
                else -> (card.interval * newEaseFactor).toInt()
            }

            return newCard.copy(
                easeFactor = newEaseFactor,
                interval = newInterval
            )
        }
    }

    /**
     * Получает карточки для повторения сегодня.
     */
    fun getCardsForReview(cards: List<Flashcard>): List<Flashcard> {
        val today = java.time.LocalDate.now()
        return cards.filter { card ->
            if (card.lastReviewed == null) return@filter true

            val daysSinceReview = java.time.Duration.between(card.lastReviewed, java.time.LocalDateTime.now()).toDays()
            daysSinceReview >= card.interval
        }
    }

    /**
     * Анализирует прогресс изучения.
     */
    fun analyzeProgress(sessions: List<StudySession>): ProgressAnalysis {
        val totalSessions = sessions.size
        val totalCardsStudied = sessions.sumOf { it.stats.cardsStudied }
        val totalCorrect = sessions.sumOf { it.stats.correctAnswers }
        val totalTime = sessions.sumOf { it.stats.timeSpent }

        val accuracy = if (totalCardsStudied > 0) totalCorrect.toDouble() / totalCardsStudied else 0.0
        val averageTimePerCard = if (totalCardsStudied > 0) totalTime.toDouble() / totalCardsStudied else 0.0

        // Анализ тренда точности
        val recentSessions = sessions.takeLast(10)
        val recentAccuracy = recentSessions.map { session ->
            if (session.stats.cardsStudied > 0) {
                session.stats.correctAnswers.toDouble() / session.stats.cardsStudied
            } else 0.0
        }.average()

        val accuracyTrend = when {
            recentAccuracy > accuracy + 0.05 -> Trend.IMPROVING
            recentAccuracy < accuracy - 0.05 -> Trend.DECLINING
            else -> Trend.STABLE
        }

        val studyStreaks = calculateStudyStreaks(sessions)
        val mostDifficultCards = findMostDifficultCards(sessions)

        return ProgressAnalysis(
            totalSessions = totalSessions,
            totalCardsStudied = totalCardsStudied,
            overallAccuracy = accuracy,
            averageTimePerCard = averageTimePerCard,
            accuracyTrend = accuracyTrend,
            studyStreaks = studyStreaks,
            mostDifficultCards = mostDifficultCards
        )
    }

    data class ProgressAnalysis(
        val totalSessions: Int,
        val totalCardsStudied: Int,
        val overallAccuracy: Double,
        val averageTimePerCard: Double,
        val accuracyTrend: Trend,
        val studyStreaks: StudyStreaks,
        val mostDifficultCards: List<CardDifficulty>
    )

    enum class Trend {
        IMPROVING, STABLE, DECLINING
    }

    data class StudyStreaks(
        val currentStreak: Int,
        val longestStreak: Int,
        val totalStudyDays: Int
    )

    data class CardDifficulty(
        val cardId: String,
        val front: String,
        val incorrectAttempts: Int,
        val totalAttempts: Int,
        val difficultyRate: Double
    )

    /**
     * Вычисляет стримы изучения.
     */
    private fun calculateStudyStreaks(sessions: List<StudySession>): StudyStreaks {
        if (sessions.isEmpty()) return StudyStreaks(0, 0, 0)

        val studyDays = sessions.map { it.startTime.toLocalDate() }.distinct().sorted()
        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 0

        for (i in studyDays.indices) {
            if (i == 0 || studyDays[i].minusDays(1) == studyDays[i - 1]) {
                tempStreak++
                longestStreak = maxOf(longestStreak, tempStreak)
            } else {
                tempStreak = 1
            }
        }

        // Текущий стрик
        val today = java.time.LocalDate.now()
        currentStreak = if (studyDays.last() == today || studyDays.last() == today.minusDays(1)) {
            tempStreak
        } else {
            0
        }

        return StudyStreaks(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalStudyDays = studyDays.size
        )
    }

    /**
     * Находит наиболее сложные карточки.
     */
    private fun findMostDifficultCards(sessions: List<StudySession>): List<CardDifficulty> {
        val cardStats = mutableMapOf<String, MutableList<Boolean>>() // cardId -> list of correct/incorrect

        for (session in sessions) {
            // Упрощённая логика - в реальном приложении нужно сохранять результаты ответов
        }

        return cardStats.map { (cardId, results) ->
            val incorrectAttempts = results.count { !it }
            val totalAttempts = results.size
            val difficultyRate = if (totalAttempts > 0) incorrectAttempts.toDouble() / totalAttempts else 0.0

            CardDifficulty(
                cardId = cardId,
                front = "Карточка $cardId", // В реальном приложении нужно получить из БД
                incorrectAttempts = incorrectAttempts,
                totalAttempts = totalAttempts,
                difficultyRate = difficultyRate
            )
        }.sortedByDescending { it.difficultyRate }.take(10)
    }

    /**
     * Импортирует карточки из CSV.
     */
    fun importFromCSV(csvData: String, subject: String): List<Flashcard> {
        return csvData.lines()
            .drop(1) // Заголовок
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split(";").map { it.trim() }
                if (parts.size >= 2) {
                    createBasicCard(
                        front = parts[0],
                        back = parts[1],
                        subject = subject,
                        topic = if (parts.size > 2) parts[2] else null,
                        tags = if (parts.size > 3) parts[3].split(",").map { it.trim() } else emptyList()
                    )
                } else null
            }
    }

    /**
     * Экспортирует карточки в CSV.
     */
    fun exportToCSV(cards: List<Flashcard>): String {
        val header = "Лицевая сторона;Обратная сторона;Предмет;Тема;Сложность;Теги"
        val rows = cards.map { card ->
            "${card.front};${card.back};${card.subject};${card.topic ?: ""};${card.difficulty};${card.tags.joinToString(",")}"
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    /**
     * Генерирует уникальный ID карточки.
     */
    private fun generateCardId(): String {
        return "card_${System.currentTimeMillis()}_${(0..9999).random()}"
    }

    /**
     * Генерирует уникальный ID колоды.
     */
    private fun generateDeckId(): String {
        return "deck_${System.currentTimeMillis()}_${(0..9999).random()}"
    }

    /**
     * Генерирует уникальный ID сессии.
     */
    private fun generateSessionId(): String {
        return "session_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
}