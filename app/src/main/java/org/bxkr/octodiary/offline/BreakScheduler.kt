package org.bxkr.octodiary.offline

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Генератор расписания отдыха и перерывов для поддержания продуктивности.
 * Планирует перерывы, выходные и время восстановления.
 */
object BreakScheduler {

    data class Break(
        val id: String,
        val type: BreakType,
        val title: String,
        val description: String? = null,
        val date: LocalDate,
        val startTime: LocalTime,
        val duration: Int, // минуты
        val endTime: LocalTime = startTime.plusMinutes(duration.toLong()),
        val isCompleted: Boolean = false,
        val category: BreakCategory = BreakCategory.REST,
        val priority: Priority = Priority.NORMAL,
        val tags: List<String> = emptyList(),
        val createdAt: LocalDateTime = LocalDateTime.now()
    )

    enum class BreakType {
        SHORT_BREAK,    // Короткий перерыв (5-15 мин)
        LONG_BREAK,     // Длинный перерыв (15-60 мин)
        LUNCH_BREAK,    // Обеденный перерыв
        DAY_OFF,        // Выходной день
        VACATION,       // Отпуск
        SICK_LEAVE,     // Больничный
        FLEXIBLE_TIME   // Гибкое время
    }

    enum class BreakCategory {
        REST,           // Отдых
        EXERCISE,       // Физическая активность
        SOCIAL,         // Общение
        HOBBY,          // Хобби
        FAMILY,         // Семья
        HEALTH,         // Здоровье
        LEARNING        // Обучение/развитие
    }

    enum class Priority {
        LOW, NORMAL, HIGH, URGENT
    }

    data class BreakSchedule(
        val date: LocalDate,
        val breaks: List<Break>,
        val totalBreakTime: Int,
        val workBlocks: List<WorkBlock>,
        val productivityScore: Double
    )

    data class WorkBlock(
        val startTime: LocalTime,
        val endTime: LocalTime,
        val duration: Int,
        val activity: String,
        val energyRequired: EnergyLevel
    )

    enum class EnergyLevel {
        LOW, MEDIUM, HIGH
    }

    data class WeeklySchedule(
        val weekStart: LocalDate,
        val dailySchedules: Map<LocalDate, BreakSchedule>,
        val totalBreakTime: Int,
        val workLifeBalance: Double, // 0-1, где 1 - идеальный баланс
        val recommendations: List<String>
    )

    /**
     * Создаёт оптимальный график перерывов на день.
     */
    fun createDailyBreakSchedule(
        workStart: LocalTime = LocalTime.of(9, 0),
        workEnd: LocalTime = LocalTime.of(18, 0),
        date: LocalDate = LocalDate.now(),
        userPreferences: BreakPreferences = BreakPreferences()
    ): BreakSchedule {
        val breaks = mutableListOf<Break>()
        var currentTime = workStart

        // Короткие перерывы каждые 90 минут
        val workBlocks = mutableListOf<WorkBlock>()
        var blockStart = workStart

        while (currentTime.isBefore(workEnd)) {
            val blockEnd = minOf(currentTime.plusMinutes(90), workEnd)

            if (blockEnd != workEnd) {
                // Добавляем короткий перерыв
                val breakStart = blockEnd
                val breakDuration = when {
                    userPreferences.preferredBreakDuration > 0 -> userPreferences.preferredBreakDuration
                    else -> 15 // 15 минут по умолчанию
                }

                breaks.add(createBreak(
                    type = BreakType.SHORT_BREAK,
                    title = "Короткий перерыв",
                    date = date,
                    startTime = breakStart,
                    duration = breakDuration,
                    category = BreakCategory.REST
                ))

                currentTime = breakStart.plusMinutes(breakDuration.toLong())
                workBlocks.add(WorkBlock(blockStart, blockEnd, 90, "Работа", EnergyLevel.MEDIUM))
                blockStart = currentTime
            } else {
                workBlocks.add(WorkBlock(blockStart, blockEnd,
                    java.time.Duration.between(blockStart, blockEnd).toMinutes().toInt(),
                    "Работа", EnergyLevel.MEDIUM))
                break
            }
        }

        // Добавляем обеденный перерыв
        val lunchTime = workStart.plusHours(4) // Через 4 часа после начала
        if (lunchTime.isBefore(workEnd) && lunchTime.isAfter(workStart)) {
            breaks.add(createBreak(
                type = BreakType.LUNCH_BREAK,
                title = "Обед",
                date = date,
                startTime = lunchTime,
                duration = 60,
                category = BreakCategory.REST,
                priority = Priority.HIGH
            ))
        }

        val totalBreakTime = breaks.sumOf { it.duration }
        val productivityScore = calculateProductivityScore(workBlocks, breaks)

        return BreakSchedule(
            date = date,
            breaks = breaks.sortedBy { it.startTime },
            totalBreakTime = totalBreakTime,
            workBlocks = workBlocks,
            productivityScore = productivityScore
        )
    }

