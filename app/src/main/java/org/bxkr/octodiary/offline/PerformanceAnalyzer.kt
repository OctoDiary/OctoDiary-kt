package org.bxkr.octodiary.offline


import androidx.compose.material.icons.Icons
/**
 * Локальный анализатор успеваемости без использования API.
 * Анализирует оценки, паттерны обучения и предсказывает будущие результаты.
 */
object PerformanceAnalyzer {

    data class PerformanceMetrics(
        val overallGrade: Double,
        val gradeTrend: Trend,
        val consistencyScore: Double, // 0-1, где 1 - максимально консистентно
        val improvementRate: Double, // оценки в месяц
        val riskFactors: List<String>,
        val strengths: List<String>,
        val recommendations: List<String>
    )

    enum class Trend {
        IMPROVING, STABLE, DECLINING, VOLATILE
    }

    data class SubjectAnalysis(
        val subject: String,
        val metrics: PerformanceMetrics,
        val weeklyProgress: Map<java.time.LocalDate, Double>,
        val assessmentDistribution: Map<String, Int>, // тип оценки -> количество
        val timeSpent: Int? = null // минуты в неделю
    )

    data class StudyPattern(
        val peakStudyTime: java.time.LocalTime?,
        val preferredStudyDays: List<java.time.DayOfWeek>,
        val averageSessionLength: Int, // минуты
        val studyFrequency: Double, // сессий в неделю
        val consistencyPattern: ConsistencyPattern
    )

    enum class ConsistencyPattern {
        HIGHLY_CONSISTENT, MODERATELY_CONSISTENT, INCONSISTENT, ERRATIC
    }

    data class PerformancePrediction(
        val subject: String,
        val predictedGrade: Double,
        val confidence: Double, // 0-1
        val factors: Map<String, Double>, // фактор -> влияние
        val timeToTarget: Int? = null // недель до достижения цели
    )

    data class StudyRecommendation(
        val priority: Priority,
        val category: String,
        val action: String,
        val expectedImpact: Double, // ожидаемое улучшение оценки
        val timeRequired: Int // минуты в неделю
    )

    enum class Priority {
        HIGH, MEDIUM, LOW
    }

    /**
     * Анализирует общую успеваемость по всем предметам.
     */
    fun analyzeOverallPerformance(
        grades: Map<String, List<Int>>,
        dates: Map<String, List<java.time.LocalDate>> = emptyMap(),
        timeSpent: Map<String, Int> = emptyMap()
    ): PerformanceMetrics {
        if (grades.isEmpty()) {
            return PerformanceMetrics(
                overallGrade = 0.0,
                gradeTrend = Trend.STABLE,
                consistencyScore = 0.0,
                improvementRate = 0.0,
                riskFactors = listOf("Недостаточно данных для анализа"),
                strengths = emptyList(),
                recommendations = listOf("Добавьте оценки для анализа успеваемости")
            )
        }

        // Вычисляем средний балл по всем предметам
        val allGrades = grades.values.flatten()
        val overallGrade = allGrades.average()

        // Анализируем тренд
        val gradeTrend = analyzeGradeTrend(grades, dates)

        // Вычисляем консистентность
        val consistencyScore = calculateConsistencyScore(grades)

        // Вычисляем скорость улучшения
        val improvementRate = calculateImprovementRate(grades, dates)

        // Определяем факторы риска
        val riskFactors = identifyRiskFactors(grades, dates, timeSpent)

        // Определяем сильные стороны
        val strengths = identifyStrengths(grades, dates, timeSpent)

        // Генерируем рекомендации
        val recommendations = generateRecommendations(gradeTrend, consistencyScore, riskFactors)

        return PerformanceMetrics(
            overallGrade = overallGrade,
            gradeTrend = gradeTrend,
            consistencyScore = consistencyScore,
            improvementRate = improvementRate,
            riskFactors = riskFactors,
            strengths = strengths,
            recommendations = recommendations
        )
    }

