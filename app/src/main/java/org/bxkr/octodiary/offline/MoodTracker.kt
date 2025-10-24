package org.bxkr.octodiary.offline

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Трекер здоровья и настроения для отслеживания эмоционального состояния.
 * Помогает анализировать влияние учебного процесса на здоровье и настроение.
 */
object MoodTracker {

    data class MoodEntry(
        val id: String,
        val date: LocalDate,
        val time: LocalTime = LocalTime.now(),
        val mood: MoodLevel,
        val energy: EnergyLevel,
        val stress: StressLevel,
        val sleepHours: Double? = null,
        val notes: String? = null,
        val tags: List<String> = emptyList(),
        val factors: List<String> = emptyList(), // факторы влияния
        val activities: List<String> = emptyList(), // выполненные активности
        val createdAt: LocalDateTime = LocalDateTime.now()
    )

    enum class MoodLevel {
        TERRIBLE, VERY_BAD, BAD, NEUTRAL, GOOD, VERY_GOOD, EXCELLENT
    }

    enum class EnergyLevel {
        EXHAUSTED, LOW, MODERATE, HIGH, VERY_HIGH
    }

    enum class StressLevel {
        NONE, LOW, MODERATE, HIGH, EXTREME
    }

    data class HealthMetrics(
        val date: LocalDate,
        val moodScore: Double, // 0-6
        val energyScore: Double, // 0-4
        val stressScore: Double, // 0-4
        val sleepQuality: Double? = null, // 0-10
        val overallWellness: Double // композитный показатель
    )

    data class WellnessReport(
        val period: kotlin.ranges.ClosedRange<LocalDate>,
        val averageMood: Double,
        val averageEnergy: Double,
        val averageStress: Double,
        val averageSleep: Double?,
        val moodTrend: Trend,
        val energyTrend: Trend,
        val stressTrend: Trend,
        val insights: List<String>,
        val recommendations: List<String>,
        val criticalDays: List<LocalDate>
    )

    enum class Trend {
        IMPROVING, STABLE, DECLINING, VOLATILE
    }

    data class ActivityImpact(
        val activity: String,
        val moodImpact: Double,
        val energyImpact: Double,
        val frequency: Int,
        val correlationStrength: Double
    )

    /**
     * Создаёт запись о настроении.
     */
    fun createMoodEntry(
        date: LocalDate,
        mood: MoodLevel,
        energy: EnergyLevel,
        stress: StressLevel,
        sleepHours: Double? = null,
        notes: String? = null,
        tags: List<String> = emptyList(),
        factors: List<String> = emptyList(),
        activities: List<String> = emptyList()
    ): MoodEntry {
        return MoodEntry(
            id = generateEntryId(),
            date = date,
            mood = mood,
            energy = energy,
            stress = stress,
            sleepHours = sleepHours,
            notes = notes,
            tags = tags,
            factors = factors,
            activities = activities
        )
    }

    /**
     * Вычисляет метрики здоровья для даты.
     */
    fun calculateHealthMetrics(entry: MoodEntry): HealthMetrics {
        val moodScore = when (entry.mood) {
            MoodLevel.TERRIBLE -> 0.0
            MoodLevel.VERY_BAD -> 1.0
            MoodLevel.BAD -> 2.0
            MoodLevel.NEUTRAL -> 3.0
            MoodLevel.GOOD -> 4.0
            MoodLevel.VERY_GOOD -> 5.0
            MoodLevel.EXCELLENT -> 6.0
        }

        val energyScore = when (entry.energy) {
            EnergyLevel.EXHAUSTED -> 0.0
            EnergyLevel.LOW -> 1.0
            EnergyLevel.MODERATE -> 2.0
            EnergyLevel.HIGH -> 3.0
            EnergyLevel.VERY_HIGH -> 4.0
        }

        val stressScore = when (entry.stress) {
            StressLevel.NONE -> 0.0
            StressLevel.LOW -> 1.0
            StressLevel.MODERATE -> 2.0
            StressLevel.HIGH -> 3.0
            StressLevel.EXTREME -> 4.0
        }

        val sleepQuality = entry.sleepHours?.let { hours ->
            when {
                hours >= 8.0 -> 9.0
                hours >= 7.0 -> 7.0
                hours >= 6.0 -> 5.0
                else -> 2.0
            }
        }

        // Композитный показатель благополучия
        val overallWellness = calculateOverallWellness(moodScore, energyScore, stressScore, sleepQuality)

        return HealthMetrics(
            date = entry.date,
            moodScore = moodScore,
            energyScore = energyScore,
            stressScore = stressScore,
            sleepQuality = sleepQuality,
            overallWellness = overallWellness
        )
    }

