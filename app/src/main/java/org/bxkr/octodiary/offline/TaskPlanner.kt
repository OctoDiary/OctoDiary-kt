package org.bxkr.octodiary.offline

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Локальный планировщик задач без связи с домашними заданиями.
 * Управляет персональными задачами пользователя.
 */
object TaskPlanner {

    data class Task(
        val id: String,
        val title: String,
        val description: String? = null,
        val priority: Priority = Priority.MEDIUM,
        val category: String = "Общее",
        val dueDate: LocalDate? = null,
        val dueTime: LocalTime? = null,
        val estimatedDuration: Int? = null, // в минутах
        val isCompleted: Boolean = false,
        val createdAt: LocalDateTime = LocalDateTime.now(),
        val completedAt: LocalDateTime? = null,
        val tags: List<String> = emptyList(),
        val reminderEnabled: Boolean = false,
        val reminderTime: LocalDateTime? = null
    )

    enum class Priority {
        LOW, MEDIUM, HIGH, URGENT
    }

    data class TaskStats(
        val totalTasks: Int,
        val completedTasks: Int,
        val pendingTasks: Int,
        val overdueTasks: Int,
        val completionRate: Double,
        val averageCompletionTime: Double? // в днях
    )

    /**
     * Создаёт новую задачу.
     */
    fun createTask(
        title: String,
        description: String? = null,
        priority: Priority = Priority.MEDIUM,
        category: String = "Общее",
        dueDate: LocalDate? = null,
        dueTime: LocalTime? = null,
        estimatedDuration: Int? = null,
        tags: List<String> = emptyList(),
        reminderEnabled: Boolean = false,
        reminderTime: LocalDateTime? = null
    ): Task {
        val id = generateTaskId()
        return Task(
            id = id,
            title = title,
            description = description,
            priority = priority,
            category = category,
            dueDate = dueDate,
            dueTime = dueTime,
            estimatedDuration = estimatedDuration,
            tags = tags,
            reminderEnabled = reminderEnabled,
            reminderTime = reminderTime
        )
    }

    /**
     * Генерирует план задач на день.
     */
    fun generateDailyPlan(tasks: List<Task>, workStart: LocalTime = LocalTime.of(9, 0), workEnd: LocalTime = LocalTime.of(18, 0)): DailyPlan {
        val todayTasks = tasks.filter { it.dueDate == LocalDate.now() && !it.isCompleted }
        val sortedTasks = todayTasks.sortedBy { it.priority.ordinal }

        val scheduledTasks = mutableListOf<ScheduledTask>()
        var currentTime = workStart

        for (task in sortedTasks) {
            val duration = task.estimatedDuration ?: 60 // 1 час по умолчанию
            val endTime = currentTime.plusMinutes(duration.toLong())

            if (endTime.isAfter(workEnd)) break // Не планируем за пределами рабочего дня

            scheduledTasks.add(ScheduledTask(task, currentTime, endTime))
            currentTime = endTime.plusMinutes(15) // 15 минут перерыва
        }

        return DailyPlan(LocalDate.now(), scheduledTasks)
    }

    data class ScheduledTask(
        val task: Task,
        val startTime: LocalTime,
        val endTime: LocalTime
    )

    data class DailyPlan(
        val date: LocalDate,
        val tasks: List<ScheduledTask>
    )

    /**
     * Анализирует продуктивность за период.
     */
    fun analyzeProductivity(tasks: List<Task>, startDate: LocalDate, endDate: LocalDate): ProductivityReport {
        val periodTasks = tasks.filter { it.createdAt.toLocalDate().isAfter(startDate.minusDays(1)) && it.createdAt.toLocalDate().isBefore(endDate.plusDays(1)) }
        val completedTasks = periodTasks.filter { it.isCompleted }
        val onTimeTasks = completedTasks.filter { task ->
            task.completedAt?.let { completed ->
                task.dueDate?.let { due ->
                    completed.toLocalDate().isBefore(due) || completed.toLocalDate().isEqual(due)
                } ?: true
            } ?: false
        }

        val completionRate = if (periodTasks.isNotEmpty()) completedTasks.size.toDouble() / periodTasks.size else 0.0
        val onTimeRate = if (completedTasks.isNotEmpty()) onTimeTasks.size.toDouble() / completedTasks.size else 0.0

        val averageCompletionTime = completedTasks.mapNotNull { task ->
            task.completedAt?.let { completed ->
                task.createdAt.let { created ->
                    java.time.Duration.between(created, completed).toDays().toDouble()
                }
            }
        }.average().takeIf { it.isFinite() }

        return ProductivityReport(
            period = startDate..endDate,
            totalTasks = periodTasks.size,
            completedTasks = completedTasks.size,
            onTimeCompletionRate = onTimeRate,
            overallCompletionRate = completionRate,
            averageCompletionTime = averageCompletionTime
        )
    }

