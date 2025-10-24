package org.bxkr.octodiary.offline

/**
 * Генератор тестов для самопроверки знаний.
 * Создаёт различные типы тестов на основе учебного материала.
 */
object TestGenerator {

    data class Question(
        val id: String,
        val type: QuestionType,
        val question: String,
        val subject: String,
        val difficulty: Difficulty = Difficulty.MEDIUM,
        val topic: String? = null,
        val options: List<String>? = null, // для multiple choice
        val correctAnswer: String,
        val explanation: String? = null,
        val tags: List<String> = emptyList()
    )

    enum class QuestionType {
        MULTIPLE_CHOICE, // Выбор одного ответа
        MULTIPLE_SELECT, // Выбор нескольких ответов
        TRUE_FALSE,      // Верно/неверно
        SHORT_ANSWER,    // Короткий ответ
        FILL_BLANK,      // Заполнить пропуск
        MATCHING,        // Сопоставление
        ORDERING         // Упорядочение
    }

    enum class Difficulty {
        EASY, MEDIUM, HARD, EXPERT
    }

    data class Test(
        val id: String,
        val title: String,
        val description: String? = null,
        val subject: String,
        val questions: List<Question>,
        val timeLimit: Int? = null, // в минутах
        val passingScore: Int = 60, // процент для сдачи
        val difficulty: Difficulty = Difficulty.MEDIUM,
        val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
    )

    data class TestResult(
        val testId: String,
        val answers: Map<String, String>, // questionId -> answer
        val score: Int, // процент правильных ответов
        val timeSpent: Int? = null, // в минутах
        val completedAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
    )

    data class TestStats(
        val testId: String,
        val averageScore: Double,
        val completionRate: Double,
        val difficultyRating: Double,
        val questionStats: Map<String, QuestionStat>
    )

    data class QuestionStat(
        val questionId: String,
        val correctAnswers: Int,
        val totalAttempts: Int,
        val successRate: Double,
        val averageTime: Double? // среднее время на ответ
    )

    /**
     * Создаёт тест из набора вопросов.
     */
    fun createTest(
        title: String,
        subject: String,
        questions: List<Question>,
        timeLimit: Int? = null,
        passingScore: Int = 60
    ): Test {
        val difficulty = calculateTestDifficulty(questions)

        return Test(
            id = generateTestId(),
            title = title,
            subject = subject,
            questions = questions,
            timeLimit = timeLimit,
            passingScore = passingScore,
            difficulty = difficulty
        )
    }

    /**
     * Генерирует тест по предмету и темам.
     */
    fun generateTest(
        subject: String,
        topics: List<String>,
        questionCount: Int = 10,
        difficulty: Difficulty = Difficulty.MEDIUM
    ): Test {
        val questions = generateQuestions(subject, topics, questionCount, difficulty)
        val title = "Тест по ${subject}: ${topics.joinToString(", ")}"

        return createTest(title, subject, questions)
    }

    /**
     * Генерирует вопросы (упрощённая версия без AI).
     */
    private fun generateQuestions(
        subject: String,
        topics: List<String>,
        count: Int,
        difficulty: Difficulty
    ): List<Question> {
        // Это упрощённая версия. В реальном приложении можно использовать шаблоны или базу данных
        val questions = mutableListOf<Question>()

        val templates = getQuestionTemplates(subject)

        for (i in 0 until count) {
            val template = templates.random()
            val topic = topics.random()

            val question = when (template.type) {
                QuestionType.MULTIPLE_CHOICE -> createMultipleChoiceQuestion(template, subject, topic, difficulty)
                QuestionType.TRUE_FALSE -> createTrueFalseQuestion(template, subject, topic, difficulty)
                QuestionType.SHORT_ANSWER -> createShortAnswerQuestion(template, subject, topic, difficulty)
                else -> createMultipleChoiceQuestion(template, subject, topic, difficulty) // fallback
            }

            questions.add(question)
        }

        return questions
    }

