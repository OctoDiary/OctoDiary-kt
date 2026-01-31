package org.bxkr.octodiary.offline


import androidx.compose.material.icons.Icons
/**
 * Генератор мотивационных цитат и сообщений.
 * Предоставляет вдохновляющие цитаты для разных ситуаций в учёбе.
 */
object MotivationGenerator {

    data class Quote(
        val id: String,
        val text: String,
        val author: String,
        val category: Category,
        val language: Language = Language.RUSSIAN,
        val tags: List<String> = emptyList()
    )

    enum class Category {
        STUDY_MOTIVATION,    // Мотивация к учёбе
        SUCCESS,            // Успех
        PERSEVERANCE,       // Настойчивость
        GROWTH,            // Рост и развитие
        FAILURE,           // Отношение к неудачам
        TIME_MANAGEMENT,   // Управление временем
        HEALTH,            // Здоровье и баланс
        GOALS,             // Цели
        CREATIVITY,        // Творчество
        TEAMWORK           // Командная работа
    }

    enum class Language {
        RUSSIAN, ENGLISH, GERMAN, FRENCH
    }

    data class DailyMotivation(
        val date: java.time.LocalDate,
        val quote: Quote,
        val personalizedMessage: String? = null,
        val context: MotivationContext? = null
    )

    enum class MotivationContext {
        MORNING_START,     // Утреннее начало дня
        STUDY_BREAK,       // Перерыв в учёбе
        DIFFICULT_TASK,    // Сложное задание
        EXAM_PREP,         // Подготовка к экзамену
        LOW_MOOD,          // Плохое настроение
        ACHIEVEMENT,       // После достижения
        WEEKEND_MOTIVATION // Мотивация на выходных
    }

    private val quotes = listOf(
        // Русские цитаты
        Quote("1", "Успех — это сумма небольших усилий, повторяющихся день за днём.", "Роберт Колльер", Category.SUCCESS, Language.RUSSIAN, listOf("успех", "усилия")),
        Quote("2", "Учёба — это не гонка, а путешествие. Наслаждайся каждым шагом.", "Автор неизвестен", Category.STUDY_MOTIVATION, Language.RUSSIAN, listOf("учёба", "путешествие")),
        Quote("3", "Великие дела начинаются с малого. Начни с одной страницы сегодня.", "Автор неизвестен", Category.GROWTH, Language.RUSSIAN, listOf("начало", "рост")),
        Quote("4", "Неудачи — это возможности начать снова, но уже умнее.", "Генри Форд", Category.FAILURE, Language.RUSSIAN, listOf("неудачи", "возможности")),
        Quote("5", "Дисциплина — мост между целями и достижениями.", "Джим Рон", Category.PERSEVERANCE, Language.RUSSIAN, listOf("дисциплина", "цели")),
        Quote("6", "Каждый эксперт когда-то был новичком. Продолжай учиться!", "Автор неизвестен", Category.GROWTH, Language.RUSSIAN, listOf("эксперт", "новичок")),
        Quote("7", "Лучшее время посадить дерево было 20 лет назад. Второе лучшее время — сегодня.", "Китайская пословица", Category.TIME_MANAGEMENT, Language.RUSSIAN, listOf("время", "действие")),
        Quote("8", "Твой мозг — как мышца. Чем больше тренируешь, тем сильнее становится.", "Автор неизвестен", Category.HEALTH, Language.RUSSIAN, listOf("мозг", "тренировка")),
        Quote("9", "Маленькие ежедневные улучшения приводят к потрясающим результатам.", "Автор неизвестен", Category.GROWTH, Language.RUSSIAN, listOf("улучшения", "результаты")),
        Quote("10", "Умение учиться — это умение успешно адаптироваться к изменениям.", "Автор неизвестен", Category.STUDY_MOTIVATION, Language.RUSSIAN, listOf("обучение", "адаптация")),

        // Английские цитаты
        Quote("11", "The only way to do great work is to love what you do.", "Steve Jobs", Category.STUDY_MOTIVATION, Language.ENGLISH, listOf("work", "love")),
        Quote("12", "Believe you can and you're halfway there.", "Theodore Roosevelt", Category.SUCCESS, Language.ENGLISH, listOf("belief", "success")),
        Quote("13", "The future belongs to those who believe in the beauty of their dreams.", "Eleanor Roosevelt", Category.GOALS, Language.ENGLISH, listOf("future", "dreams")),
        Quote("14", "Don't watch the clock; do what it does. Keep going.", "Sam Levenson", Category.PERSEVERANCE, Language.ENGLISH, listOf("time", "persistence")),
        Quote("15", "The secret of getting ahead is getting started.", "Mark Twain", Category.TIME_MANAGEMENT, Language.ENGLISH, listOf("action", "start")),
        Quote("16", "Your limitation—it's only your imagination.", "Unknown", Category.CREATIVITY, Language.ENGLISH, listOf("imagination", "limits")),
        Quote("17", "Push yourself, because no one else is going to do it for you.", "Unknown", Category.PERSEVERANCE, Language.ENGLISH, listOf("motivation", "self")),
        Quote("18", "Great things never come from comfort zones.", "Unknown", Category.GROWTH, Language.ENGLISH, listOf("comfort", "growth")),
        Quote("19", "Dream it. Wish it. Do it.", "Unknown", Category.GOALS, Language.ENGLISH, listOf("dreams", "action")),
        Quote("20", "Success doesn't just find you. You have to go out and get it.", "Unknown", Category.SUCCESS, Language.ENGLISH, listOf("success", "effort")),

        // Немецкие цитаты
        Quote("21", "Wer aufhört zu werben, hat verloren.", "Henry Ford (auf Deutsch)", Category.PERSEVERANCE, Language.GERMAN, listOf("werben", "verlieren")),
        Quote("22", "Die beste Zeit, einen Baum zu pflanzen, war vor 20 Jahren. Die zweitbeste Zeit ist heute.", "Chinesisches Sprichwort", Category.TIME_MANAGEMENT, Language.GERMAN, listOf("zeit", "baum")),

        // Французские цитаты
        Quote("23", "Le succès c'est d'aller d'échec en échec sans perdre son enthousiasme.", "Winston Churchill", Category.FAILURE, Language.FRENCH, listOf("succès", "échec")),
        Quote("24", "La motivation vient de la réalisation de soi.", "Aristote", Category.GROWTH, Language.FRENCH, listOf("motivation", "réalisation"))
    )