    data class BreakPreferences(
        val preferredBreakDuration: Int = 15,
        val includeLunchBreak: Boolean = true,
        val maxWorkBlockDuration: Int = 90,
        val preferredBreakCategories: List<BreakCategory> = listOf(BreakCategory.REST),
        val avoidBreaksDuring: List<ClosedRange<LocalTime>> = emptyList()
    )

    /**
     * Создаёт перерыв.
     */
    fun createBreak(
        type: BreakType,
        title: String,
        date: LocalDate,
        startTime: LocalTime,
        duration: Int,
        category: BreakCategory = BreakCategory.REST,
        description: String? = null,
        priority: Priority = Priority.NORMAL,
        tags: List<String> = emptyList()
    ): Break {
        return Break(
            id = generateBreakId(),
            type = type,
            title = title,
            description = description,
            date = date,
            startTime = startTime,
            duration = duration,
            category = category,
            priority = priority,
            tags = tags
        )
    }

    /**
     * Создаёт недельный план отдыха.
     */
    fun createWeeklyBreakPlan(
        weekStart: LocalDate = LocalDate.now().minusDays(LocalDate.now().dayOfWeek.value - 1L),
        workDays: Set<java.time.DayOfWeek> = setOf(
            java.time.DayOfWeek.MONDAY,
            java.time.DayOfWeek.TUESDAY,
            java.time.DayOfWeek.WEDNESDAY,
            java.time.DayOfWeek.THURSDAY,
            java.time.DayOfWeek.FRIDAY
        ),
        preferences: WeeklyPreferences = WeeklyPreferences()
    ): WeeklySchedule {
        val dailySchedules = mutableMapOf<LocalDate, BreakSchedule>()

        for (i in 0..6) {
            val date = weekStart.plusDays(i.toLong())
            val isWorkDay = workDays.contains(date.dayOfWeek)

            val schedule = if (isWorkDay) {
                createDailyBreakSchedule(
                    workStart = preferences.workStartTime,
                    workEnd = preferences.workEndTime,
                    date = date,
                    userPreferences = preferences.breakPreferences
                )
            } else {
                // Выходной день - минимальный план отдыха
                BreakSchedule(
                    date = date,
                    breaks = listOf(createBreak(
                        type = BreakType.DAY_OFF,
                        title = "Выходной день",
                        date = date,
                        startTime = LocalTime.of(0, 0),
                        duration = 1440, // 24 часа
                        category = BreakCategory.REST
                    )),
                    totalBreakTime = 1440,
                    workBlocks = emptyList(),
                    productivityScore = 0.0
                )
            }

            dailySchedules[date] = schedule
        }

        val totalBreakTime = dailySchedules.values.sumOf { it.totalBreakTime }
        val workLifeBalance = calculateWorkLifeBalance(dailySchedules, workDays.size)
        val recommendations = generateWeeklyRecommendations(dailySchedules, workDays.size)

        return WeeklySchedule(
            weekStart = weekStart,
            dailySchedules = dailySchedules,
            totalBreakTime = totalBreakTime,
            workLifeBalance = workLifeBalance,
            recommendations = recommendations
        )
    }

    data class WeeklyPreferences(
        val workStartTime: LocalTime = LocalTime.of(9, 0),
        val workEndTime: LocalTime = LocalTime.of(18, 0),
        val breakPreferences: BreakPreferences = BreakPreferences()
    )

