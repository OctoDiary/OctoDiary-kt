package org.bxkr.octodiary.offline


import androidx.compose.material.icons.Icons
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import androidx.core.app.NotificationCompat
import org.bxkr.octodiary.MainActivity
import org.bxkr.octodiary.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Таймер уроков с уведомлениями.
 * Отслеживает время до конца урока и показывает уведомления.
 */
class LessonTimer(private val context: Context) {

    private var timer: CountDownTimer? = null
    private var isRunning = false
    private var currentLesson: LessonInfo? = null

    data class LessonInfo(
        val subject: String,
        val startTime: LocalTime,
        val endTime: LocalTime,
        val room: String? = null,
        val teacher: String? = null
    )

    data class TimerState(
        val isRunning: Boolean,
        val remainingMinutes: Long,
        val remainingSeconds: Long,
        val currentLesson: LessonInfo?,
        val nextLesson: LessonInfo?
    )

    companion object {
        private const val CHANNEL_ID = "lesson_timer"
        private const val NOTIFICATION_ID = 1001
        private const val WARNING_MINUTES = 5L // Уведомление за 5 минут до конца
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = "Таймер урока"
        val descriptionText = "Уведомления о времени уроков"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Запускает таймер для текущего урока.
     */
    fun startTimer(lesson: LessonInfo, onTick: (TimerState) -> Unit = {}, onFinish: () -> Unit = {}) {
        stopTimer()

        currentLesson = lesson
        isRunning = true

        val durationMillis = calculateDuration(lesson)
        if (durationMillis <= 0) return

        timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000

                val state = TimerState(
                    isRunning = true,
                    remainingMinutes = minutes,
                    remainingSeconds = seconds,
                    currentLesson = lesson,
                    nextLesson = null // Можно добавить логику для следующего урока
                )

                onTick(state)

                // Уведомление за 5 минут до конца
                if (minutes == WARNING_MINUTES && seconds == 0L) {
                    showWarningNotification(lesson, minutes)
                }

                // Уведомление каждые 10 минут
                if (minutes > 0 && minutes % 10 == 0L && seconds == 0L) {
                    showProgressNotification(lesson, minutes)
                }
            }

            override fun onFinish() {
                isRunning = false
                showLessonEndNotification(lesson)
                onFinish()
            }
        }.start()
    }

    /**
     * Останавливает таймер.
     */
    fun stopTimer() {
        timer?.cancel()
        timer = null
        isRunning = false
        currentLesson = null
        cancelNotifications()
    }

    /**
     * Возвращает текущее состояние таймера.
     */
    fun getCurrentState(): TimerState {
        return TimerState(
            isRunning = isRunning,
            remainingMinutes = 0, // Нужно рассчитать остаток
            remainingSeconds = 0,
            currentLesson = currentLesson,
            nextLesson = null
        )
    }

    /**
     * Рассчитывает длительность урока в миллисекундах.
     */
    private fun calculateDuration(lesson: LessonInfo): Long {
        val now = LocalTime.now()
        return if (now.isBefore(lesson.endTime)) {
            java.time.Duration.between(now, lesson.endTime).toMillis()
        } else {
            0
        }
    }

    /**
     * Показывает уведомление с предупреждением.
     */
    private fun showWarningNotification(lesson: LessonInfo, minutesLeft: Long) {
        val title = "Урок скоро закончится!"
        val content = "${lesson.subject} - осталось $minutesLeft мин"

        showNotification(title, content, lesson)
    }

    /**
     * Показывает уведомление о прогрессе.
     */
    private fun showProgressNotification(lesson: LessonInfo, minutesLeft: Long) {
        val title = "Идёт урок"
        val content = "${lesson.subject} - осталось $minutesLeft мин"

        showNotification(title, content, lesson, silent = true)
    }

    /**
     * Показывает уведомление о завершении урока.
     */
    private fun showLessonEndNotification(lesson: LessonInfo) {
        val title = "Урок завершён!"
        val content = "${lesson.subject} закончился"

        showNotification(title, content, lesson)
    }

    /**
     * Универсальный метод для показа уведомлений.
     */
    private fun showNotification(
        title: String,
        content: String,
        lesson: LessonInfo,
        silent: Boolean = false
    ) {
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

        if (!silent) {
            builder.setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
        }

        // Добавляем информацию о кабинете/преподавателе
        val bigText = buildString {
            append(content)
            lesson.room?.let { append("\nКабинет: $it") }
            lesson.teacher?.let { append("\nПреподаватель: $it") }
        }
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText))

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    /**
     * Отменяет все уведомления таймера.
     */
    private fun cancelNotifications() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    /**
     * Форматирует время для отображения.
     */
    fun formatTime(minutes: Long, seconds: Long): String {
        return String.format("%02d:%02d", minutes, seconds)
    }

    /**
     * Рассчитывает время до начала следующего урока.
     */
    fun getTimeToNextLesson(lessons: List<LessonInfo>): Long? {
        val now = LocalTime.now()
        val nextLesson = lessons
            .filter { it.startTime.isAfter(now) }
            .minByOrNull { it.startTime }

        return nextLesson?.let {
            java.time.Duration.between(now, it.startTime).toMinutes()
        }
    }
}