    /**
     * Получает случайную цитату.
     */
    fun getRandomQuote(category: Category? = null, language: Language = Language.RUSSIAN): Quote {
        val filteredQuotes = quotes.filter { quote ->
            (category == null || quote.category == category) &&
            quote.language == language
        }

        return if (filteredQuotes.isNotEmpty()) {
            filteredQuotes.random()
        } else {
            // Fallback to any language if no quotes in requested language
            quotes.filter { category == null || it.category == category }.random()
        }
    }

    /**
     * Получает цитату для конкретного контекста.
     */
    fun getQuoteForContext(context: MotivationContext, language: Language = Language.RUSSIAN): Quote {
        val suitableCategories = when (context) {
            MotivationContext.MORNING_START -> listOf(Category.STUDY_MOTIVATION, Category.GROWTH, Category.GOALS)
            MotivationContext.STUDY_BREAK -> listOf(Category.HEALTH, Category.PERSEVERANCE, Category.TIME_MANAGEMENT)
            MotivationContext.DIFFICULT_TASK -> listOf(Category.PERSEVERANCE, Category.SUCCESS, Category.GROWTH)
            MotivationContext.EXAM_PREP -> listOf(Category.STUDY_MOTIVATION, Category.PERSEVERANCE, Category.SUCCESS)
            MotivationContext.LOW_MOOD -> listOf(Category.SUCCESS, Category.PERSEVERANCE, Category.GROWTH)
            MotivationContext.ACHIEVEMENT -> listOf(Category.SUCCESS, Category.GROWTH, Category.GOALS)
            MotivationContext.WEEKEND_MOTIVATION -> listOf(Category.TIME_MANAGEMENT, Category.GROWTH, Category.GOALS)
        }

        return getRandomQuote(suitableCategories.random(), language)
    }

    /**
     * Генерирует ежедневную мотивацию.
     */
    fun generateDailyMotivation(date: java.time.LocalDate = java.time.LocalDate.now(), context: MotivationContext? = null): DailyMotivation {
        val quote = if (context != null) {
            getQuoteForContext(context)
        } else {
            getRandomQuote()
        }

        val personalizedMessage = generatePersonalizedMessage(quote, context)

        return DailyMotivation(
            date = date,
            quote = quote,
            personalizedMessage = personalizedMessage,
            context = context
        )
    }

    /**
     * Генерирует персонализированное сообщение.
     */
    private fun generatePersonalizedMessage(quote: Quote, context: MotivationContext?): String? {
        if (context == null) return null

        return when (context) {
            MotivationContext.MORNING_START -> "Доброе утро! Начни свой учебный день с правильного настроя."
            MotivationContext.STUDY_BREAK -> "Перерыв - время восстановить силы. Продолжай в том же духе!"
            MotivationContext.DIFFICULT_TASK -> "Это задание может быть сложным, но ты справишься!"
            MotivationContext.EXAM_PREP -> "Каждый день подготовки приближает тебя к успеху на экзамене."
            MotivationContext.LOW_MOOD -> "Не позволяй плохому настроению остановить тебя. Ты сильнее!"
            MotivationContext.ACHIEVEMENT -> "Поздравляем с достижением! Продолжай в том же духе."
            MotivationContext.WEEKEND_MOTIVATION -> "Выходные - время подготовиться к новой учебной неделе."
        }
    }

