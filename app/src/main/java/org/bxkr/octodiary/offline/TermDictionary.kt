package org.bxkr.octodiary.offline

/**
 * Локальный словарь терминов для хранения и поиска учебных терминов.
 * Поддерживает несколько языков и предметов.
 */
object TermDictionary {

    data class Term(
        val id: String,
        val term: String,
        val definition: String,
        val subject: String,
        val language: Language = Language.RUSSIAN,
        val difficulty: Difficulty = Difficulty.MEDIUM,
        val tags: List<String> = emptyList(),
        val examples: List<String> = emptyList(),
        val relatedTerms: List<String> = emptyList(),
        val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
        val lastReviewed: java.time.LocalDateTime? = null,
        val reviewCount: Int = 0
    )

    enum class Language(val displayName: String) {
        RUSSIAN("Русский"),
        ENGLISH("English"),
        GERMAN("Deutsch"),
        FRENCH("Français"),
        SPANISH("Español")
    }

    enum class Difficulty {
        EASY, MEDIUM, HARD, EXPERT
    }

    data class SearchResult(
        val term: Term,
        val relevance: Double,
        val matchedField: String
    )

    /**
     * Создаёт новый термин.
     */
    fun createTerm(
        term: String,
        definition: String,
        subject: String,
        language: Language = Language.RUSSIAN,
        difficulty: Difficulty = Difficulty.MEDIUM,
        tags: List<String> = emptyList(),
        examples: List<String> = emptyList(),
        relatedTerms: List<String> = emptyList()
    ): Term {
        val id = generateTermId()
        return Term(
            id = id,
            term = term,
            definition = definition,
            subject = subject,
            language = language,
            difficulty = difficulty,
            tags = tags,
            examples = examples,
            relatedTerms = relatedTerms
        )
    }

    /**
     * Ищет термины по запросу.
     */
    fun searchTerms(terms: List<Term>, query: String, language: Language? = null): List<SearchResult> {
        val results = mutableListOf<SearchResult>()

        terms.filter { language == null || it.language == language }.forEach { term ->
            // Поиск в термине
            calculateRelevance(term.term, query).takeIf { it > 0 }?.let {
                results.add(SearchResult(term, it, "term"))
            }

            // Поиск в определении
            calculateRelevance(term.definition, query).takeIf { it > 0 }?.let {
                results.add(SearchResult(term, it, "definition"))
            }

            // Поиск в примерах
            term.examples.forEach { example ->
                calculateRelevance(example, query).takeIf { it > 0 }?.let {
                    results.add(SearchResult(term, it, "example"))
                }
            }

            // Поиск в тегах
            term.tags.forEach { tag ->
                calculateRelevance(tag, query).takeIf { it > 0 }?.let {
                    results.add(SearchResult(term, it, "tag"))
                }
            }
        }

        // Убираем дубликаты терминов, оставляя наиболее релевантный результат
        return results
            .groupBy { it.term.id }
            .map { (_, groupResults) -> groupResults.maxBy { it.relevance } }
            .sortedByDescending { it.relevance }
    }

    /**
     * Вычисляет релевантность поиска.
     */
    private fun calculateRelevance(text: String, query: String): Double {
        val textLower = text.lowercase()
        val queryLower = query.lowercase()

        // Точное совпадение
        if (textLower == queryLower) return 1.0

        // Начинается с запроса
        if (textLower.startsWith(queryLower)) return 0.9

        // Содержит запрос как отдельное слово
        if (textLower.contains("\\b$queryLower\\b".toRegex())) return 0.8

        // Содержит запрос
        if (textLower.contains(queryLower)) return 0.6

        // Частичное совпадение по буквам
        val queryChars = queryLower.toSet()
        val textChars = textLower.toSet()
        val intersection = queryChars.intersect(textChars).size
        val union = queryChars.union(textChars).size

        return if (union > 0) intersection.toDouble() / union else 0.0
    }

    /**
     * Получает термины по предмету.
     */
    fun getTermsBySubject(terms: List<Term>, subject: String): List<Term> {
        return terms.filter { it.subject.equals(subject, ignoreCase = true) }
    }

    /**
     * Получает термины по языку.
     */
    fun getTermsByLanguage(terms: List<Term>, language: Language): List<Term> {
        return terms.filter { it.language == language }
    }

    /**
     * Получает термины по сложности.
     */
    fun getTermsByDifficulty(terms: List<Term>, difficulty: Difficulty): List<Term> {
        return terms.filter { it.difficulty == difficulty }
    }

    /**
     * Получает связанные термины.
     */
    fun getRelatedTerms(terms: List<Term>, termId: String): List<Term> {
        val term = terms.find { it.id == termId } ?: return emptyList()
        return terms.filter { relatedTerm ->
            relatedTerm.id != termId &&
            (term.relatedTerms.contains(relatedTerm.term) ||
             relatedTerm.relatedTerms.contains(term.term) ||
             term.tags.any { tag -> relatedTerm.tags.contains(tag) })
        }
    }

