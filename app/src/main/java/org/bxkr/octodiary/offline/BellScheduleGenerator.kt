package org.bxkr.octodiary.offline


import androidx.compose.material.icons.Icons
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Генератор кастомных расписаний звонков.
 * Создаёт и оптимизирует расписания уроков для школ.
 */
object BellScheduleGenerator {

    data class LessonPeriod(
        val lessonNumber: Int,
        val startTime: LocalTime,
        val endTime: LocalTime,
        val duration: Int, // в минутах
        val breakAfter: Int = 0 // перерыв после урока в минутах
    )

    data class BellSchedule(
        val name: String,
        val description: String? = null,
        val periods: List<LessonPeriod>,
        val totalDuration: Int, // общая длительность в минутах
        val applicableDays: Set<DayOfWeek> = setOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        )
    )

    /**
     * Создаёт стандартное расписание звонков (45 мин урок + 10 мин перерыв).
     */
    fun createStandardSchedule(name: String = "Стандартное расписание"): BellSchedule {
        val periods = mutableListOf<LessonPeriod>()
        var currentTime = LocalTime.of(8, 30) // Начало в 8:30

        for (lessonNum in 1..7) {
            val startTime = currentTime
            val endTime = startTime.plusMinutes(45)
            val breakAfter = if (lessonNum < 7) 10 else 0

            periods.add(LessonPeriod(lessonNum, startTime, endTime, 45, breakAfter))
            currentTime = endTime.plusMinutes(breakAfter.toLong())
        }

        val totalDuration = periods.sumOf { it.duration + it.breakAfter }

        return BellSchedule(
            name = name,
            description = "Стандартное расписание: 45 мин урок + 10 мин перерыв",
            periods = periods,
            totalDuration = totalDuration
        )
    }

    /**
     * Создаёт ускоренное расписание (40 мин урок + 5 мин перерыв).
     */
    fun createAcceleratedSchedule(name: String = "Ускоренное расписание"): BellSchedule {
        val periods = mutableListOf<LessonPeriod>()
        var currentTime = LocalTime.of(8, 30)

        for (lessonNum in 1..8) {
            val startTime = currentTime
            val endTime = startTime.plusMinutes(40)
            val breakAfter = if (lessonNum < 8) 5 else 0

            periods.add(LessonPeriod(lessonNum, startTime, endTime, 40, breakAfter))
            currentTime = endTime.plusMinutes(breakAfter.toLong())
        }

        val totalDuration = periods.sumOf { it.duration + it.breakAfter }

        return BellSchedule(
            name = name,
            description = "Ускоренное расписание: 40 мин урок + 5 мин перерыв",
            periods = periods,
            totalDuration = totalDuration
        )
    }

    /**
     * Создаёт кастомное расписание с заданными параметрами.
     */
    fun createCustomSchedule(
        name: String,
        lessonDuration: Int = 45,
        breakDuration: Int = 10,
        startTime: LocalTime = LocalTime.of(8, 30),
        lessonCount: Int = 6,
        bigBreakAfter: Int = 3, // Большой перерыв после 3-го урока
        bigBreakDuration: Int = 20
    ): BellSchedule {
        val periods = mutableListOf<LessonPeriod>()
        var currentTime = startTime

        for (lessonNum in 1..lessonCount) {
            val start = currentTime
            val end = start.plusMinutes(lessonDuration.toLong())

            val breakAfter = when {
                lessonNum == lessonCount -> 0 // Последний урок
                lessonNum == bigBreakAfter -> bigBreakDuration // Большой перерыв
                else -> breakDuration
            }

            periods.add(LessonPeriod(lessonNum, start, end, lessonDuration, breakAfter))
            currentTime = end.plusMinutes(breakAfter.toLong())
        }

        val totalDuration = periods.sumOf { it.duration + it.breakAfter }

        return BellSchedule(
            name = name,
            description = "Кастомное расписание: ${lessonDuration} мин урок, ${breakDuration} мин перерыв, большой перерыв ${bigBreakDuration} мин после ${bigBreakAfter}-го урока",
            periods = periods,
            totalDuration = totalDuration
        )
    }

    /**
     * Оптимизирует расписание для минимизации усталости.
     * Добавляет более длинные перерывы в середине дня.
     */
    fun createOptimizedSchedule(
        name: String = "Оптимизированное расписание",
        lessonCount: Int = 6
    ): BellSchedule {
        val periods = mutableListOf<LessonPeriod>()
        var currentTime = LocalTime.of(8, 30)

        for (lessonNum in 1..lessonCount) {
            val lessonDuration = when (lessonNum) {
                1, 2 -> 45 // Первые уроки стандартные
                3, 4 -> 40 // Средние короче
                else -> 35 // Последние еще короче
            }

            val breakAfter = when (lessonNum) {
                lessonCount -> 0
                2 -> 15 // Длинный перерыв после 2-го урока
                4 -> 20 // Еще длиннее после 4-го
                else -> 10
            }

            val start = currentTime
            val end = start.plusMinutes(lessonDuration.toLong())

            periods.add(LessonPeriod(lessonNum, start, end, lessonDuration, breakAfter))
            currentTime = end.plusMinutes(breakAfter.toLong())
        }

        val totalDuration = periods.sumOf { it.duration + it.breakAfter }

        return BellSchedule(
            name = name,
            description = "Оптимизированное расписание: адаптивная длительность уроков и перерывы",
            periods = periods,
            totalDuration = totalDuration
        )
    }

    /**
     * Создаёт расписание для субботы (короткие уроки).
     */
    fun createSaturdaySchedule(name: String = "Субботнее расписание"): BellSchedule {
        val periods = mutableListOf<LessonPeriod>()
        var currentTime = LocalTime.of(9, 0)

        for (lessonNum in 1..4) {
            val startTime = currentTime
            val endTime = startTime.plusMinutes(35) // Короткие уроки
            val breakAfter = if (lessonNum < 4) 15 else 0 // Длинные перерывы

            periods.add(LessonPeriod(lessonNum, startTime, endTime, 35, breakAfter))
            currentTime = endTime.plusMinutes(breakAfter.toLong())
        }

        val totalDuration = periods.sumOf { it.duration + it.breakAfter }

        return BellSchedule(
            name = name,
            description = "Субботнее расписание: 35 мин урок + 15 мин перерыв",
            periods = periods,
            totalDuration = totalDuration,
            applicableDays = setOf(DayOfWeek.SATURDAY)
        )
    }

    /**
     * Валидирует расписание на корректность.
     */
    fun validateSchedule(schedule: BellSchedule): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Проверка на пересечение уроков
        for (i in 0 until schedule.periods.size - 1) {
            val current = schedule.periods[i]
            val next = schedule.periods[i + 1]

            if (current.endTime.plusMinutes(current.breakAfter.toLong()) != next.startTime) {
                errors.add("Пересечение между уроками ${current.lessonNumber} и ${next.lessonNumber}")
            }
        }

        // Проверка длительности уроков
        schedule.periods.forEach { period ->
            if (period.duration < 20) {
                errors.add("Урок ${period.lessonNumber}: длительность слишком мала (${period.duration} мин)")
            }
            if (period.duration > 90) {
                warnings.add("Урок ${period.lessonNumber}: длительность очень большая (${period.duration} мин)")
            }
        }

        // Проверка перерывов
        schedule.periods.forEach { period ->
            if (period.breakAfter < 0) {
                errors.add("Урок ${period.lessonNumber}: отрицательный перерыв")
            }
            if (period.breakAfter > 60) {
                warnings.add("Урок ${period.lessonNumber}: очень длинный перерыв (${period.breakAfter} мин)")
            }
        }

        // Проверка общего времени
        if (schedule.totalDuration > 8 * 60) { // 8 часов
            warnings.add("Общая длительность расписания очень большая (${schedule.totalDuration} мин)")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>,
        val warnings: List<String>
    )

    /**
     * Экспортирует расписание в читаемый формат.
     */
    fun exportSchedule(schedule: BellSchedule): String {
        return buildString {
            appendLine("Расписание звонков: ${schedule.name}")
            schedule.description?.let { appendLine(it) }
            appendLine("Общая длительность: ${schedule.totalDuration} мин")
            appendLine("Применимо к дням: ${schedule.applicableDays.joinToString { it.name.lowercase() }}")
            appendLine()
            appendLine("Уроки:")
            schedule.periods.forEach { period ->
                appendLine("Урок ${period.lessonNumber}: ${period.startTime} - ${period.endTime} (${period.duration} мин)")
                if (period.breakAfter > 0) {
                    appendLine("  Перерыв: ${period.breakAfter} мин")
                }
            }
        }
    }

    /**
     * Предлагает оптимальное время начала уроков.
     */
    fun suggestStartTime(lessonCount: Int, preferredEndTime: LocalTime? = null): LocalTime {
        val defaultStart = LocalTime.of(8, 30)
        if (preferredEndTime == null) return defaultStart

        // Предполагаем 45 мин урок + 10 мин перерыв
        val totalTimePerLesson = 55 // 45 + 10
        val totalTime = lessonCount * 45 + (lessonCount - 1) * 10

        val suggestedStart = preferredEndTime.minusMinutes(totalTime.toLong())
        return if (suggestedStart.isBefore(LocalTime.of(7, 0))) LocalTime.of(8, 0) else suggestedStart
    }

    /**
     * Создаёт расписание на основе возраста учеников.
     */
    fun createAgeAppropriateSchedule(ageGroup: AgeGroup, name: String = "Расписание для ${ageGroup.description}"): BellSchedule {
        return when (ageGroup) {
            AgeGroup.PRIMARY -> createCustomSchedule(
                name = name,
                lessonDuration = 35,
                breakDuration = 15,
                startTime = LocalTime.of(8, 30),
                lessonCount = 4
            )
            AgeGroup.MIDDLE -> createCustomSchedule(
                name = name,
                lessonDuration = 40,
                breakDuration = 10,
                startTime = LocalTime.of(8, 30),
                lessonCount = 6
            )
            AgeGroup.HIGH -> createStandardSchedule(name)
        }
    }

    enum class AgeGroup(val description: String) {
        PRIMARY("начальной школы"),
        MIDDLE("средней школы"),
        HIGH("старшей школы")
    }
}


