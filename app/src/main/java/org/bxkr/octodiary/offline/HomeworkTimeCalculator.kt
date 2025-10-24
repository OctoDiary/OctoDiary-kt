package org.bxkr.octodiary.offline

/**
 * Калькулятор времени на выполнение домашних заданий.
 * Оценивает время на основе сложности, типа задания и индивидуальных факторов.
 */
object HomeworkTimeCalculator {

    data class HomeworkTask(
        val id: String,
        val subject: String,
        val title: String,
        val description: String? = null,
        val taskType: TaskType,
        val difficulty: Difficulty = Difficulty.MEDIUM,
        val estimatedPages: Int? = null, // для чтения
        val estimatedProblems: Int? = null, // для задач
        val hasExamples: Boolean = false, // есть ли примеры в учебнике
        val isNewTopic: Boolean = false, // новая тема?
        val dueDate: java.time.LocalDate? = null,
        val priority: Priority = Priority.NORMAL
    )

    enum class TaskType {
        READING,        // Чтение текста
        PROBLEMS,       // Решение задач
        WRITING,        // Письменные работы
        MEMORIZATION,   // Заучивание
        RESEARCH,       // Исследование/проект
        PRACTICE,       // Практика/упражнения
        REVIEW,         // Повторение материала
        CREATIVE        // Творческое задание
    }

    enum class Difficulty {
        EASY, MEDIUM, HARD, VERY_HARD
    }

    enum class Priority {
        LOW, NORMAL, HIGH, URGENT
    }

    data class TimeEstimate(
        val baseTime: Int,        // базовое время в минутах
        val adjustedTime: Int,    // скорректированное время
        val breakTime: Int,       // рекомендуемое время перерыва
        val factors: Map<String, Double>, // применённые коэффициенты
        val recommendations: List<String>
    )

    data class UserProfile(
        val averageReadingSpeed: Int = 200, // слов в минуту
        val averageProblemSpeed: Int = 15,  // минут на задачу средней сложности
        val attentionSpan: Int = 45,        // минуты непрерывной работы
        val subjectProficiency: Map<String, Double> = emptyMap(), // предмет -> коэффициент умения (0.5-2.0)
        val fatigueLevel: FatigueLevel = FatigueLevel.NORMAL
    )

    enum class FatigueLevel {
        FRESH, NORMAL, TIRED, VERY_TIRED
    }

    /**
     * Оценивает время на выполнение домашнего задания.
     */
    fun estimateTime(
        task: HomeworkTask,
        userProfile: UserProfile = UserProfile(),
        currentTime: java.time.LocalTime = java.time.LocalTime.now(),
        availableTimeToday: Int? = null // минут доступно сегодня
    ): TimeEstimate {
        val baseTime = calculateBaseTime(task, userProfile)
        val factors = calculateAdjustmentFactors(task, userProfile, currentTime, availableTimeToday)
        val adjustedTime = (baseTime * factors.values.reduce { acc, factor -> acc * factor }).toInt()

        val breakTime = calculateBreakTime(adjustedTime, userProfile.attentionSpan)
        val recommendations = generateRecommendations(task, adjustedTime, factors)

        return TimeEstimate(
            baseTime = baseTime,
            adjustedTime = adjustedTime,
            breakTime = breakTime,
            factors = factors,
            recommendations = recommendations
        )
    }