    /**
     * Обновляет статистику повторения термина.
     */
    fun updateReviewStats(term: Term): Term {
        return term.copy(
            lastReviewed = java.time.LocalDateTime.now(),
            reviewCount = term.reviewCount + 1
        )
    }

    /**
     * Получает термины для повторения (алгоритм spaced repetition).
     */
    fun getTermsForReview(terms: List<Term>, limit: Int = 10): List<Term> {
        val now = java.time.LocalDateTime.now()

        return terms
            .filter { it.lastReviewed != null }
            .map { term ->
                val daysSinceReview = java.time.Duration.between(term.lastReviewed, now).toDays()
                val reviewInterval = calculateReviewInterval(term.reviewCount, term.difficulty)
                val urgency = daysSinceReview.toDouble() / reviewInterval
                term to urgency
            }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }

    /**
     * Вычисляет интервал повторения на основе количества повторений и сложности.
     */
    private fun calculateReviewInterval(reviewCount: Int, difficulty: Difficulty): Double {
        val baseInterval = when (difficulty) {
            Difficulty.EASY -> 1.0
            Difficulty.MEDIUM -> 1.5
            Difficulty.HARD -> 2.0
            Difficulty.EXPERT -> 3.0
        }

        return baseInterval * (1.3.pow(reviewCount.coerceAtMost(10)))
    }

    /**
     * Создаёт набор терминов для изучения.
     */
    fun createStudySet(terms: List<Term>, name: String, subject: String, difficulty: Difficulty? = null): StudySet {
        val filteredTerms = if (difficulty != null) {
            terms.filter { it.difficulty == difficulty && it.subject == subject }
        } else {
            terms.filter { it.subject == subject }
        }

        return StudySet(
            id = generateSetId(),
            name = name,
            subject = subject,
            terms = filteredTerms,
            createdAt = java.time.LocalDateTime.now(),
            difficulty = difficulty
        )
    }

    data class StudySet(
        val id: String,
        val name: String,
        val subject: String,
        val terms: List<Term>,
        val createdAt: java.time.LocalDateTime,
        val difficulty: Difficulty? = null
    )

    /**
     * Импортирует термины из CSV-подобного формата.
     */
    fun importTerms(csvData: String, subject: String, language: Language = Language.RUSSIAN): List<Term> {
        return csvData.lines()
            .drop(1) // Пропускаем заголовок
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split(";").map { it.trim() }
                if (parts.size >= 2) {
                    createTerm(
                        term = parts[0],
                        definition = parts[1],
                        subject = subject,
                        language = language,
                        tags = if (parts.size > 2) parts[2].split(",").map { it.trim() } else emptyList()
                    )
                } else null
            }
    }

    /**
     * Экспортирует термины в CSV формат.
     */
    fun exportTerms(terms: List<Term>): String {
        val header = "Термин;Определение;Предмет;Язык;Сложность;Теги;Примеры"
        val rows = terms.map { term ->
            "${term.term};${term.definition};${term.subject};${term.language.displayName};${term.difficulty};${term.tags.joinToString(",")};${term.examples.joinToString("|")}"
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    /**
     * Получает статистику словаря.
     */
    fun getDictionaryStats(terms: List<Term>): DictionaryStats {
        val totalTerms = terms.size
        val subjects = terms.groupBy { it.subject }.size
        val languages = terms.groupBy { it.language }.size

        val difficultyStats = Difficulty.values().associateWith { difficulty ->
            terms.count { it.difficulty == difficulty }
        }

        val languageStats = Language.values().associateWith { language ->
            terms.count { it.language == language }
        }

        val mostReviewedTerm = terms.maxByOrNull { it.reviewCount }

        return DictionaryStats(
            totalTerms = totalTerms,
            totalSubjects = subjects,
            totalLanguages = languages,
            difficultyStats = difficultyStats,
            languageStats = languageStats,
            mostReviewedTerm = mostReviewedTerm?.term
        )
    }

    data class DictionaryStats(
        val totalTerms: Int,
        val totalSubjects: Int,
        val totalLanguages: Int,
        val difficultyStats: Map<Difficulty, Int>,
        val languageStats: Map<Language, Int>,
        val mostReviewedTerm: String?
    )

    /**
     * Генерирует уникальный ID для термина.
     */
    private fun generateTermId(): String {
        return "term_${System.currentTimeMillis()}_${(0..9999).random()}"
    }

    /**
     * Генерирует уникальный ID для набора терминов.
     */
    private fun generateSetId(): String {
        return "set_${System.currentTimeMillis()}_${(0..9999).random()}"
    }

    private fun Double.pow(exponent: Int): Double = Math.pow(this, exponent.toDouble())
}