    data class ProductivityReport(
        val period: ClosedRange<LocalDate>,
        val totalTasks: Int,
        val completedTasks: Int,
        val onTimeCompletionRate: Double,
        val overallCompletionRate: Double,
        val averageCompletionTime: Double?
    )

    /**
     * Находит просроченные задачи.
     */
    fun getOverdueTasks(tasks: List<Task>): List<Task> {
        val now = LocalDateTime.now()
        return tasks.filter { task ->
            !task.isCompleted && task.dueDate != null && task.dueTime != null &&
            LocalDateTime.of(task.dueDate, task.dueTime).isBefore(now)
        }
    }

    /**
     * Фильтрует задачи по критериям.
     */
    fun filterTasks(
        tasks: List<Task>,
        category: String? = null,
        priority: Priority? = null,
        completed: Boolean? = null,
        tags: List<String>? = null,
        dateRange: ClosedRange<LocalDate>? = null
    ): List<Task> {
        return tasks.filter { task ->
            (category == null || task.category == category) &&
            (priority == null || task.priority == priority) &&
            (completed == null || task.isCompleted == completed) &&
            (tags == null || tags.any { task.tags.contains(it) }) &&
            (dateRange == null || task.dueDate?.let { it in dateRange } ?: false)
        }
    }

    /**
     * Группирует задачи по категориям.
     */
    fun groupTasksByCategory(tasks: List<Task>): Map<String, List<Task>> {
        return tasks.groupBy { it.category }
    }

    /**
     * Группирует задачи по приоритету.
     */
    fun groupTasksByPriority(tasks: List<Task>): Map<Priority, List<Task>> {
        return tasks.groupBy { it.priority }
    }

    /**
     * Вычисляет статистику задач.
     */
    fun calculateStats(tasks: List<Task>): TaskStats {
        val totalTasks = tasks.size
        val completedTasks = tasks.count { it.isCompleted }
        val pendingTasks = totalTasks - completedTasks
        val overdueTasks = getOverdueTasks(tasks).size
        val completionRate = if (totalTasks > 0) completedTasks.toDouble() / totalTasks else 0.0

        val completionTimes = tasks.filter { it.isCompleted }.mapNotNull { task ->
            task.completedAt?.let { completed ->
                task.createdAt.let { created ->
                    java.time.Duration.between(created, completed).toDays().toDouble()
                }
            }
        }
        val averageCompletionTime = completionTimes.average().takeIf { it.isFinite() }

        return TaskStats(
            totalTasks = totalTasks,
            completedTasks = completedTasks,
            pendingTasks = pendingTasks,
            overdueTasks = overdueTasks,
            completionRate = completionRate,
            averageCompletionTime = averageCompletionTime
        )
    }

    /**
     * Генерирует уникальный ID для задачи.
     */
    private fun generateTaskId(): String {
        return "task_${System.currentTimeMillis()}_${(0..9999).random()}"
    }

    /**
     * Предлагает время для выполнения задачи.
     */
    fun suggestOptimalTime(task: Task, existingSchedule: List<ScheduledTask>): LocalTime? {
        // Простая логика: найти ближайший свободный слот
        val workStart = LocalTime.of(9, 0)
        val workEnd = LocalTime.of(18, 0)
        val duration = task.estimatedDuration ?: 60

        val occupiedSlots = existingSchedule.map { it.startTime..it.endTime }

        var currentTime = workStart
        while (currentTime.plusMinutes(duration.toLong()).isBefore(workEnd)) {
            val proposedEnd = currentTime.plusMinutes(duration.toLong())
            val isFree = occupiedSlots.none { slot ->
                currentTime in slot || proposedEnd in slot || (currentTime <= slot.start && proposedEnd >= slot.endInclusive)
            }

            if (isFree) return currentTime

            currentTime = currentTime.plusMinutes(30) // Проверяем каждые 30 минут
        }

        return null // Нет свободного времени
    }
}