    /**
     * Создаёт вопрос с множественным выбором.
     */
    private fun createMultipleChoiceQuestion(
        template: QuestionTemplate,
        subject: String,
        topic: String,
        difficulty: Difficulty
    ): Question {
        val question = template.questionTemplate
            .replace("{subject}", subject)
            .replace("{topic}", topic)

        val options = when (difficulty) {
            Difficulty.EASY -> listOf("Вариант A", "Вариант B", "Вариант C", "Вариант D")
            Difficulty.MEDIUM -> listOf("Первый ответ", "Второй ответ", "Третий ответ", "Четвертый ответ")
            Difficulty.HARD -> listOf("Комплексный ответ 1", "Комплексный ответ 2", "Комплексный ответ 3", "Комплексный ответ 4")
            Difficulty.EXPERT -> listOf("Экспертный ответ A", "Экспертный ответ B", "Экспертный ответ C", "Экспертный ответ D")
        }

        return Question(
            id = generateQuestionId(),
            type = QuestionType.MULTIPLE_CHOICE,
            question = question,
            subject = subject,
            difficulty = difficulty,
            topic = topic,
            options = options,
            correctAnswer = options[0], // Первый вариант правильный
            explanation = "Это правильный ответ потому что...",
            tags = listOf(subject.lowercase(), topic.lowercase())
        )
    }

    /**
     * Создаёт вопрос верный/неверный.
     */
    private fun createTrueFalseQuestion(
        template: QuestionTemplate,
        subject: String,
        topic: String,
        difficulty: Difficulty
    ): Question {
        val question = "${template.questionTemplate.replace("{subject}", subject).replace("{topic}", topic)} (Верно/Неверно)"

        return Question(
            id = generateQuestionId(),
            type = QuestionType.TRUE_FALSE,
            question = question,
            subject = subject,
            difficulty = difficulty,
            topic = topic,
            options = listOf("Верно", "Неверно"),
            correctAnswer = if (kotlin.random.Random.nextBoolean()) "Верно" else "Неверно",
            tags = listOf(subject.lowercase(), topic.lowercase(), "true-false")
        )
    }

    /**
     * Создаёт вопрос с коротким ответом.
     */
    private fun createShortAnswerQuestion(
        template: QuestionTemplate,
        subject: String,
        topic: String,
        difficulty: Difficulty
    ): Question {
        val question = template.questionTemplate
            .replace("{subject}", subject)
            .replace("{topic}", topic)

        val correctAnswer = when (difficulty) {
            Difficulty.EASY -> "Простой ответ"
            Difficulty.MEDIUM -> "Средний ответ"
            Difficulty.HARD -> "Сложный ответ"
            Difficulty.EXPERT -> "Экспертный ответ"
        }

        return Question(
            id = generateQuestionId(),
            type = QuestionType.SHORT_ANSWER,
            question = question,
            subject = subject,
            difficulty = difficulty,
            topic = topic,
            correctAnswer = correctAnswer,
            tags = listOf(subject.lowercase(), topic.lowercase(), "short-answer")
        )
    }

    /**
     * Шаблоны вопросов для разных предметов.
     */
    data class QuestionTemplate(
        val type: QuestionType,
        val questionTemplate: String
    )