    /**
     * Анализирует успеваемость по конкретному предмету.
     */
    fun analyzeSubjectPerformance(
        subject: String,
        grades: List<Int>,
        dates: List<java.time.LocalDate> = emptyList(),
        timeSpent: Int? = null
    ): SubjectAnalysis {
        val metrics = analyzeSubjectMetrics(subject, grades, dates, timeSpent)
        val weeklyProgress = calculateWeeklyProgress(grades, dates)

        // Анализируем распределение типов оценок (упрощённо)
        val assessmentDistribution = mapOf(
            "Тесты" to grades.count { it >= 4 },
            "Домашние задания" to grades.count { it in 3..4 },
            "Контрольные" to grades.count { it < 3 }
        ).filter { it.value > 0 }

        return SubjectAnalysis(
            subject = subject,
            metrics = metrics,
            weeklyProgress = weeklyProgress,
            assessmentDistribution = assessmentDistribution,
            timeSpent = timeSpent
        )
    }

    /**
     * Вычисляет метрики для предмета.
     */
    private fun analyzeSubjectMetrics(
        subject: String,
        grades: List<Int>,
        dates: List<java.time.LocalDate>,
        timeSpent: Int?
    ): PerformanceMetrics {
        if (grades.isEmpty()) return analyzeOverallPerformance(emptyMap())

        val overallGrade = grades.average()
        val gradeTrend = analyzeGradeTrend(mapOf(subject to grades), mapOf(subject to dates))
        val consistencyScore = calculateSubjectConsistency(grades)
        val improvementRate = calculateSubjectImprovement(grades, dates)

        val riskFactors = identifySubjectRisks(subject, grades, dates, timeSpent)
        val strengths = identifySubjectStrengths(subject, grades, dates, timeSpent)
        val recommendations = generateSubjectRecommendations(subject, gradeTrend, consistencyScore, grades)

        return PerformanceMetrics(
            overallGrade = overallGrade,
            gradeTrend = gradeTrend,
            consistencyScore = consistencyScore,
            improvementRate = improvementRate,
            riskFactors = riskFactors,
            strengths = strengths,
            recommendations = recommendations
        )
    }

    /**
     * Анализирует тренд оценок.
     */
    private fun analyzeGradeTrend(
        grades: Map<String, List<Int>>,
        dates: Map<String, List<java.time.LocalDate>>
    ): Trend {
        val allGrades = grades.values.flatten()
        if (allGrades.size < 3) return Trend.STABLE

        // Простой анализ тренда: сравнение первой и второй половины
        val midPoint = allGrades.size / 2
        val firstHalf = allGrades.take(midPoint).average()
        val secondHalf = allGrades.takeLast(allGrades.size - midPoint).average()

        val difference = secondHalf - firstHalf
        val threshold = allGrades.average() * 0.1 // 10% изменение

        return when {
            Math.abs(difference) < threshold -> Trend.STABLE
            difference > threshold -> Trend.IMPROVING
            difference < -threshold -> Trend.DECLINING
            else -> Trend.VOLATILE
        }
    }

    /**
     * Вычисляет консистентность оценок.
     */
    private fun calculateConsistencyScore(grades: Map<String, List<Int>>): Double {
        val allGrades = grades.values.flatten()
        if (allGrades.size < 2) return 1.0

        val average = allGrades.average()
        val variance = allGrades.map { (it - average) * (it - average) }.average()
        val standardDeviation = Math.sqrt(variance)

        // Консистентность = 1 - (стандартное отклонение / максимальное возможное отклонение)
        return 1.0 - (standardDeviation / 2.0).coerceIn(0.0, 1.0)
    }

    /**
     * Вычисляет консистентность для одного предмета.
     */
    private fun calculateSubjectConsistency(grades: List<Int>): Double {
        return calculateConsistencyScore(mapOf("subject" to grades))
    }

    /**
     * Вычисляет скорость улучшения.
     */
    private fun calculateImprovementRate(
        grades: Map<String, List<Int>>,
        dates: Map<String, List<java.time.LocalDate>>
    ): Double {
        // Упрощённый расчёт - среднее изменение по всем предметам
        return grades.mapNotNull { (subject, subjectGrades) ->
            val subjectDates = dates[subject] ?: return@mapNotNull null
            if (subjectGrades.size >= 2 && subjectDates.size == subjectGrades.size) {
                calculateSubjectImprovement(subjectGrades, subjectDates)
            } else null
        }.average().takeIf { it.isFinite() } ?: 0.0
    }