    /**
     * Вычисляет баланс работы и отдыха.
     */
    private fun calculateWorkLifeBalance(dailySchedules: Map<LocalDate, BreakSchedule>, workDaysCount: Int): Double {
        val totalWorkTime = dailySchedules.values.sumOf { schedule ->
            schedule.workBlocks.sumOf { it.duration }
        }

        val totalBreakTime = dailySchedules.values.sumOf { it.totalBreakTime }

        // Идеальный баланс: 8 часов работы + 8 часов отдыха в будни
        val idealWorkTime = workDaysCount * 8 * 60 // 8 часов в минуты
        val idealBreakTime = workDaysCount * 8 * 60 + (7 - workDaysCount) * 24 * 60 // отдых + выходные

        val workRatio = totalWorkTime.toDouble() / idealWorkTime
        val breakRatio = totalBreakTime.toDouble() / idealBreakTime

        // Баланс - среднее между работой и отдыхом, ближе к 1 лучше
        return 1.0 - Math.abs(workRatio - breakRatio)
    }

    /**
     * Вычисляет оценку продуктивности.
     */
    private fun calculateProductivityScore(workBlocks: List<WorkBlock>, breaks: List<Break>): Double {
        if (workBlocks.isEmpty()) return 0.0

        val averageBlockLength = workBlocks.map { it.duration }.average()
        val breaksPerHour = breaks.size.toDouble() / (workBlocks.sumOf { it.duration }.toDouble() / 60)

        // Оптимально: блоки по 90 минут, перерывы каждые 1.5 часа
        val blockScore = 1.0 - Math.abs(averageBlockLength - 90.0) / 90.0
        val breakScore = 1.0 - Math.abs(breaksPerHour - 0.67) / 0.67 // 0.67 перерывов в час

        return (blockScore + breakScore) / 2.0
    }

    /**
     * Генерирует рекомендации для недели.
     */
    private fun generateWeeklyRecommendations(
        dailySchedules: Map<LocalDate, BreakSchedule>,
        workDaysCount: Int
    ): List<String> {
        val recommendations = mutableListOf<String>()

        val averageProductivity = dailySchedules.values.map { it.productivityScore }.average()
        when {
            averageProductivity < 0.5 -> {
                recommendations.add("Продуктивность низкая - попробуйте короче рабочие блоки")
                recommendations.add("Добавьте больше коротких перерывов в течение дня")
            }
            averageProductivity > 0.8 -> {
                recommendations.add("Отличная продуктивность! Продолжайте в том же духе")
            }
        }

        val totalBreakTime = dailySchedules.values.sumOf { it.totalBreakTime }
        val averageDailyBreakTime = totalBreakTime / 7.0

        when {
            averageDailyBreakTime < 300 -> // менее 5 часов отдыха в день
                recommendations.add("Недостаточно времени отдыха - увеличьте перерывы")
            averageDailyBreakTime > 600 -> // более 10 часов отдыха в день
                recommendations.add("Слишком много времени отдыха - попробуйте более интенсивный график")
        }

        if (workDaysCount >= 5) {
            recommendations.add("5+ рабочих дней в неделю - обеспечьте полноценный отдых на выходных")
        }

        return recommendations
    }

    /**
     * Предлагает типы активности для перерыва.
     */
    fun suggestBreakActivities(category: BreakCategory, duration: Int): List<String> {
        return when (category) {
            BreakCategory.REST -> listOf(
                "Прогулка на свежем воздухе",
                "Расслабляющая музыка",
                "Медитация или дыхательные упражнения",
                "Легкая растяжка",
                "Чашка чая/кофе"
            )
            BreakCategory.EXERCISE -> listOf(
                "Быстрая прогулка",
                "Простые упражнения",
                "Йога или растяжка",
                "Прыжки на месте",
                "Отжимания или приседания"
            )
            BreakCategory.SOCIAL -> listOf(
                "Звонок другу",
                "Общение с коллегами",
                "Сообщение близким",
                "Участие в чате",
                "Обед с друзьями"
            )
            BreakCategory.HOBBY -> listOf(
                "Чтение книги",
                "Рисование или скетчинг",
                "Игра на музыкальном инструменте",
                "Кулинария",
                "Разгадывание кроссвордов"
            )
            BreakCategory.FAMILY -> listOf(
                "Время с семьей",
                "Помощь по дому",
                "Игра с детьми",
                "Семейный обед",
                "Совместный отдых"
            )
            BreakCategory.HEALTH -> listOf(
                "Здоровый перекус",
                "Стакан воды",
                "Глазные упражнения",
                "Массаж шеи и плеч",
                "Контроль осанки"
            )
            BreakCategory.LEARNING -> listOf(
                "Изучение нового навыка",
                "Просмотр образовательного видео",
                "Чтение статьи",
                "Изучение языка",
                "Онлайн-курс"
            )
        }.filter { activity ->
            // Фильтруем по длительности
            when (duration) {
                in 5..10 -> activity.length <= 30 // короткие активности
                in 11..30 -> true // любые
                else -> activity.length >= 20 // более содержательные для длинных перерывов
            }
        }
    }

