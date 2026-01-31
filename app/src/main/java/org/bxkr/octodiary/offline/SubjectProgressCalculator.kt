package org.bxkr.octodiary.offline


import androidx.compose.material.icons.Icons
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Калькулятор прогресса по предметам.
 * Анализирует успеваемость, тренды и предсказывает будущие результаты.
 */
object SubjectProgressCalculator {

    data class SubjectProgress(
        val subject: String,
        val currentAverage: Double,
        val trend: Trend,
        val trendSlope: Double, // наклон тренда (изменение в месяц)
        val prediction: Double, // прогноз на следующий месяц
        val consistency: Double, // консистентность оценок (0-1)
        val strengthAreas: List<String>,
        val improvementAreas: List<String>,
        val studyRecommendations: List<String>
    )

    enum class Trend {
        STRONG_IMPROVEMENT, // Сильное улучшение (>0.5 в месяц)
        IMPROVING,          // Улучшение (0.1-0.5)
        STABLE,             // Стабильный (изменение <0.1)
        DECLINING,          // Ухудшение (-0.1 - -0.5)
        STRONG_DECLINE      // Сильное ухудшение (<-0.5)
    }

    data class GradeEntry(
        val grade: Int,
        val date: LocalDate,
        val topic: String? = null,
        val assessmentType: AssessmentType = AssessmentType.TEST
    )

    enum class AssessmentType {
        TEST, QUIZ, HOMEWORK, PROJECT, EXAM
    }

    data class ProgressReport(
        val subject: String,
        val period: ClosedRange<LocalDate>,
        val grades: List<GradeEntry>,
        val progress: SubjectProgress,
        val achievements: List<String>,
        val concerns: List<String>,
        val nextMilestones: List<String>
    )

    /**
     * Вычисляет прогресс по предмету на основе оценок.
     */
    fun calculateProgress(
        subject: String,
        grades: List<GradeEntry>,
        analysisPeriod: Int = 90 // дней для анализа
    ): SubjectProgress {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(analysisPeriod.toLong())

        val relevantGrades = grades.filter { it.date >= startDate && it.date <= endDate }
        if (relevantGrades.isEmpty()) {
            return SubjectProgress(
                subject = subject,
                currentAverage = 0.0,
                trend = Trend.STABLE,
                trendSlope = 0.0,
                prediction = 0.0,
                consistency = 0.0,
                strengthAreas = emptyList(),
                improvementAreas = listOf("Недостаточно данных для анализа"),
                studyRecommendations = listOf("Нужно больше оценок для анализа прогресса")
            )
        }

        val currentAverage = relevantGrades.map { it.grade.toDouble() }.average()
        val trendSlope = calculateTrendSlope(relevantGrades)
        val trend = determineTrend(trendSlope)
        val prediction = predictNextGrade(relevantGrades, trendSlope)
        val consistency = calculateConsistency(relevantGrades)

        val strengthAreas = identifyStrengthAreas(relevantGrades, currentAverage)
        val improvementAreas = identifyImprovementAreas(relevantGrades, currentAverage)
        val studyRecommendations = generateStudyRecommendations(trend, consistency, relevantGrades)

        return SubjectProgress(
            subject = subject,
            currentAverage = currentAverage,
            trend = trend,
            trendSlope = trendSlope,
            prediction = prediction.coerceIn(1.0, 5.0), // ограничиваем диапазон оценок
            consistency = consistency,
            strengthAreas = strengthAreas,
            improvementAreas = improvementAreas,
            studyRecommendations = studyRecommendations
        )
    }

    /**
     * Вычисляет наклон тренда оценок.
     */
    private fun calculateTrendSlope(grades: List<GradeEntry>): Double {
        if (grades.size < 2) return 0.0

        val sortedGrades = grades.sortedBy { it.date }
        val days = sortedGrades.map { ChronoUnit.DAYS.between(sortedGrades.first().date, it.date).toDouble() }
        val gradeValues = sortedGrades.map { it.grade.toDouble() }

        // Линейная регрессия: y = mx + b
        val n = days.size.toDouble()
        val sumX = days.sum()
        val sumY = gradeValues.sum()
        val sumXY = days.zip(gradeValues).sumOf { (x, y) -> x * y }
        val sumXX = days.sumOf { it * it }

        val slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX)