    /**
     * Вычисляет скорость улучшения для предмета.
     */
    private fun calculateSubjectImprovement(grades: List<Int>, dates: List<java.time.LocalDate>): Double {
        if (grades.size < 2 || dates.size != grades.size) return 0.0

        val sortedPairs = dates.zip(grades).sortedBy { it.first }
        val firstGrade = sortedPairs.first().second
        val lastGrade = sortedPairs.last().second
        val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(sortedPairs.first().first, sortedPairs.last().first)

        return if (daysDiff > 0) {
            (lastGrade - firstGrade).toDouble() / (daysDiff / 30.0) // оценок в месяц
        } else 0.0
    }

    /**
     * Определяет факторы риска.
     */
    private fun identifyRiskFactors(
        grades: Map<String, List<Int>>,
        dates: Map<String, List<java.time.LocalDate>>,
        timeSpent: Map<String, Int>
    ): List<String> {
        val risks = mutableListOf<String>()

        // Анализ низких оценок
        val lowGradesCount = grades.values.flatten().count { it < 3 }
        if (lowGradesCount > grades.values.flatten().size * 0.2) {
            risks.add("Высокий процент неудовлетворительных оценок")
        }

        // Анализ тренда
        if (analyzeGradeTrend(grades, dates) == Trend.DECLINING) {
            risks.add("Наблюдается тенденция к снижению оценок")
        }

        // Анализ распределения оценок
        val averageGrade = grades.values.flatten().average()
        if (calculateConsistencyScore(grades) < 0.5) {
            risks.add("Оценки очень нестабильны")
        }

        // Анализ времени
        val averageTime = timeSpent.values.average().takeIf { it.isFinite() }
        if (averageTime != null && averageTime < 300) { // менее 5 часов в неделю
            risks.add("Недостаточно времени уделяется учёбе")
        }

        return risks
    }

    /**
     * Определяет сильные стороны.
     */
    private fun identifyStrengths(
        grades: Map<String, List<Int>>,
        dates: Map<String, List<java.time.LocalDate>>,
        timeSpent: Map<String, Int>
    ): List<String> {
        val strengths = mutableListOf<String>()

        val averageGrade = grades.values.flatten().average()
        if (averageGrade >= 4.0) {
            strengths.add("Высокий средний балл (${String.format("%.1f", averageGrade)})")
        }

        if (analyzeGradeTrend(grades, dates) == Trend.IMPROVING) {
            strengths.add("Стабильное улучшение результатов")
        }

        if (calculateConsistencyScore(grades) >= 0.7) {
            strengths.add("Консистентные и стабильные оценки")
        }

        val excellentGrades = grades.values.flatten().count { it == 5 }
        if (excellentGrades > 0) {
            strengths.add("$excellentGrades отличных оценок")
        }

        return strengths
    }

    /**
     * Определяет риски для предмета.
     */
    private fun identifySubjectRisks(
        subject: String,
        grades: List<Int>,
        dates: List<java.time.LocalDate>,
        timeSpent: Int?
    ): List<String> {
        val risks = mutableListOf<String>()

        if (grades.average() < 3.0) {
            risks.add("Низкий средний балл по предмету")
        }

        if (grades.any { it < 3 }) {
            risks.add("Есть неудовлетворительные оценки")
        }

        if (timeSpent != null && timeSpent < 120) { // менее 2 часов в неделю
            risks.add("Недостаточно времени уделяется предмету")
        }

        return risks
    }

    /**
     * Определяет сильные стороны предмета.
     */
    private fun identifySubjectStrengths(
        subject: String,
        grades: List<Int>,
        dates: List<java.time.LocalDate>,
        timeSpent: Int?
    ): List<String> {
        val strengths = mutableListOf<String>()

        if (grades.average() >= 4.0) {
            strengths.add("Хороший средний балл по предмету")
        }

        val excellentCount = grades.count { it == 5 }
        if (excellentCount > 0) {
            strengths.add("$excellentCount отличных оценок")
        }

        return strengths
    }