    /**
     * Вычисляет общий показатель благополучия.
     */
    private fun calculateOverallWellness(
        mood: Double,
        energy: Double,
        stress: Double,
        sleep: Double?
    ): Double {
        val baseScore = (mood / 6.0 * 0.4) + (energy / 4.0 * 0.3) + ((4.0 - stress) / 4.0 * 0.3)
        val sleepBonus = sleep?.let { (it / 10.0) * 0.2 } ?: 0.0
        return minOf(baseScore + sleepBonus, 1.0)
    }

    /**
     * Генерирует отчёт о благополучии за период.
     */
    fun generateWellnessReport(
        entries: List<MoodEntry>,
        period: ClosedRange<LocalDate> = LocalDate.now().minusDays(30)..LocalDate.now()
    ): WellnessReport {
        val periodEntries = entries.filter { it.date in period }
        if (periodEntries.isEmpty()) {
            return WellnessReport(
                period = period,
                averageMood = 3.0,
                averageEnergy = 2.0,
                averageStress = 2.0,
                averageSleep = null,
                moodTrend = Trend.STABLE,
                energyTrend = Trend.STABLE,
                stressTrend = Trend.STABLE,
                insights = listOf("Недостаточно данных для анализа"),
                recommendations = listOf("Добавьте больше записей о настроении"),
                criticalDays = emptyList()
            )
        }

        val metrics = periodEntries.map { calculateHealthMetrics(it) }

        val averageMood = metrics.map { it.moodScore }.average()
        val averageEnergy = metrics.map { it.energyScore }.average()
        val averageStress = metrics.map { it.stressScore }.average()
        val averageSleep = metrics.mapNotNull { it.sleepQuality }.takeIf { it.isNotEmpty() }?.average()

        val moodTrend = calculateTrend(metrics.map { it.moodScore })
        val energyTrend = calculateTrend(metrics.map { it.energyScore })
        val stressTrend = calculateTrend(metrics.map { it.stressScore })

        val insights = generateInsights(metrics, periodEntries)
        val recommendations = generateRecommendations(metrics, moodTrend, energyTrend, stressTrend)
        val criticalDays = identifyCriticalDays(metrics)

        return WellnessReport(
            period = period,
            averageMood = averageMood,
            averageEnergy = averageEnergy,
            averageStress = averageStress,
            averageSleep = averageSleep,
            moodTrend = moodTrend,
            energyTrend = energyTrend,
            stressTrend = stressTrend,
            insights = insights,
            recommendations = recommendations,
            criticalDays = criticalDays
        )
    }

    /**
     * Вычисляет тренд показателей.
     */
    private fun calculateTrend(values: List<Double>): Trend {
        if (values.size < 3) return Trend.STABLE

        val firstHalf = values.take(values.size / 2).average()
        val secondHalf = values.takeLast(values.size / 2).average()
        val difference = secondHalf - firstHalf

        val threshold = values.average() * 0.1 // 10% изменение
        val volatility = calculateVolatility(values)

        return when {
            volatility > 0.5 -> Trend.VOLATILE
            difference > threshold -> Trend.IMPROVING
            difference < -threshold -> Trend.DECLINING
            else -> Trend.STABLE
        }
    }

    /**
     * Вычисляет волатильность (изменчивость) показателей.
     */
    private fun calculateVolatility(values: List<Double>): Double {
        if (values.size < 2) return 0.0

        val average = values.average()
        val variance = values.map { (it - average) * (it - average) }.average()
        val standardDeviation = Math.sqrt(variance)

        return standardDeviation / average // коэффициент вариации
    }