    /**
     * Получает цитаты по категории.
     */
    fun getQuotesByCategory(category: Category, language: Language = Language.RUSSIAN): List<Quote> {
        return quotes.filter { it.category == category && it.language == language }
    }

    /**
     * Ищет цитаты по ключевым словам.
     */
    fun searchQuotes(query: String, language: Language = Language.RUSSIAN): List<Quote> {
        val queryLower = query.lowercase()

        return quotes.filter { quote ->
            quote.language == language && (
                quote.text.lowercase().contains(queryLower) ||
                quote.author.lowercase().contains(queryLower) ||
                quote.tags.any { it.lowercase().contains(queryLower) }
            )
        }
    }

    /**
     * Получает мотивационные сообщения для конкретных ситуаций.
     */
    fun getMotivationalMessages(situation: StudySituation): List<String> {
        return when (situation) {
            StudySituation.BEFORE_STUDY -> listOf(
                "Соберись и настройся на продуктивную работу!",
                "Помни: каждое усилие приближает тебя к цели.",
                "Начни с малого, но будь настойчив.",
                "Ты способен на большее, чем думаешь!"
            )
            StudySituation.DURING_BREAK -> listOf(
                "Перерыв - время для отдыха и восстановления.",
                "Сделай глубокий вдох и продолжай.",
                "Ты на правильном пути!",
                "Каждый шаг важен."
            )
            StudySituation.AFTER_FAILURE -> listOf(
                "Неудачи - это уроки, а не конец пути.",
                "Попробуй ещё раз, теперь ты знаешь, что делать.",
                "Каждый великий человек когда-то терпел неудачи.",
                "Используй это как возможность стать сильнее."
            )
            StudySituation.BEFORE_EXAM -> listOf(
                "Ты готов! Верь в себя.",
                "Вся подготовка была не зря.",
                "Дыши глубоко и оставайся спокойным.",
                "Показывай всё, на что способен!"
            )
            StudySituation.AFTER_SUCCESS -> listOf(
                "Поздравляем! Ты заслужил этот успех.",
                "Продолжай в том же духе!",
                "Это только начало великих свершений.",
                "Твои усилия окупились."
            )
        }
    }

    enum class StudySituation {
        BEFORE_STUDY, DURING_BREAK, AFTER_FAILURE, BEFORE_EXAM, AFTER_SUCCESS
    }

    /**
     * Генерирует последовательность цитат на неделю.
     */
    fun generateWeeklyMotivationPlan(startDate: java.time.LocalDate = java.time.LocalDate.now()): List<DailyMotivation> {
        val weekDays = listOf(
            MotivationContext.MORNING_START,
            MotivationContext.MORNING_START,
            MotivationContext.STUDY_BREAK,
            MotivationContext.STUDY_BREAK,
            MotivationContext.DIFFICULT_TASK,
            MotivationContext.EXAM_PREP,
            MotivationContext.WEEKEND_MOTIVATION
        )

        return weekDays.mapIndexed { index, context ->
            generateDailyMotivation(startDate.plusDays(index.toLong()), context)
        }
    }

    /**
     * Получает статистику использования цитат.
     */
    fun getUsageStats(): Map<Category, Int> {
        // В реальном приложении здесь был бы анализ использования
        return Category.values().associateWith { kotlin.random.Random.nextInt(10, 50) }
    }

    /**
     * Создаёт кастомную цитату.
     */
    fun createCustomQuote(
        text: String,
        author: String,
        category: Category,
        language: Language = Language.RUSSIAN,
        tags: List<String> = emptyList()
    ): Quote {
        return Quote(
            id = generateQuoteId(),
            text = text,
            author = author,
            category = category,
            language = language,
            tags = tags
        )
    }

    /**
     * Получает цитаты для конкретного времени дня.
     */
    fun getTimeBasedMotivation(hour: Int): Quote {
        val context = when (hour) {
            in 6..9 -> MotivationContext.MORNING_START
            in 10..16 -> MotivationContext.STUDY_BREAK
            in 17..20 -> MotivationContext.EXAM_PREP
            else -> MotivationContext.LOW_MOOD
        }

        return getQuoteForContext(context)
    }

    /**
     * Генерирует уникальный ID цитаты.
     */
    private fun generateQuoteId(): String {
        return "quote_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
}