    /**
     * Вычисляет базовое время на основе типа задания.
     */
    private fun calculateBaseTime(task: HomeworkTask, profile: UserProfile): Int {
        return when (task.taskType) {
            TaskType.READING -> {
                val pages = task.estimatedPages ?: 10
                val wordsPerPage = 300 // предположение
                val totalWords = pages * wordsPerPage
                (totalWords / profile.averageReadingSpeed) * 60 // конвертируем в минуты
            }
            TaskType.PROBLEMS -> {
                val problems = task.estimatedProblems ?: 5
                problems * profile.averageProblemSpeed
            }
            TaskType.WRITING -> {
                when (task.difficulty) {
                    Difficulty.EASY -> 30
                    Difficulty.MEDIUM -> 60
                    Difficulty.HARD -> 120
                    Difficulty.VERY_HARD -> 180
                }
            }
            TaskType.MEMORIZATION -> {
                when (task.difficulty) {
                    Difficulty.EASY -> 20
                    Difficulty.MEDIUM -> 40
                    Difficulty.HARD -> 80
                    Difficulty.VERY_HARD -> 120
                }
            }
            TaskType.RESEARCH -> {
                when (task.difficulty) {
                    Difficulty.EASY -> 45
                    Difficulty.MEDIUM -> 90
                    Difficulty.HARD -> 180
                    Difficulty.VERY_HARD -> 300
                }
            }
            TaskType.PRACTICE -> {
                when (task.difficulty) {
                    Difficulty.EASY -> 25
                    Difficulty.MEDIUM -> 50
                    Difficulty.HARD -> 90
                    Difficulty.VERY_HARD -> 150
                }
            }
            TaskType.REVIEW -> {
                when (task.difficulty) {
                    Difficulty.EASY -> 15
                    Difficulty.MEDIUM -> 30
                    Difficulty.HARD -> 60
                    Difficulty.VERY_HARD -> 90
                }
            }
            TaskType.CREATIVE -> {
                when (task.difficulty) {
                    Difficulty.EASY -> 40
                    Difficulty.MEDIUM -> 80
                    Difficulty.HARD -> 150
                    Difficulty.VERY_HARD -> 240
                }
            }
        }
    }

    /**
     * Вычисляет коэффициенты корректировки времени.
     */
    private fun calculateAdjustmentFactors(
        task: HomeworkTask,
        profile: UserProfile,
        currentTime: java.time.LocalTime,
        availableTimeToday: Int?
    ): Map<String, Double> {
        val factors = mutableMapOf<String, Double>()

        // Коэффициент сложности
        val difficultyFactor = when (task.difficulty) {
            Difficulty.EASY -> 0.8
            Difficulty.MEDIUM -> 1.0
            Difficulty.HARD -> 1.4
            Difficulty.VERY_HARD -> 2.0
        }
        factors["Сложность"] = difficultyFactor

        // Коэффициент владения предметом
        val subjectProficiency = profile.subjectProficiency[task.subject] ?: 1.0
        factors["Владение предметом"] = subjectProficiency

        // Коэффициент новой темы
        if (task.isNewTopic) {
            factors["Новая тема"] = 1.3
        }

        // Коэффициент наличия примеров
        if (task.hasExamples) {
            factors["Примеры в учебнике"] = 0.85
        }

        // Коэффициент усталости
        val fatigueFactor = when (profile.fatigueLevel) {
            FatigueLevel.FRESH -> 0.9
            FatigueLevel.NORMAL -> 1.0
            FatigueLevel.TIRED -> 1.2
            FatigueLevel.VERY_TIRED -> 1.5
        }
        factors["Усталость"] = fatigueFactor

        // Коэффициент времени суток
        val timeFactor = when (currentTime.hour) {
            in 6..8 -> 0.9  // Утро - высокая продуктивность
            in 9..12 -> 1.0  // День
            in 13..15 -> 1.1 // После обеда - снижение
            in 16..18 -> 0.95 // Вечер
            in 19..21 -> 0.9  // Вечер - хорошее время
            else -> 1.2       // Поздний вечер/ночь - снижение
        }
        factors["Время суток"] = timeFactor

        // Коэффициент приоритета
        val priorityFactor = when (task.priority) {
            Priority.LOW -> 1.1    // Можно отложить
            Priority.NORMAL -> 1.0
            Priority.HIGH -> 0.9   // Нужно сделать быстрее
            Priority.URGENT -> 0.8 // Срочно!
        }
        factors["Приоритет"] = priorityFactor

        // Коэффициент дедлайна
        val daysUntilDue = task.dueDate?.let {
            java.time.Duration.between(java.time.LocalDate.now().atStartOfDay(), it.atStartOfDay()).toDays()
        }
        if (daysUntilDue != null) {
            val deadlineFactor = when {
                daysUntilDue > 7 -> 1.1  // Много времени
                daysUntilDue > 3 -> 1.0  // Нормально
                daysUntilDue > 1 -> 0.9  // Скоро
                daysUntilDue == 1L -> 0.8 // Завтра
                daysUntilDue <= 0 -> 0.7  // Сегодня/просрочено
                else -> 1.0
            }
            factors["Дедлайн"] = deadlineFactor
        }

        // Коэффициент доступного времени
        if (availableTimeToday != null && availableTimeToday > 0) {
            val estimatedTime = factors.values.reduce { acc, factor -> acc * factor } * calculateBaseTime(task, profile)
            if (estimatedTime > availableTimeToday) {
                factors["Доступное время"] = availableTimeToday.toDouble() / estimatedTime
            }
        }

        return factors
    }

