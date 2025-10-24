package org.bxkr.octodiary.offline

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.bxkr.octodiary.MainActivity
import org.bxkr.octodiary.R
import java.time.LocalTime

/**
 * Локальный таймер Pomodoro для эффективного обучения.
 * Реализует технику Pomodoro: 25 минут работы + 5 минут перерыва.
 */
class PomodoroTimer(private val context: Context) {

    data class PomodoroSession(
        val id: String,
        val task: String? = null,
        val workDuration: Int = 25, // минуты
        val breakDuration: Int = 5,  // минуты
        val longBreakDuration: Int = 15, // минуты для длинного перерыва
        val sessionsUntilLongBreak: Int = 4,
        val startTime: java.time.LocalDateTime = java.time.LocalDateTime.now(),
        val completedSessions: Int = 0,
        val isActive: Boolean = false
    )

    data class TimerState(
        val isRunning: Boolean,
        val isWorkSession: Boolean,
        val currentSession: Int,
        val remainingMinutes: Int,
        val remainingSeconds: Int,
        val totalSessions: Int,
        val completedSessions: Int,
        val currentTask: String?
    )

    enum class SessionType {
        WORK, SHORT_BREAK, LONG_BREAK
    }

    data class PomodoroStats(
        val totalSessions: Int,
        val completedSessions: Int,
        val totalWorkTime: Int, // в минутах
        val totalBreakTime: Int, // в минутах
        val averageSessionTime: Double,
        val longestStreak: Int,
        val favoriteWorkTime: LocalTime?,
        val productivityScore: Double // 0-100
    )

    companion object {
        private const val CHANNEL_ID = "pomodoro_timer"
        private const val NOTIFICATION_ID = 2001
        private const val DEFAULT_WORK_DURATION = 25
        private const val DEFAULT_BREAK_DURATION = 5
        private const val DEFAULT_LONG_BREAK_DURATION = 15
    }

    private var currentSession: PomodoroSession? = null
    private var timer: android.os.CountDownTimer? = null
    private var isRunning = false
    private var isWorkSession = true
    private var currentSessionNumber = 0
    private var completedSessions = 0
    private var remainingMinutes = 0
    private var remainingSeconds = 0

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = "Таймер Pomodoro"
        val descriptionText = "Уведомления таймера Pomodoro"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Запускает сессию Pomodoro.
     */
    fun startSession(session: PomodoroSession): TimerState {
        stopTimer()

        currentSession = session
        currentSessionNumber = 1
        completedSessions = session.completedSessions
        isRunning = true
        isWorkSession = true

        startWorkSession()
        return getCurrentState()
    }

    /**
     * Останавливает текущую сессию.
     */
    fun stopSession(): TimerState {
        stopTimer()
        isRunning = false
        return getCurrentState()
    }

    /**
     * Ставит таймер на паузу.
     */
    fun pauseTimer(): TimerState {
        timer?.cancel()
        isRunning = false
        return getCurrentState()
    }

    /**
     * Возобновляет таймер.
     */
    fun resumeTimer(): TimerState {
        if (currentSession == null) return getCurrentState()

        isRunning = true
        val durationMillis = (remainingMinutes * 60L + remainingSeconds) * 1000L

        // startCountdown(durationMillis)
        return getCurrentState()
    }

    /**
     * Пропускает текущую сессию.
     */
    fun skipSession(): TimerState {
        if (isWorkSession) {
            completedSessions++
        }

        determineNextSession()
        return getCurrentState()
    }

    /**
     * Получает текущее состояние таймера.
     */
    fun getCurrentState(): TimerState {
        return TimerState(
            isRunning = isRunning,
            isWorkSession = isWorkSession,
            currentSession = currentSessionNumber,
            remainingMinutes = remainingMinutes,
            remainingSeconds = remainingSeconds,
            totalSessions = currentSession?.sessionsUntilLongBreak ?: 4,
            completedSessions = completedSessions,
            currentTask = currentSession?.task
        )
    }

    /**
     * Запускает рабочую сессию.
     */
    private fun startWorkSession() {
        val duration = currentSession?.workDuration ?: DEFAULT_WORK_DURATION
        startTimer(duration * 60 * 1000L, SessionType.WORK)
        showNotification("Рабочая сессия", "Время сосредоточиться на задаче", duration)
    }