    /**
     * Генерирует общие рекомендации.
     */
    private fun generateRecommendations(
        trend: Trend,
        consistency: Double,
        riskFactors: List<String>
    ): List<String> {
        val recommendations = mutableListOf<String>()

        when (trend) {
            Trend.IMPROVING -> recommendations.add("Продолжайте текущий подход - результаты улучшаются!")
            Trend.STABLE -> recommendations.add("Результаты стабильны - попробуйте новые методы для улучшения")
            Trend.DECLINING -> recommendations.add("Результаты снижаются - требуется дополнительная работа")
            Trend.VOLATILE -> recommendations.add("Результаты нестабильны - нужен более системный подход")
        }

        if (consistency < 0.5) {
            recommendations.add("Улучшите консистентность подготовки к урокам")
        }

        if (riskFactors.contains("Недостаточно времени уделяется учёбе")) {
            recommendations.add("Увеличьте время на самостоятельную подготовку")
        }

        return recommendations
    }

    /**
     * Генерирует рекомендации для предмета.
     */
    private fun generateSubjectRecommendations(
        subject: String,
        trend: Trend,
        consistency: Double,
        grades: List<Int>
    ): List<String> {
        val recommendations = mutableListOf<String>()

        if (grades.average() < 3.5) {
            recommendations.add("Увеличьте время на подготовку по $subject")
        }

        when (trend) {
            Trend.DECLINING -> recommendations.add("Нужен дополнительный разбор сложных тем по $subject")
            Trend.IMPROVING -> recommendations.add("Продолжайте успешную подготовку по $subject")
            else -> {}
        }

        if (consistency < 0.6) {
            recommendations.add("Организуйте регулярные занятия по $subject")
        }

        return recommendations
    }

    /**
     * Вычисляет недельный прогресс.
     */
    private fun calculateWeeklyProgress(
        grades: List<Int>,
        dates: List<java.time.LocalDate>
    ): Map<java.time.LocalDate, Double> {
        if (dates.size != grades.size) return emptyMap()

        return dates.zip(grades).groupBy { it.first }
            .mapValues { (_, pairs) -> pairs.map { it.second.toDouble() }.average() }
    }

    /**
     * Предсказывает будущую успеваемость.
     */
    fun predictPerformance(
        subject: String,
        grades: List<Int>,
        dates: List<java.time.LocalDate>,
        targetGrade: Int = 4
    ): PerformancePrediction {
        if (grades.size < 2 || dates.size != grades.size) {
            return PerformancePrediction(
                subject = subject,
                predictedGrade = grades.average(),
                confidence = 0.5,
                factors = mapOf("Недостаточно данных" to 0.0)
            )
        }

        val improvement = calculateSubjectImprovement(grades, dates)
        val currentGrade = grades.average()
        val monthsToPredict = 1.0

        val predictedGrade = currentGrade + (improvement * monthsToPredict)
        val confidence = minOf(0.9, grades.size / 10.0) // больше оценок = выше уверенность

        val factors = mutableMapOf<String, Double>()
        factors["Текущий тренд"] = improvement * monthsToPredict
        factors["Консистентность"] = calculateSubjectConsistency(grades) - 0.5
        factors["Количество оценок"] = minOf(grades.size / 10.0, 0.3)

        val timeToTarget = if (improvement > 0) {
            ((targetGrade - currentGrade) / improvement).toInt().coerceAtLeast(1)
        } else null

        return PerformancePrediction(
            subject = subject,
            predictedGrade = predictedGrade.coerceIn(1.0, 5.0),
            confidence = confidence,
            factors = factors,
            timeToTarget = timeToTarget
        )
    }