    /**
     * Генерирует инсайты на основе данных.
     */
    private fun generateInsights(metrics: List<HealthMetrics>, entries: List<MoodEntry>): List<String> {
        val insights = mutableListOf<String>()

        val moodEnergyCorrelation = calculateCorrelation(
            metrics.map { it.moodScore },
            metrics.map { it.energyScore }
        )

        if (moodEnergyCorrelation > 0.5) {
            insights.add("Настроение сильно коррелирует с уровнем энергии")
        } else if (moodEnergyCorrelation < -0.3) {
            insights.add("Низкая энергия часто сопровождается плохим настроением")
        }

        val lowSleepDays = entries.count { (it.sleepHours ?: 0.0) < 6.0 }
        if (lowSleepDays > metrics.size * 0.3) {
            insights.add("Часто отмечается недостаток сна (${lowSleepDays} дней)")
        }

        val highStressDays = metrics.count { it.stressScore >= 3.0 }
        if (highStressDays > metrics.size * 0.4) {
            insights.add("Высокий уровень стресса отмечается в ${highStressDays} днях")
        }

        val bestDay = metrics.maxByOrNull { it.overallWellness }
        bestDay?.let {
            insights.add("Лучший день по благополучию: ${it.date} (${String.format("%.1f", it.overallWellness * 100)}%)")
        }

        return insights
    }

    /**
     * Генерирует рекомендации на основе анализа.
     */
    private fun generateRecommendations(
        metrics: List<HealthMetrics>,
        moodTrend: Trend,
        energyTrend: Trend,
        stressTrend: Trend
    ): List<String> {
        val recommendations = mutableListOf<String>()

        when (moodTrend) {
            Trend.DECLINING -> recommendations.add("Настроение ухудшается - рассмотрите дополнительные перерывы в учёбе")
            Trend.VOLATILE -> recommendations.add("Настроение нестабильное - попробуйте ежедневные ритуалы для стабилизации")
            Trend.IMPROVING -> recommendations.add("Настроение улучшается - продолжайте текущие привычки")
            Trend.STABLE -> {}
        }

        when (energyTrend) {
            Trend.DECLINING -> recommendations.add("Уровень энергии снижается - увеличьте время отдыха и физической активности")
            Trend.VOLATILE -> recommendations.add("Энергия нестабильна - следите за режимом сна и питания")
            Trend.IMPROVING -> recommendations.add("Энергия растёт - поддерживайте активный образ жизни")
            Trend.STABLE -> {}
        }

        when (stressTrend) {
            Trend.IMPROVING -> recommendations.add("Стресс уменьшается - эффективны текущие методы борьбы со стрессом")
            Trend.DECLINING -> recommendations.add("Стресс растёт - добавьте техники релаксации")
            Trend.VOLATILE -> recommendations.add("Стресс нестабилен - практикуйте mindfulness")
            Trend.STABLE -> {}
        }

        val averageWellness = metrics.map { it.overallWellness }.average()
        when {
            averageWellness < 0.4 -> recommendations.add("Общее благополучие низкое - обратитесь к специалисту по необходимости")
            averageWellness < 0.6 -> recommendations.add("Рассмотрите улучшение баланса между учёбой и отдыхом")
            averageWellness > 0.8 -> recommendations.add("Отличное состояние! Поддерживайте текущий баланс")
        }

        return recommendations
    }

    /**
     * Определяет критические дни (низкое благополучие).
     */
    private fun identifyCriticalDays(metrics: List<HealthMetrics>): List<LocalDate> {
        return metrics.filter { it.overallWellness < 0.3 }
            .map { it.date }
            .sorted()
    }