        // Нормализуем к изменению в месяц
        return slope * 30.0
    }

    /**
     * Определяет тренд на основе наклона.
     */
    private fun determineTrend(slope: Double): Trend {
        return when {
            slope > 0.5 -> Trend.STRONG_IMPROVEMENT
            slope > 0.1 -> Trend.IMPROVING
            slope >= -0.1 -> Trend.STABLE
            slope >= -0.5 -> Trend.DECLINING
            else -> Trend.STRONG_DECLINE
        }
    }

    /**
     * Предсказывает следующую оценку.
     */
    private fun predictNextGrade(grades: List<GradeEntry>, slope: Double): Double {
        if (grades.isEmpty()) return 3.0

        val lastGrade = grades.maxByOrNull { it.date }?.grade?.toDouble() ?: 3.0
        val daysToNext = 30.0 // Предсказание на месяц вперед

        return lastGrade + (slope / 30.0) * daysToNext
    }

    /**
     * Вычисляет консистентность оценок (0-1).
     */
    private fun calculateConsistency(grades: List<GradeEntry>): Double {
        if (grades.size < 2) return 1.0

        val gradeValues = grades.map { it.grade.toDouble() }
        val average = gradeValues.average()
        val variance = gradeValues.map { (it - average) * (it - average) }.average()
        val standardDeviation = Math.sqrt(variance)

        // Консистентность = 1 - (стандартное отклонение / максимальное возможное отклонение)
        val maxDeviation = 2.0 // максимальное отклонение в пятибалльной шкале
        return (1.0 - (standardDeviation / maxDeviation)).coerceIn(0.0, 1.0)
    }

    /**
     * Определяет сильные стороны.
     */
    private fun identifyStrengthAreas(grades: List<GradeEntry>, average: Double): List<String> {
        val strengths = mutableListOf<String>()

        // Анализ по типам оценок
        val assessmentTypes = grades.groupBy { it.assessmentType }
        assessmentTypes.forEach { (type, typeGrades) ->
            val typeAverage = typeGrades.map { it.grade.toDouble() }.average()
            if (typeAverage > average + 0.3) {
                strengths.add("Хорошие результаты в ${getAssessmentTypeName(type)}")
            }
        }

        // Анализ по темам
        val topicGrades = grades.filter { it.topic != null }.groupBy { it.topic!! }
        topicGrades.forEach { (topic, topicGradeList) ->
            val topicAverage = topicGradeList.map { it.grade.toDouble() }.average()
            if (topicAverage >= 4.0) {
                strengths.add("Отличное понимание темы '$topic'")
            }
        }

        if (strengths.isEmpty()) {
            strengths.add("Консистентные результаты по всем типам заданий")
        }

        return strengths
    }

    /**
     * Определяет области для улучшения.
     */
    private fun identifyImprovementAreas(grades: List<GradeEntry>, average: Double): List<String> {
        val improvements = mutableListOf<String>()

        // Анализ по типам оценок
        val assessmentTypes = grades.groupBy { it.assessmentType }
        assessmentTypes.forEach { (type, typeGrades) ->
            val typeAverage = typeGrades.map { it.grade.toDouble() }.average()
            if (typeAverage < average - 0.3) {
                improvements.add("Нужно улучшить результаты в ${getAssessmentTypeName(type)}")
            }
        }

        // Анализ по темам
        val topicGrades = grades.filter { it.topic != null }.groupBy { it.topic!! }
        topicGrades.forEach { (topic, topicGradeList) ->
            val topicAverage = topicGradeList.map { it.grade.toDouble() }.average()
            if (topicAverage < 3.0) {
                improvements.add("Требуется дополнительная работа над темой '$topic'")
            }
        }

        // Анализ низких оценок
        val lowGrades = grades.filter { it.grade < 3 }
        if (lowGrades.isNotEmpty()) {
            improvements.add("Анализ причин низких оценок (${lowGrades.size} случаев)")
        }

        if (improvements.isEmpty()) {
            improvements.add("Общий уровень знаний хороший, продолжайте в том же духе")
        }

        return improvements
    }

    /**
     * Генерирует рекомендации по изучению.
     */
    private fun generateStudyRecommendations(
        trend: Trend,
        consistency: Double,
        grades: List<GradeEntry>
    ): List<String> {
        val recommendations = mutableListOf<String>()

        when (trend) {
            Trend.STRONG_IMPROVEMENT -> {
                recommendations.add("Отличная динамика! Продолжайте текущий подход к обучению")
                recommendations.add("Можно увеличить сложность заданий для дальнейшего прогресса")
            }
            Trend.IMPROVING -> {
                recommendations.add("Прогресс налицо! Анализируйте, что работает лучше всего")
                recommendations.add("Закрепите успешные методы обучения")
            }
            Trend.STABLE -> {
                recommendations.add("Результаты стабильны - хорошая основа для роста")
                recommendations.add("Попробуйте новые методы обучения для улучшения результатов")
            }
            Trend.DECLINING -> {
                recommendations.add("Наблюдается снижение результатов - требуется дополнительное внимание")
                recommendations.add("Вернитесь к основам и укрепите понимание базовых понятий")
            }
            Trend.STRONG_DECLINE -> {
                recommendations.add("Серьёзное снижение результатов - нужна срочная помощь")
                recommendations.add("Обратитесь к учителю или репетитору за дополнительной поддержкой")
            }
        }

        if (consistency < 0.5) {
            recommendations.add("Оценки нестабильны - работайте над консистентностью подготовки")
            recommendations.add("Создайте регулярный график повторения материала")
        }

        // Рекомендации по типам заданий
        val assessmentTypes = grades.groupBy { it.assessmentType }
        val homeworkGrades = assessmentTypes[AssessmentType.HOMEWORK]?.map { it.grade.toDouble() }?.average()
        val testGrades = assessmentTypes[AssessmentType.TEST]?.map { it.grade.toDouble() }?.average()

        if (homeworkGrades != null && testGrades != null) {
            if (homeworkGrades > testGrades + 0.5) {
                recommendations.add("Домашние задания получаются лучше тестов - практикуйте тестовый формат")
            } else if (testGrades > homeworkGrades + 0.5) {
                recommendations.add("На тестах получается лучше - продолжайте углублять понимание")
            }
        }

        return recommendations
    }

    /**
     * Получает название типа оценки.
     */
    private fun getAssessmentTypeName(type: AssessmentType): String {
        return when (type) {
            AssessmentType.TEST -> "контрольных работах"
            AssessmentType.QUIZ -> "тестах"
            AssessmentType.HOMEWORK -> "домашних заданиях"
            AssessmentType.PROJECT -> "проектах"
            AssessmentType.EXAM -> "экзаменах"
        }
    }

    /**
     * Генерирует полный отчёт о прогрессе.
     */
    fun generateProgressReport(
        subject: String,
        grades: List<GradeEntry>,
        period: ClosedRange<LocalDate> = LocalDate.now().minusDays(90)..LocalDate.now()
    ): ProgressReport {
        val periodGrades = grades.filter { it.date in period }
        val progress = calculateProgress(subject, periodGrades)

        val achievements = identifyAchievements(progress, periodGrades)
        val concerns = identifyConcerns(progress, periodGrades)
        val nextMilestones = suggestNextMilestones(progress, periodGrades)

        return ProgressReport(
            subject = subject,
            period = period,
            grades = periodGrades,
            progress = progress,
            achievements = achievements,
            concerns = concerns,
            nextMilestones = nextMilestones
        )
    }

    /**
     * Определяет достижения.
     */
    private fun identifyAchievements(progress: SubjectProgress, grades: List<GradeEntry>): List<String> {
        val achievements = mutableListOf<String>()

        if (progress.currentAverage >= 4.5) {
            achievements.add("Отличный средний балл (${String.format("%.1f", progress.currentAverage)})")
        } else if (progress.currentAverage >= 4.0) {
            achievements.add("Хороший средний балл (${String.format("%.1f", progress.currentAverage)})")
        }

        if (progress.consistency >= 0.8) {
            achievements.add("Высокая консистентность результатов")
        }

        when (progress.trend) {
            Trend.STRONG_IMPROVEMENT -> achievements.add("Значительный прогресс в последнее время")
            Trend.IMPROVING -> achievements.add("Стабильное улучшение результатов")
            else -> {}
        }

        val highGrades = grades.count { it.grade >= 5 }
        if (highGrades > 0) {
            achievements.add("$highGrades отличных оценок")
        }

        return achievements
    }

    /**
     * Определяет проблемные области.
     */
    private fun identifyConcerns(progress: SubjectProgress, grades: List<GradeEntry>): List<String> {
        val concerns = mutableListOf<String>()

        if (progress.currentAverage < 3.0) {
            concerns.add("Низкий средний балл требует внимания")
        }

        if (progress.consistency < 0.5) {
            concerns.add("Нестабильные результаты")
        }

        when (progress.trend) {
            Trend.DECLINING, Trend.STRONG_DECLINE -> {
                concerns.add("Наблюдается тенденция к снижению результатов")
            }
            else -> {}
        }

        val lowGrades = grades.count { it.grade <= 2 }
        if (lowGrades > 0) {
            concerns.add("$lowGrades неудовлетворительных оценок")
        }

        return concerns
    }

    /**
     * Предлагает следующие цели.
     */
    private fun suggestNextMilestones(progress: SubjectProgress, grades: List<GradeEntry>): List<String> {
        val milestones = mutableListOf<String>()

        val targetGrade = when {
            progress.currentAverage < 3.5 -> 4.0
            progress.currentAverage < 4.5 -> 4.5
            else -> 5.0
        }

        milestones.add("Достичь среднего балла ${targetGrade} в следующем месяце")

        if (progress.consistency < 0.7) {
            milestones.add("Стабилизировать результаты (добиться консистентности >70%)")
        }

        val currentMonth = LocalDate.now().month
        val nextMonthGrades = grades.count { it.date.month == currentMonth }
        if (nextMonthGrades < 3) {
            milestones.add("Получить больше оценок для точного анализа прогресса")
        }

        // Персональные цели
        if (progress.improvementAreas.any { it.contains("тест") }) {
            milestones.add("Улучшить результаты контрольных работ")
        }

        if (progress.improvementAreas.any { it.contains("домашн") }) {
            milestones.add("Повысить качество выполнения домашних заданий")
        }

        return milestones
    }

    /**
     * Сравнивает прогресс между предметами.
     */
    fun compareSubjectsProgress(subjectsProgress: Map<String, SubjectProgress>): SubjectComparison {
        if (subjectsProgress.isEmpty()) {
            return SubjectComparison(
                bestSubject = "",
                worstSubject = "",
                averageProgress = 0.0,
                insights = listOf("Недостаточно данных для сравнения")
            )
        }

        val bestSubject = subjectsProgress.maxByOrNull { it.value.currentAverage }?.key ?: ""
        val worstSubject = subjectsProgress.minByOrNull { it.value.currentAverage }?.key ?: ""
        val averageProgress = subjectsProgress.values.map { it.currentAverage }.average()

        val insights = mutableListOf<String>()

        // Анализ трендов
        val improvingSubjects = subjectsProgress.count { it.value.trend == Trend.IMPROVING || it.value.trend == Trend.STRONG_IMPROVEMENT }
        if (improvingSubjects > 0) {
            insights.add("$improvingSubjects предметов показывают положительную динамику")
        }

        val decliningSubjects = subjectsProgress.count { it.value.trend == Trend.DECLINING || it.value.trend == Trend.STRONG_DECLINE }
        if (decliningSubjects > 0) {
            insights.add("$decliningSubjects предметов требуют дополнительного внимания")
        }

        // Анализ консистентности
        val consistentSubjects = subjectsProgress.count { it.value.consistency >= 0.7 }
        insights.add("$consistentSubjects предметов имеют стабильные результаты")

        return SubjectComparison(
            bestSubject = bestSubject,
            worstSubject = worstSubject,
            averageProgress = averageProgress,
            insights = insights
        )
    }

    data class SubjectComparison(
        val bestSubject: String,
        val worstSubject: String,
        val averageProgress: Double,
        val insights: List<String>
    )
}