    /**
     * Запускает перерыв.
     */
    private fun startBreakSession() {
        val isLongBreak = currentSessionNumber % (currentSession?.sessionsUntilLongBreak ?: 4) == 0
        val duration = if (isLongBreak) {
            currentSession?.longBreakDuration ?: DEFAULT_LONG_BREAK_DURATION
        } else {
            currentSession?.breakDuration ?: DEFAULT_BREAK_DURATION
        }

        startTimer(duration * 60 * 1000L, if (isLongBreak) SessionType.LONG_BREAK else SessionType.SHORT_BREAK)

        val title = if (isLongBreak) "Длинный перерыв" else "Короткий перерыв"
        showNotification(title, "Время отдохнуть и восстановиться", duration)
    }

    /**
     * Запускает обратный отсчёт.
     */
    private fun startTimer(durationMillis: Long, sessionType: SessionType) {
        timer = object : android.os.CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMinutes = (millisUntilFinished / 60000).toInt()
                remainingSeconds = ((millisUntilFinished % 60000) / 1000).toInt()
            }

            override fun onFinish() {
                handleSessionComplete(sessionType)
            }
        }.start()
    }

    /**
     * Обрабатывает завершение сессии.
     */
    private fun handleSessionComplete(sessionType: SessionType) {
        when (sessionType) {
            SessionType.WORK -> {
                completedSessions++
                showNotification("Сессия завершена!", "Отлично поработал! Время перерыва.", 0)
                playCompletionSound()
            }
            SessionType.SHORT_BREAK, SessionType.LONG_BREAK -> {
                showNotification("Перерыв завершён!", "Время вернуться к работе.", 0)
                playBreakEndSound()
            }
        }

        determineNextSession()
    }

    /**
     * Определяет следующую сессию.
     */
    private fun determineNextSession() {
        if (isWorkSession) {
            isWorkSession = false
            startBreakSession()
        } else {
            isWorkSession = true
            currentSessionNumber++
            if (currentSessionNumber <= (currentSession?.sessionsUntilLongBreak ?: 4)) {
                startWorkSession()
            } else {
                // Сессия Pomodoro завершена
                showNotification("Pomodoro завершён!", "Поздравляем с завершением сессии!", 0)
                stopSession()
            }
        }
    }

    /**
     * Останавливает таймер.
     */
    private fun stopTimer() {
        timer?.cancel()
        timer = null
        cancelNotifications()
    }

    /**
     * Показывает уведомление.
     */
    private fun showNotification(title: String, content: String, duration: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(isRunning)

        if (duration > 0) {
            builder.setContentText("$content Осталось: ${duration} мин")
        }

        // Добавляем действия
        if (isRunning) {
            val pauseIntent = Intent(context, PomodoroNotificationReceiver::class.java).apply {
                action = "PAUSE"
            }
            val pausePendingIntent = PendingIntent.getBroadcast(
                context, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(R.drawable.ic_launcher_foreground, "Пауза", pausePendingIntent)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    /**
     * Отменяет уведомления.
     */
    private fun cancelNotifications() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    /**
     * Воспроизводит звук завершения сессии.
     */
    private fun playCompletionSound() {
        // Реализация звука завершения
        // Можно использовать MediaPlayer или SoundPool
    }

    /**
     * Воспроизводит звук завершения перерыва.
     */
    private fun playBreakEndSound() {
        // Реализация звука завершения перерыва
    }

    /**
     * Создаёт кастомную сессию Pomodoro.
     */
    fun createCustomSession(
        task: String? = null,
        workDuration: Int = DEFAULT_WORK_DURATION,
        breakDuration: Int = DEFAULT_BREAK_DURATION,
        longBreakDuration: Int = DEFAULT_LONG_BREAK_DURATION,
        sessionsUntilLongBreak: Int = 4
    ): PomodoroSession {
        return PomodoroSession(
            id = generateSessionId(),
            task = task,
            workDuration = workDuration,
            breakDuration = breakDuration,
            longBreakDuration = longBreakDuration,
            sessionsUntilLongBreak = sessionsUntilLongBreak
        )
    }

    /**
     * Получает рекомендуемые настройки для разных типов задач.
     */
    fun getRecommendedSettings(taskType: TaskType): PomodoroSession {
        return when (taskType) {
            TaskType.FOCUS_INTENSIVE -> createCustomSession(
                workDuration = 50,
                breakDuration = 10,
                sessionsUntilLongBreak = 2
            )
            TaskType.CREATIVE -> createCustomSession(
                workDuration = 25,
                breakDuration = 5,
                sessionsUntilLongBreak = 4
            )
            TaskType.LEARNING -> createCustomSession(
                workDuration = 25,
                breakDuration = 5,
                sessionsUntilLongBreak = 4
            )
            TaskType.PROGRAMMING -> createCustomSession(
                workDuration = 45,
                breakDuration = 15,
                sessionsUntilLongBreak = 3
            )
            TaskType.WRITING -> createCustomSession(
                workDuration = 25,
                breakDuration = 5,
                sessionsUntilLongBreak = 6
            )
        }
    }

    enum class TaskType {
        FOCUS_INTENSIVE, // Высококонцентрированные задачи
        CREATIVE,        // Творческие задачи
        LEARNING,        // Обучение
        PROGRAMMING,     // Программирование
        WRITING          // Письменные работы
    }

    /**
     * Анализирует продуктивность на основе сессий.
     */
    fun analyzeProductivity(sessions: List<PomodoroSession>): PomodoroStats {
        val totalSessions = sessions.sumOf { it.sessionsUntilLongBreak }
        val completedSessionsTotal = sessions.sumOf { it.completedSessions }
        val totalWorkTime = sessions.sumOf { it.completedSessions * it.workDuration }
        val totalBreakTime = sessions.sumOf { it.completedSessions * (it.breakDuration + it.longBreakDuration) / it.sessionsUntilLongBreak }

        val averageSessionTime = if (completedSessionsTotal > 0) {
            sessions.map { it.completedSessions * it.workDuration }.average()
        } else 0.0

        val longestStreak = calculateLongestStreak(sessions)
        val favoriteWorkTime = findFavoriteWorkTime(sessions)
        val productivityScore = calculateProductivityScore(sessions)

        return PomodoroStats(
            totalSessions = totalSessions,
            completedSessions = completedSessionsTotal,
            totalWorkTime = totalWorkTime,
            totalBreakTime = totalBreakTime,
            averageSessionTime = averageSessionTime,
            longestStreak = longestStreak,
            favoriteWorkTime = favoriteWorkTime,
            productivityScore = productivityScore
        )
    }

    /**
     * Вычисляет самый длинный стрик завершенных сессий.
     */
    private fun calculateLongestStreak(sessions: List<PomodoroSession>): Int {
        if (sessions.isEmpty()) return 0

        var longestStreak = 0
        var currentStreak = 0

        for (session in sessions.sortedBy { it.startTime }) {
            if (session.completedSessions > 0) {
                currentStreak++
                longestStreak = maxOf(longestStreak, currentStreak)
            } else {
                currentStreak = 0
            }
        }

        return longestStreak
    }

    /**
     * Находит предпочитаемое время работы.
     */
    private fun findFavoriteWorkTime(sessions: List<PomodoroSession>): LocalTime? {
        val workTimes = sessions.mapNotNull { session ->
            if (session.completedSessions > 0) session.startTime.toLocalTime() else null
        }

        if (workTimes.isEmpty()) return null

        val hourGroups = workTimes.groupBy { it.hour }
        val mostFrequentHour = hourGroups.maxByOrNull { it.value.size }?.key ?: return null

        val timesInHour = hourGroups[mostFrequentHour] ?: return null
        val averageMinute = timesInHour.map { it.minute }.average().toInt()

        return LocalTime.of(mostFrequentHour, averageMinute)
    }

    /**
     * Вычисляет оценку продуктивности.
     */
    private fun calculateProductivityScore(sessions: List<PomodoroSession>): Double {
        if (sessions.isEmpty()) return 0.0

        val completionRate = sessions.map { session ->
            if (session.sessionsUntilLongBreak > 0) {
                session.completedSessions.toDouble() / session.sessionsUntilLongBreak
            } else 0.0
        }.average()

        val averageWorkTime = sessions.map { it.workDuration }.average()
        val optimalWorkTime = 25.0 // оптимальное время по технике Pomodoro
        val timeEfficiency = 1.0 - (Math.abs(averageWorkTime - optimalWorkTime) / optimalWorkTime)

        // Комбинированная оценка
        return ((completionRate * 0.7) + (timeEfficiency * 0.3)) * 100
    }

    /**
     * Генерирует уникальный ID сессии.
     */
    private fun generateSessionId(): String {
        return "pomodoro_${System.currentTimeMillis()}_${(0..9999).random()}"
    }

    /**
     * Класс для обработки уведомлений (нужен для действий в уведомлениях).
     */
    class PomodoroNotificationReceiver : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "PAUSE" -> {
                    // Обработка паузы через сервис или статический экземпляр
                }
            }
        }
    }
}