    /**
     * Вычисляет корреляцию между двумя рядами данных.
     */
    private fun calculateCorrelation(x: List<Double>, y: List<Double>): Double {
        if (x.size != y.size || x.size < 2) return 0.0

        val n = x.size.toDouble()
        val sumX = x.sum()
        val sumY = y.sum()
        val sumXY = x.zip(y).sumOf { (a, b) -> a * b }
        val sumX2 = x.sumOf { it * it }
        val sumY2 = y.sumOf { it * it }

        val numerator = n * sumXY - sumX * sumY
        val denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY))

        return if (denominator != 0.0) numerator / denominator else 0.0
    }

    /**
     * Анализирует влияние активностей на настроение и энергию.
     */
    fun analyzeActivityImpact(entries: List<MoodEntry>): List<ActivityImpact> {
        val activityStats = mutableMapOf<String, MutableList<Pair<Double, Double>>>() // activity -> list of (mood, energy)

        for (entry in entries) {
            val moodScore = when (entry.mood) {
                MoodLevel.TERRIBLE -> 0.0
                MoodLevel.VERY_BAD -> 1.0
                MoodLevel.BAD -> 2.0
                MoodLevel.NEUTRAL -> 3.0
                MoodLevel.GOOD -> 4.0
                MoodLevel.VERY_GOOD -> 5.0
                MoodLevel.EXCELLENT -> 6.0
            }

            val energyScore = when (entry.energy) {
                EnergyLevel.EXHAUSTED -> 0.0
                EnergyLevel.LOW -> 1.0
                EnergyLevel.MODERATE -> 2.0
                EnergyLevel.HIGH -> 3.0
                EnergyLevel.VERY_HIGH -> 4.0
            }

            for (activity in entry.activities) {
                activityStats.getOrPut(activity) { mutableListOf() }
                    .add(moodScore to energyScore)
            }
        }

        return activityStats.map { (activity, scores) ->
            val frequency = scores.size
            val averageMood = scores.map { it.first }.average()
            val averageEnergy = scores.map { it.second }.average()

            // Упрощённый расчёт корреляции - в реальном приложении нужна более сложная логика
            val moodImpact = (averageMood - 3.0) / 3.0 // нормализуем к [-1, 1]
            val energyImpact = (averageEnergy - 2.0) / 2.0 // нормализуем к [-1, 1]

            ActivityImpact(
                activity = activity,
                moodImpact = moodImpact,
                energyImpact = energyImpact,
                frequency = frequency,
                correlationStrength = Math.abs(moodImpact) * frequency / entries.size.toDouble()
            )
        }.sortedByDescending { it.correlationStrength }
    }

    /**
     * Получает ежедневные подсказки на основе текущего состояния.
     */
    fun getDailyPrompt(currentMetrics: HealthMetrics? = null): String {
        val prompts = listOf(
            "Как вы чувствуете себя сегодня? Запишите свои эмоции.",
            "Что повлияло на ваше настроение сегодня?",
            "Как прошел сон? Сколько часов вы спали?",
            "Были ли моменты стресса сегодня? Что их вызвало?",
            "Какие активности помогли вам сегодня?",
            "Что можно улучшить в вашем распорядке дня?",
            "Как учеба влияет на ваше самочувствие?",
            "Что помогает вам расслабиться и восстановиться?"
        )

        return if (currentMetrics != null && currentMetrics.overallWellness < 0.5) {
            // Специальные подсказки для низкого благополучия
            listOf(
                "Сегодня был тяжелый день. Что конкретно повлияло на ваше состояние?",
                "Что могло бы улучшить ваше самочувствие прямо сейчас?",
                "Какие маленькие шаги вы можете предпринять для улучшения настроения?",
                "Стоит ли сегодня сделать перерыв в учёбе?"
            ).random()
        } else {
            prompts.random()
        }
    }

    /**
     * Создаёт шаблон быстрой записи.
     */
    fun createQuickEntry(
        mood: MoodLevel,
        energy: EnergyLevel,
        stress: StressLevel
    ): MoodEntry {
        return createMoodEntry(
            date = LocalDate.now(),
            mood = mood,
            energy = energy,
            stress = stress,
            notes = "Быстрая запись"
        )
    }

    /**
     * Получает статистику по периодам времени.
     */
    fun getTimeBasedStats(entries: List<MoodEntry>): Map<String, HealthMetrics> {
        val hourlyStats = mutableMapOf<String, MutableList<MoodEntry>>()

        for (entry in entries) {
            val hourKey = "${entry.time.hour}:00"
            hourlyStats.getOrPut(hourKey) { mutableListOf() }.add(entry)
        }

        return hourlyStats.mapValues { (_, hourEntries) ->
            val averageMetrics = hourEntries.map { calculateHealthMetrics(it) }
            HealthMetrics(
                date = LocalDate.now(), // не важно для статистики
                moodScore = averageMetrics.map { it.moodScore }.average(),
                energyScore = averageMetrics.map { it.energyScore }.average(),
                stressScore = averageMetrics.map { it.stressScore }.average(),
                sleepQuality = averageMetrics.mapNotNull { it.sleepQuality }.average().takeIf { it.isFinite() },
                overallWellness = averageMetrics.map { it.overallWellness }.average()
            )
        }
    }

    /**
     * Экспортирует данные о здоровье в читаемый формат.
     */
    fun exportHealthData(entries: List<MoodEntry>): String {
        return buildString {
            appendLine("Данные отслеживания здоровья и настроения")
            appendLine("Экспорт от ${LocalDateTime.now()}")
            appendLine("Всего записей: ${entries.size}")
            appendLine()

            val report = generateWellnessReport(entries)
            appendLine("Общая статистика за период:")
            appendLine("Среднее настроение: ${String.format("%.1f", report.averageMood)}/6")
            appendLine("Средняя энергия: ${String.format("%.1f", report.averageEnergy)}/4")
            appendLine("Средний стресс: ${String.format("%.1f", report.averageStress)}/4")
            if (report.averageSleep != null) {
                appendLine("Среднее качество сна: ${String.format("%.1f", report.averageSleep)}/10")
            }
            appendLine()

            appendLine("Инсайты:")
            report.insights.forEach { appendLine("• $it") }
            appendLine()

            appendLine("Рекомендации:")
            report.recommendations.forEach { appendLine("• $it") }
            appendLine()

            if (report.criticalDays.isNotEmpty()) {
                appendLine("Критические дни:")
                report.criticalDays.forEach { appendLine("• $it") }
                appendLine()
            }

            appendLine("Ежедневные записи:")
            entries.sortedByDescending { it.date }.forEach { entry ->
                appendLine("--- ${entry.date} ${entry.time} ---")
                appendLine("Настроение: ${getMoodDescription(entry.mood)}")
                appendLine("Энергия: ${getEnergyDescription(entry.energy)}")
                appendLine("Стресс: ${getStressDescription(entry.stress)}")
                entry.sleepHours?.let { appendLine("Сон: ${it} часов") }
                entry.notes?.let { appendLine("Заметки: $it") }
                if (entry.activities.isNotEmpty()) {
                    appendLine("Активности: ${entry.activities.joinToString(", ")}")
                }
                if (entry.factors.isNotEmpty()) {
                    appendLine("Факторы: ${entry.factors.joinToString(", ")}")
                }
                appendLine()
            }
        }
    }

    /**
     * Получает текстовое описание настроения.
     */
    private fun getMoodDescription(mood: MoodLevel): String {
        return when (mood) {
            MoodLevel.TERRIBLE -> "Ужасное"
            MoodLevel.VERY_BAD -> "Очень плохое"
            MoodLevel.BAD -> "Плохое"
            MoodLevel.NEUTRAL -> "Нейтральное"
            MoodLevel.GOOD -> "Хорошее"
            MoodLevel.VERY_GOOD -> "Очень хорошее"
            MoodLevel.EXCELLENT -> "Отличное"
        }
    }

    /**
     * Получает текстовое описание энергии.
     */
    private fun getEnergyDescription(energy: EnergyLevel): String {
        return when (energy) {
            EnergyLevel.EXHAUSTED -> "Истощён"
            EnergyLevel.LOW -> "Низкий"
            EnergyLevel.MODERATE -> "Умеренный"
            EnergyLevel.HIGH -> "Высокий"
            EnergyLevel.VERY_HIGH -> "Очень высокий"
        }
    }

    /**
     * Получает текстовое описание стресса.
     */
    private fun getStressDescription(stress: StressLevel): String {
        return when (stress) {
            StressLevel.NONE -> "Отсутствует"
            StressLevel.LOW -> "Низкий"
            StressLevel.MODERATE -> "Умеренный"
            StressLevel.HIGH -> "Высокий"
            StressLevel.EXTREME -> "Экстремальный"
        }
    }

    /**
     * Генерирует уникальный ID записи.
     */
    private fun generateEntryId(): String {
        return "mood_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
}