    private fun getQuestionTemplates(subject: String): List<QuestionTemplate> {
        val mathTemplates = listOf(
            QuestionTemplate(QuestionType.MULTIPLE_CHOICE, "Какое из следующих утверждений верно для {topic} в {subject}?"),
            QuestionTemplate(QuestionType.SHORT_ANSWER, "Решите уравнение: {topic}"),
            QuestionTemplate(QuestionType.TRUE_FALSE, "{topic} является фундаментальным понятием в {subject}")
        )

        val languageTemplates = listOf(
            QuestionTemplate(QuestionType.MULTIPLE_CHOICE, "Как правильно склонять слово '{topic}'?"),
            QuestionTemplate(QuestionType.SHORT_ANSWER, "Напишите синоним к слову '{topic}'"),
            QuestionTemplate(QuestionType.TRUE_FALSE, "'{topic}' является правильным написанием в {subject}")
        )

        val scienceTemplates = listOf(
            QuestionTemplate(QuestionType.MULTIPLE_CHOICE, "Какое свойство {topic} наиболее важно в {subject}?"),
            QuestionTemplate(QuestionType.SHORT_ANSWER, "Опишите процесс {topic}"),
            QuestionTemplate(QuestionType.TRUE_FALSE, "{topic} влияет на окружающую среду")
        )

        return when (subject.lowercase()) {
            "математика", "math" -> mathTemplates
            "русский язык", "литература", "language", "literature" -> languageTemplates
            "физика", "химия", "биология", "physics", "chemistry", "biology" -> scienceTemplates
            else -> mathTemplates // fallback
        }
    }

    /**
     * Проверяет тест и возвращает результат.
     */
    fun gradeTest(test: Test, answers: Map<String, String>): TestResult {
        var correctAnswers = 0

        for (question in test.questions) {
            val userAnswer = answers[question.id]
            if (userAnswer != null && isAnswerCorrect(question, userAnswer)) {
                correctAnswers++
            }
        }

        val score = if (test.questions.isNotEmpty()) {
            (correctAnswers.toDouble() / test.questions.size * 100).toInt()
        } else 0

        return TestResult(
            testId = test.id,
            answers = answers,
            score = score
        )
    }

    /**
     * Проверяет правильность ответа на вопрос.
     */
    private fun isAnswerCorrect(question: Question, userAnswer: String): Boolean {
        return when (question.type) {
            QuestionType.MULTIPLE_CHOICE, QuestionType.TRUE_FALSE -> {
                userAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
            }
            QuestionType.SHORT_ANSWER -> {
                // Упрощённая проверка - в реальном приложении нужна более сложная логика
                userAnswer.trim().lowercase() == question.correctAnswer.trim().lowercase()
            }
            QuestionType.MULTIPLE_SELECT -> {
                // Для множественного выбора - сравнение множеств
                val userAnswers = userAnswer.split(",").map { it.trim().lowercase() }
                val correctAnswers = question.correctAnswer.split(",").map { it.trim().lowercase() }
                userAnswers.toSet() == correctAnswers.toSet()
            }
            else -> false // Другие типы пока не поддерживаются
        }
    }

    /**
     * Вычисляет сложность теста на основе вопросов.
     */
    private fun calculateTestDifficulty(questions: List<Question>): Difficulty {
        if (questions.isEmpty()) return Difficulty.MEDIUM

        val averageDifficulty = questions.map {
            when (it.difficulty) {
                Difficulty.EASY -> 1
                Difficulty.MEDIUM -> 2
                Difficulty.HARD -> 3
                Difficulty.EXPERT -> 4
            }
        }.average()

        return when {
            averageDifficulty < 1.5 -> Difficulty.EASY
            averageDifficulty < 2.5 -> Difficulty.MEDIUM
            averageDifficulty < 3.5 -> Difficulty.HARD
            else -> Difficulty.EXPERT
        }
    }

    /**
     * Создаёт адаптивный тест на основе предыдущих результатов.
     */
    fun createAdaptiveTest(
        subject: String,
        userHistory: List<TestResult>,
        questionPool: List<Question>,
        targetQuestions: Int = 10
    ): Test {
        val weakTopics = identifyWeakTopics(userHistory, questionPool)

        val selectedQuestions = mutableListOf<Question>()
        val questionsByTopic = questionPool.groupBy { it.topic ?: "general" }

        // 70% вопросов из слабых тем, 30% из остальных
        val weakQuestions = weakTopics.flatMap { topic ->
            questionsByTopic[topic] ?: emptyList()
        }.shuffled().take((targetQuestions * 0.7).toInt())

        val otherQuestions = questionPool.filter { question ->
            !weakTopics.contains(question.topic ?: "general")
        }.shuffled().take(targetQuestions - weakQuestions.size)

        selectedQuestions.addAll(weakQuestions)
        selectedQuestions.addAll(otherQuestions)

        return createTest(
            title = "Адаптивный тест по $subject",
            subject = subject,
            questions = selectedQuestions
        )
    }