    /**
     * Вычисляет рекомендуемое время перерыва.
     */
    private fun calculateBreakTime(totalTime: Int, attentionSpan: Int): Int {
        val sessionsNeeded = (totalTime / attentionSpan) + 1
        val breaksNeeded = sessionsNeeded - 1
        return breaksNeeded * 5 // 5 минут перерыва между сессиями
    }

    /**
     * Генерирует рекомендации по выполнению.
     */
    private fun generateRecommendations(
        task: HomeworkTask,
        totalTime: Int,
        factors: Map<String, Double>
    ): List<String> {
        val recommendations = mutableListOf<String>()

        // Рекомендации по времени
        if (totalTime > 120) {
            recommendations.add("Разделите задание на несколько дней по ${totalTime / 3} минут каждый")
        } else if (totalTime > 60) {
            recommendations.add("Сделайте перерыв через ${totalTime / 2} минут работы")
        }

        // Рекомендации по сложности
        if (factors["Сложность"] ?: 1.0 > 1.5) {
            recommendations.add("Это сложное задание - начните пораньше и работайте в тихом месте")
        }

        // Рекомендации по новой теме
        if (task.isNewTopic) {
            recommendations.add("Это новая тема - сначала прочитайте теорию в учебнике")
        }

        // Рекомендации по типу задания
        when (task.taskType) {
            TaskType.READING -> {
                recommendations.add("Для чтения найдите тихое место без отвлечений")
                if (task.estimatedPages ?: 0 > 20) {
                    recommendations.add("Делайте пометки во время чтения для лучшего запоминания")
                }
            }
            TaskType.PROBLEMS -> {
                recommendations.add("Решайте задачи по одной, проверяя каждую")
                if (!task.hasExamples) {
                    recommendations.add("Посмотрите примеры в учебнике перед решением")
                }
            }
            TaskType.MEMORIZATION -> {
                recommendations.add("Повторяйте материал несколько раз с интервалами")
                recommendations.add("Используйте мнемотехнику для лучшего запоминания")
            }
            TaskType.WRITING -> {
                recommendations.add("Составьте план перед написанием")
                recommendations.add("Оставьте время на проверку и исправление")
            }
            TaskType.RESEARCH -> {
                recommendations.add("Соберите все необходимые материалы заранее")
                recommendations.add("Делайте заметки во время исследования")
            }
            TaskType.PRACTICE -> {
                recommendations.add("Повторяйте упражнения до автоматизма")
            }
            TaskType.REVIEW -> {
                recommendations.add("Сосредоточьтесь на ключевых понятиях")
            }
            TaskType.CREATIVE -> {
                recommendations.add("Дайте волю фантазии, но следуйте инструкциям")
            }
        }

        // Рекомендации по усталости
        if (factors["Усталость"] ?: 1.0 > 1.2) {
            recommendations.add("Вы устали - сделайте короткий перерыв или отложите до завтра")
        }

        // Рекомендации по дедлайну
        val daysUntilDue = task.dueDate?.let {
            java.time.Duration.between(java.time.LocalDate.now().atStartOfDay(), it.atStartOfDay()).toDays()
        }
        if (daysUntilDue != null && daysUntilDue <= 1) {
            recommendations.add("Дедлайн близко - сосредоточьтесь на самом важном")
        }

        return recommendations
    }