    /**
     * Создаёт план восстановления после переутомления.
     */
    fun createRecoveryPlan(
        fatigueLevel: FatigueLevel,
        availableTime: Int, // минуты доступного времени
        startDate: LocalDate = LocalDate.now()
    ): List<Break> {
        val plan = mutableListOf<Break>()

        when (fatigueLevel) {
            FatigueLevel.LOW -> {
                // Легкое восстановление
                plan.add(createBreak(
                    type = BreakType.SHORT_BREAK,
                    title = "Легкий отдых",
                    date = startDate,
                    startTime = LocalTime.now(),
                    duration = minOf(30, availableTime),
                    category = BreakCategory.REST
                ))
            }
            FatigueLevel.MEDIUM -> {
                // Среднее восстановление
                val sessions = minOf(3, availableTime / 60)
                for (i in 0 until sessions) {
                    plan.add(createBreak(
                        type = BreakType.LONG_BREAK,
                        title = "Восстановление ${i + 1}",
                        date = startDate,
                        startTime = LocalTime.now().plusMinutes(i * 120L),
                        duration = 60,
                        category = BreakCategory.HEALTH
                    ))
                }
            }
            FatigueLevel.HIGH -> {
                // Интенсивное восстановление
                plan.add(createBreak(
                    type = BreakType.DAY_OFF,
                    title = "День восстановления",
                    date = startDate,
                    startTime = LocalTime.of(0, 0),
                    duration = 1440,
                    category = BreakCategory.HEALTH,
                    priority = Priority.URGENT
                ))
            }
        }

        return plan
    }

    enum class FatigueLevel {
        LOW, MEDIUM, HIGH
    }

    /**
     * Анализирует эффективность перерывов.
     */
    fun analyzeBreakEffectiveness(
        breaks: List<Break>,
        energyLevels: List<Pair<LocalDateTime, Int>> // время -> уровень энергии 1-10
    ): BreakAnalysis {
        val breakEnergyChanges = mutableListOf<Double>()

        for (breakItem in breaks) {
            val breakStart = LocalDateTime.of(breakItem.date, breakItem.startTime)
            val breakEnd = breakStart.plusMinutes(breakItem.duration.toLong())

            val beforeBreak = energyLevels.filter { it.first.isBefore(breakStart) }
                .maxByOrNull { it.first }?.second ?: 5

            val afterBreak = energyLevels.filter { it.first.isAfter(breakEnd) }
                .minByOrNull { it.first }?.second ?: 5

            val energyChange = afterBreak - beforeBreak
            breakEnergyChanges.add(energyChange.toDouble())
        }

        val averageEnergyChange = breakEnergyChanges.average().takeIf { it.isFinite() } ?: 0.0
        val positiveBreaks = breakEnergyChanges.count { it > 0 }
        val effectiveness = if (breakEnergyChanges.isNotEmpty()) {
            positiveBreaks.toDouble() / breakEnergyChanges.size
        } else 0.0

        val bestBreakType = breaks.groupBy { it.type }
            .maxByOrNull { (_, typeBreaks) ->
                typeBreaks.size // по количеству (упрощенно)
            }?.key

        return BreakAnalysis(
            averageEnergyChange = averageEnergyChange,
            effectiveness = effectiveness,
            totalBreaks = breaks.size,
            bestBreakType = bestBreakType,
            recommendations = generateBreakRecommendations(effectiveness, averageEnergyChange)
        )
    }

