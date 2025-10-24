package org.bxkr.octodiary.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import org.bxkr.octodiary.MainActivity
import org.bxkr.octodiary.R
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.models.BellSchedule
import org.bxkr.octodiary.models.DefaultBellSchedules
import java.time.Duration
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class BellScheduleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val CHANNEL_ID = "bell_schedule_notifications"
        private const val CHANNEL_NAME = "Расписание звонков"
        
        fun scheduleNotifications(context: Context) {
            val bellScheduleEnabled = context.mainPrefs.get<Boolean>("bell_schedule_enabled") ?: false
            
            if (!bellScheduleEnabled) {
                WorkManager.getInstance(context).cancelAllWorkByTag("bell_schedule")
                return
            }
            
            val workRequest = PeriodicWorkRequestBuilder<BellScheduleWorker>(
                15, TimeUnit.MINUTES
            )
                .addTag("bell_schedule")
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "bell_schedule_notifications",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
    
    override suspend fun doWork(): Result {
        val bellScheduleEnabled = applicationContext.mainPrefs.get<Boolean>("bell_schedule_enabled") ?: false
        
        if (!bellScheduleEnabled) {
            return Result.success()
        }
        
        createNotificationChannel()
        
        val schedule = getSchedule()
        val currentTime = LocalTime.now()
        
        // Находим текущий и следующий урок
        val currentLesson = schedule.find { lesson ->
            currentTime.isAfter(lesson.startTime) && currentTime.isBefore(lesson.endTime)
        }
        
        val nextLesson = schedule.find { lesson ->
            currentTime.isBefore(lesson.startTime)
        }
        
        // Показываем обратный отсчёт для текущего урока
        currentLesson?.let { lesson ->
            val showCountdown = applicationContext.mainPrefs.get<Boolean>("bell_schedule_show_countdown") ?: true
            if (showCountdown) {
                showLessonCountdown(lesson, currentTime, isEndCountdown = true)
            }
        }
        
        // Уведомление перед следующим уроком
        nextLesson?.let { lesson ->
            val notifyBefore = applicationContext.mainPrefs.get<Boolean>("bell_schedule_notify_before") ?: true
            val minutesBefore = applicationContext.mainPrefs.get<Int>("bell_schedule_minutes_before") ?: 5
            
            if (notifyBefore) {
                val timeUntilLesson = Duration.between(currentTime, lesson.startTime).toMinutes()
                
                if (timeUntilLesson in 1..minutesBefore.toLong()) {
                    showLessonNotification(
                        lesson,
                        "Урок начнётся через $timeUntilLesson мин",
                        "Урок ${lesson.lessonNumber}: ${lesson.startTime} - ${lesson.endTime}"
                    )
                }
            }
        }
        
        return Result.success()
    }
    
    private fun getSchedule(): List<BellSchedule> {
        val scheduleType = applicationContext.mainPrefs.get<String>("bell_schedule_type") ?: "standard"
        
        return when (scheduleType) {
            "standard" -> DefaultBellSchedules.STANDARD
            "short" -> DefaultBellSchedules.SHORT
            "second_shift" -> DefaultBellSchedules.SECOND_SHIFT
            else -> DefaultBellSchedules.STANDARD
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Уведомления о начале и конце уроков"
            }
            
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showLessonCountdown(lesson: BellSchedule, currentTime: LocalTime, isEndCountdown: Boolean) {
        val targetTime = if (isEndCountdown) lesson.endTime else lesson.startTime
        val duration = Duration.between(currentTime, targetTime)
        
        if (duration.isNegative || duration.isZero) return
        
        val minutes = duration.toMinutes()
        val seconds = duration.seconds % 60
        
        val title = if (isEndCountdown) {
            "До конца урока ${lesson.lessonNumber}"
        } else {
            "До начала урока ${lesson.lessonNumber}"
        }
        
        val text = if (minutes > 0) {
            "$minutes мин ${seconds} сек"
        } else {
            "$seconds сек"
        }
        
        showOngoingNotification(
            lesson.lessonNumber,
            title,
            text,
            lesson
        )
    }
    
    private fun showLessonNotification(lesson: BellSchedule, title: String, text: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_schedule_24)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1000 + lesson.lessonNumber, notification)
    }
    
    private fun showOngoingNotification(lessonNumber: Int, title: String, text: String, lesson: BellSchedule) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_schedule_24)
            .setContentTitle(title)
            .setContentText(text)
            .setSubText("${lesson.startTime} - ${lesson.endTime}")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
        
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2000 + lessonNumber, notification)
    }
}