    /**
     * Создаёт план выполнения нескольких заданий.
     */
    fun createHomeworkPlan(
        tasks: List<HomeworkTask>,
        userProfile: UserProfile = UserProfile(),
        availableTimePerDay: Int = 120 // 2 часа по умолчанию
    ): HomeworkPlan {
        val estimates = tasks.map { task ->
            estimateTime(task, userProfile) to task
        }.sortedBy { (estimate, _) -> estimate.adjustedTime }

        val dailyPlans = mutableListOf<DailyHomeworkPlan>()
        var currentDay = java.time.LocalDate.now()
        var remainingTasks = estimates.toMutableList()

        while (remainingTasks.isNotEmpty()) {
            val dayTasks = mutableListOf<Pair<TimeEstimate, HomeworkTask>>()
            var timeUsed = 0

            val iterator = remainingTasks.iterator()
            while (iterator.hasNext()) {
                val (estimate, task) = iterator.next()
                if (timeUsed + estimate.adjustedTime <= availableTimePerDay) {
                    dayTasks.add(estimate to task)
                    timeUsed += estimate.adjustedTime
                    iterator.remove()
                }
            }

            if (dayTasks.isNotEmpty()) {
                dailyPlans.add(DailyHomeworkPlan(
                    date = currentDay,
                    tasks = dayTasks,
                    totalTime = timeUsed,
                    breaks = (dayTasks.size - 1) * 10 // 10 мин между заданиями
                ))
            }

            currentDay = currentDay.plusDays(1)
        }

        val totalTime = dailyPlans.sumOf { it.totalTime }
        val totalBreaks = dailyPlans.sumOf { it.breaks }

        return HomeworkPlan(
            dailyPlans = dailyPlans,
            totalTime = totalTime,
            totalBreaks = totalBreaks,
            completionDays = dailyPlans.size
        )
    }

    data class DailyHomeworkPlan(
        val date: java.time.LocalDate,
        val tasks: List<Pair<TimeEstimate, HomeworkTask>>,
        val totalTime: Int,
        val breaks: Int
    )

    data class HomeworkPlan(
        val dailyPlans: List<DailyHomeworkPlan>,
        val totalTime: Int,
        val totalBreaks: Int,
        val completionDays: Int
    )

    /**
     * Создаёт шаблон типичного домашнего задания.
     */
    fun createHomeworkTemplate(subject: String, taskType: TaskType, difficulty: Difficulty = Difficulty.MEDIUM): HomeworkTask {
        val title = when (taskType) {
            TaskType.READING -> "Прочитать текст"
            TaskType.PROBLEMS -> "Решить задачи"
            TaskType.WRITING -> "Написать работу"
            TaskType.MEMORIZATION -> "Выучить материал"
            TaskType.RESEARCH -> "Выполнить исследование"
            TaskType.PRACTICE -> "Выполнить упражнения"
            TaskType.REVIEW -> "Повторить материал"
            TaskType.CREATIVE -> "Выполнить творческое задание"
        }

        return HomeworkTask(
            id = generateHomeworkId(),
            subject = subject,
            title = title,
            taskType = taskType,
            difficulty = difficulty
        )
    }

    /**
     * Генерирует уникальный ID домашнего задания.
     */
    private fun generateHomeworkId(): String {
        return "homework_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
}