    data class BreakAnalysis(
        val averageEnergyChange: Double,
        val effectiveness: Double,
        val totalBreaks: Int,
        val bestBreakType: BreakType?,
        val recommendations: List<String>
    )

    /**
     * Генерирует рекомендации по перерывам.
     */
    private fun generateBreakRecommendations(effectiveness: Double, averageEnergyChange: Double): List<String> {
        val recommendations = mutableListOf<String>()

        when {
            effectiveness < 0.3 -> {
                recommendations.add("Перерывы малоэффективны - попробуйте другие виды активности")
                recommendations.add("Увеличьте длительность перерывов")
            }
            effectiveness > 0.7 -> {
                recommendations.add("Перерывы эффективны - продолжайте в том же духе")
            }
        }

        when {
            averageEnergyChange < -1 -> {
                recommendations.add("Перерывы снижают энергию - выбирайте более бодрящие активности")
            }
            averageEnergyChange > 1 -> {
                recommendations.add("Перерывы хорошо восстанавливают энергию")
            }
        }

        return recommendations
    }

    /**
     * Экспортирует план перерывов.
     */
    fun exportBreakSchedule(schedule: BreakSchedule): String {
        return buildString {
            appendLine("Расписание перерывов на ${schedule.date}")
            appendLine("Общее время перерывов: ${schedule.totalBreakTime} мин")
            appendLine("Оценка продуктивности: ${String.format("%.1f", schedule.productivityScore * 100)}%")
            appendLine()

            appendLine("Перерывы:")
            schedule.breaks.forEach { breakItem ->
                appendLine("${breakItem.startTime}-${breakItem.endTime}: ${breakItem.title}")
                appendLine("  Тип: ${getBreakTypeName(breakItem.type)}")
                appendLine("  Категория: ${getBreakCategoryName(breakItem.category)}")
                appendLine("  Длительность: ${breakItem.duration} мин")
                if (breakItem.description != null) {
                    appendLine("  Описание: ${breakItem.description}")
                }
                appendLine()
            }

            appendLine("Рабочие блоки:")
            schedule.workBlocks.forEach { block ->
                appendLine("${block.startTime}-${block.endTime}: ${block.activity}")
                appendLine("  Длительность: ${block.duration} мин")
                appendLine("  Требуемый уровень энергии: ${getEnergyLevelName(block.energyRequired)}")
                appendLine()
            }
        }
    }

    /**
     * Получает название типа перерыва.
     */
    private fun getBreakTypeName(type: BreakType): String {
        return when (type) {
            BreakType.SHORT_BREAK -> "Короткий перерыв"
            BreakType.LONG_BREAK -> "Длинный перерыв"
            BreakType.LUNCH_BREAK -> "Обед"
            BreakType.DAY_OFF -> "Выходной"
            BreakType.VACATION -> "Отпуск"
            BreakType.SICK_LEAVE -> "Больничный"
            BreakType.FLEXIBLE_TIME -> "Гибкое время"
        }
    }

    /**
     * Получает название категории перерыва.
     */
    private fun getBreakCategoryName(category: BreakCategory): String {
        return when (category) {
            BreakCategory.REST -> "Отдых"
            BreakCategory.EXERCISE -> "Физическая активность"
            BreakCategory.SOCIAL -> "Общение"
            BreakCategory.HOBBY -> "Хобби"
            BreakCategory.FAMILY -> "Семья"
            BreakCategory.HEALTH -> "Здоровье"
            BreakCategory.LEARNING -> "Обучение"
        }
    }

    /**
     * Получает название уровня энергии.
     */
    private fun getEnergyLevelName(level: EnergyLevel): String {
        return when (level) {
            EnergyLevel.LOW -> "Низкий"
            EnergyLevel.MEDIUM -> "Средний"
            EnergyLevel.HIGH -> "Высокий"
        }
    }

    /**
     * Генерирует уникальный ID перерыва.
     */
    private fun generateBreakId(): String {
        return "break_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
}