    /**
     * Определяет слабые темы на основе истории тестов.
     */
    private fun identifyWeakTopics(userHistory: List<TestResult>, questionPool: List<Question>): List<String> {
        val topicPerformance = mutableMapOf<String, MutableList<Double>>()

        // Группируем результаты по темам
        for (result in userHistory) {
            // Упрощённая логика - в реальном приложении нужно связать answers с questions
            // Здесь просто симулируем анализ
        }

        // Возвращаем темы с низкой успеваемостью (< 70%)
        return topicPerformance.entries
            .filter { it.value.average() < 70.0 }
            .map { it.key }
    }

    /**
     * Генерирует отчёт по статистике тестов.
     */
    fun generateTestReport(tests: List<Test>, results: List<TestResult>): TestStats {
        val testResults = results.filter { result -> tests.any { it.id == result.testId } }

        val averageScore = testResults.map { it.score }.average()
        val completionRate = if (tests.isNotEmpty()) testResults.size.toDouble() / tests.size else 0.0

        val questionStats = mutableMapOf<String, QuestionStat>()

        // Вычисляем статистику по вопросам (упрощённая версия)
        for (test in tests) {
            for (question in test.questions) {
                val questionResults = testResults.filter { it.answers.containsKey(question.id) }
                val correctCount = questionResults.count { isAnswerCorrect(question, it.answers[question.id] ?: "") }

                val existing = questionStats[question.id]
                if (existing != null) {
                    questionStats[question.id] = existing.copy(
                        correctAnswers = existing.correctAnswers + correctCount,
                        totalAttempts = existing.totalAttempts + questionResults.size
                    )
                } else {
                    questionStats[question.id] = QuestionStat(
                        questionId = question.id,
                        correctAnswers = correctCount,
                        totalAttempts = questionResults.size,
                        successRate = if (questionResults.isNotEmpty()) correctCount.toDouble() / questionResults.size else 0.0,
                        averageTime = null
                    )
                }
            }
        }

        // Обновляем success rate
        questionStats.values.forEach { stat ->
            stat.copy(successRate = if (stat.totalAttempts > 0) stat.correctAnswers.toDouble() / stat.totalAttempts else 0.0)
        }

        val difficultyRating = calculateOverallDifficulty(questionStats.values)

        return TestStats(
            testId = "overall", // Для общего отчёта
            averageScore = averageScore,
            completionRate = completionRate,
            difficultyRating = difficultyRating,
            questionStats = questionStats
        )
    }

    /**
     * Вычисляет общую сложность на основе статистики вопросов.
     */
    private fun calculateOverallDifficulty(questionStats: Collection<QuestionStat>): Double {
        if (questionStats.isEmpty()) return 2.0 // medium

        val averageSuccessRate = questionStats.map { it.successRate }.average()

        // Преобразуем success rate в рейтинг сложности (0-4)
        // Низкий success rate = высокая сложность
        return when {
            averageSuccessRate > 0.8 -> 1.0 // easy
            averageSuccessRate > 0.6 -> 2.0 // medium
            averageSuccessRate > 0.4 -> 3.0 // hard
            else -> 4.0 // expert
        }
    }

    /**
     * Генерирует уникальный ID теста.
     */
    private fun generateTestId(): String {
        return "test_${System.currentTimeMillis()}_${(0..9999).random()}"
    }

    /**
     * Генерирует уникальный ID вопроса.
     */
    private fun generateQuestionId(): String {
        return "question_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
}