    /**
     * Генерирует персонализированные рекомендации по улучшению.
     */
    fun generateStudyRecommendations(
        subjectAnalyses: List<SubjectAnalysis>,
        availableTime: Int // минуты в неделю
    ): List<StudyRecommendation> {
        val recommendations = mutableListOf<StudyRecommendation>()

        // Рекомендации для предметов с низкими оценками
        subjectAnalyses.filter { it.metrics.overallGrade < 3.5 }
            .sortedBy { it.metrics.overallGrade }
            .take(3)
            .forEach { analysis ->
                recommendations.add(StudyRecommendation(
                    priority = Priority.HIGH,
                    category = analysis.subject,
                    action = "Увеличить время на подготовку по ${analysis.subject}",
                    expectedImpact = 0.5,
                    timeRequired = 180 // 3 часа
                ))
            }

        // Рекомендации для предметов с низкой консистентностью
        subjectAnalyses.filter { it.metrics.consistencyScore < 0.6 }
            .forEach { analysis ->
                recommendations.add(StudyRecommendation(
                    priority = Priority.MEDIUM,
                    category = analysis.subject,
                    action = "Организовать регулярные занятия по ${analysis.subject}",
                    expectedImpact = 0.3,
                    timeRequired = 120
                ))
            }

        // Рекомендации по трендам
        subjectAnalyses.filter { it.metrics.gradeTrend == Trend.DECLINING }
            .forEach { analysis ->
                recommendations.add(StudyRecommendation(
                    priority = Priority.HIGH,
                    category = analysis.subject,
                    action = "Разобрать сложные темы по ${analysis.subject}",
                    expectedImpact = 0.4,
                    timeRequired = 150
                ))
            }

        return recommendations.sortedBy { it.priority.ordinal }
    }

    /**
     * Анализирует паттерны обучения.
     */
    fun analyzeStudyPatterns(
        studySessions: List<StudySession>,
        dateRange: ClosedRange<java.time.LocalDate>
    ): StudyPattern {
        if (studySessions.isEmpty()) {
            return StudyPattern(
                peakStudyTime = null,
                preferredStudyDays = emptyList(),
                averageSessionLength = 0,
                studyFrequency = 0.0,
                consistencyPattern = ConsistencyPattern.INCONSISTENT
            )
        }

        // Анализ времени занятий
        val sessionTimes = studySessions.map { it.startTime }
        val timeGroups = sessionTimes.groupBy { it.hour }
        val peakHour = timeGroups.maxByOrNull { it.value.size }?.key
        val peakStudyTime = peakHour?.let { java.time.LocalTime.of(it, 0) }

        // Анализ дней недели
        val studyDays = studySessions.map { it.date.dayOfWeek }.distinct()
        val dayFrequency = studySessions.groupBy { it.date.dayOfWeek }
            .mapValues { it.value.size }
        val preferredStudyDays = dayFrequency.filter { it.value >= dayFrequency.values.average() }
            .keys.toList()

        // Средняя длина сессии
        val averageSessionLength = studySessions.map { it.duration }.average().toInt()

        // Частота занятий
        val daysInRange = java.time.temporal.ChronoUnit.DAYS.between(dateRange.start, dateRange.endInclusive) + 1
        val uniqueStudyDays = studySessions.map { it.date }.distinct().size
        val studyFrequency = uniqueStudyDays.toDouble() / (daysInRange / 7.0) // сессий в неделю

        // Анализ консистентности
        val consistencyPattern = when {
            studyFrequency >= 5 -> ConsistencyPattern.HIGHLY_CONSISTENT
            studyFrequency >= 3 -> ConsistencyPattern.MODERATELY_CONSISTENT
            studyFrequency >= 1 -> ConsistencyPattern.INCONSISTENT
            else -> ConsistencyPattern.ERRATIC
        }

        return StudyPattern(
            peakStudyTime = peakStudyTime,
            preferredStudyDays = preferredStudyDays,
            averageSessionLength = averageSessionLength,
            studyFrequency = studyFrequency,
            consistencyPattern = consistencyPattern
        )
    }

    data class StudySession(
        val date: java.time.LocalDate,
        val startTime: java.time.LocalTime,
        val duration: Int,
        val subject: String? = null
    )
}


