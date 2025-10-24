package org.bxkr.octodiary.audio

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.database.AppDatabase
import org.bxkr.octodiary.database.entity.LectureNoteEntity
import org.bxkr.octodiary.models.events.Event
import org.bxkr.octodiary.parseSimpleLongDate
import org.bxkr.octodiary.utils.BatteryMonitor
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * Сервис автоматической записи лекций по расписанию
 */
class AutomaticLectureRecordingService : Service(), CoroutineScope {

    private val TAG = "AutoLectureRecording"
    private val NOTIFICATION_ID = 2001
    private val CHANNEL_ID = "automatic_recording_channel"

    private lateinit var coroutineJob: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    private val audioRecordingService by lazy { AudioRecordingService(this) }
    private val batteryMonitor by lazy { BatteryMonitor(this) }

    private var currentRecording: LectureRecordingSession? = null
    private var isServiceActive = false

    companion object {
        const val ACTION_START_AUTO_RECORDING = "org.bxkr.octodiary.START_AUTO_RECORDING"
        const val ACTION_STOP_AUTO_RECORDING = "org.bxkr.octodiary.STOP_AUTO_RECORDING"
        const val ACTION_CHECK_SCHEDULE = "org.bxkr.octodiary.CHECK_SCHEDULE"

        fun startAutomaticRecording(context: Context) {
            val intent = Intent(context, AutomaticLectureRecordingService::class.java).apply {
                action = ACTION_START_AUTO_RECORDING
            }
            context.startService(intent)
        }

        fun stopAutomaticRecording(context: Context) {
            val intent = Intent(context, AutomaticLectureRecordingService::class.java).apply {
                action = ACTION_STOP_AUTO_RECORDING
            }
            context.startService(intent)
        }

        fun checkSchedule(context: Context) {
            val intent = Intent(context, AutomaticLectureRecordingService::class.java).apply {
                action = ACTION_CHECK_SCHEDULE
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        coroutineJob = Job()
        createNotificationChannel()
        Log.d(TAG, "AutomaticLectureRecordingService created")

        batteryMonitor.startMonitoring { isBatterySaver ->
            handleBatterySaverChange(isBatterySaver)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_AUTO_RECORDING -> startAutomaticRecording()
            ACTION_STOP_AUTO_RECORDING -> stopAutomaticRecording()
            ACTION_CHECK_SCHEDULE -> checkCurrentSchedule()
        }
        return START_STICKY
    }

    private fun startAutomaticRecording() {
        if (isServiceActive) {
            Log.d(TAG, "Service already active")
            return
        }

        isServiceActive = true
        Log.d(TAG, "Starting automatic lecture recording service")

        startForeground(NOTIFICATION_ID, createNotification("Автозапись уроков активна"))

        launch {
            while (isServiceActive) {
                checkCurrentSchedule()
                delay(60000) // Проверяем каждую минуту
            }
        }
    }

    private fun stopAutomaticRecording() {
        Log.d(TAG, "Stopping automatic lecture recording service")
        isServiceActive = false
        stopCurrentRecording()
        batteryMonitor.stopMonitoring()
        stopForeground(true)
        stopSelf()
    }

    private fun checkCurrentSchedule() {
        try {
            if (!isServiceActive) {
                Log.v(TAG, "Service not active, skipping schedule check")
                return
            }

            if (!shouldRecordAutomatically()) {
                Log.v(TAG, "Recording conditions not met, skipping schedule check")
                return
            }

            val currentTime = System.currentTimeMillis()
            val currentLecture = findCurrentLecture(currentTime)

            Log.v(TAG, "Schedule check - Current time: ${java.util.Date(currentTime)}, Current lecture: ${currentLecture?.subjectName}")

            when {
                currentLecture != null && currentRecording == null -> {
                    // Начало урока - начать запись
                    Log.i(TAG, "Detected lesson start: ${currentLecture.subjectName}")
                    startLectureRecording(currentLecture)
                }
                currentLecture == null && currentRecording != null -> {
                    // Конец урока - остановить запись
                    Log.i(TAG, "Detected lesson end, stopping recording")
                    stopCurrentRecording()
                }
                currentLecture != null && currentRecording != null -> {
                    // Продолжаем урок - обновить уведомление
                    Log.v(TAG, "Continuing lesson: ${currentLecture.subjectName}")
                    updateRecordingNotification(currentLecture)
                }
                else -> {
                    Log.v(TAG, "No lecture detected, no active recording")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in checkCurrentSchedule", e)
        }
    }

    private fun findCurrentLecture(currentTime: Long): Event? {
        if (!DataService.hasEventCalendar) {
            Log.d(TAG, "No event calendar available")
            return null
        }

        val calendar = Calendar.getInstance().apply { timeInMillis = currentTime }

        return DataService.eventCalendar.find { event ->
            val eventStart = Calendar.getInstance().apply {
                try {
                    time = event.startAt.parseSimpleLongDate()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse event start time: ${event.startAt}", e)
                    return@find false
                }
            }
            val eventEnd = Calendar.getInstance().apply {
                try {
                    time = event.finishAt.parseSimpleLongDate()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse event end time: ${event.finishAt}", e)
                    return@find false
                }
            }

            calendar.get(Calendar.YEAR) == eventStart.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == eventStart.get(Calendar.DAY_OF_YEAR) &&
            calendar.get(Calendar.HOUR_OF_DAY) == eventStart.get(Calendar.HOUR_OF_DAY) &&
            calendar.get(Calendar.MINUTE) >= eventStart.get(Calendar.MINUTE) &&
            calendar.get(Calendar.MINUTE) < eventEnd.get(Calendar.MINUTE)
        }
    }

    private fun startLectureRecording(event: Event) {
        try {
            Log.d(TAG, "Starting recording for lesson: ${event.subjectName}")

            val topic = event.lessonTheme ?: "Урок"
            val subjectName = event.subjectName ?: "Неизвестный предмет"

            // Проверяем, не идет ли уже запись
            if (audioRecordingService.isCurrentlyRecording()) {
                Log.w(TAG, "Recording already in progress, cannot start new recording")
                return
            }

            val audioPath = audioRecordingService.startRecording(
                subjectName = subjectName,
                lessonTopic = topic
            )

            if (audioPath != null) {
                currentRecording = LectureRecordingSession(
                    event = event,
                    audioPath = audioPath,
                    startTime = System.currentTimeMillis()
                )
                updateRecordingNotification(event)
                Log.i(TAG, "Successfully started recording for $subjectName: $topic (file: $audioPath)")
            } else {
                Log.e(TAG, "Failed to start audio recording for $subjectName - MediaRecorder initialization failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error starting lecture recording", e)
        }
    }

    private fun stopCurrentRecording() {
        try {
            currentRecording?.let { session ->
                Log.d(TAG, "Stopping recording for lesson: ${session.event.subjectName}")

                if (audioRecordingService.isCurrentlyRecording()) {
                    val audioPath = audioRecordingService.stopRecording()
                    if (audioPath != null) {
                        saveLectureNote(session, audioPath)
                        Log.i(TAG, "Recording stopped and saved successfully")
                    } else {
                        Log.e(TAG, "Failed to stop recording - no audio path returned")
                        // Все равно пытаемся сохранить с известным путем
                        saveLectureNote(session, session.audioPath)
                    }
                } else {
                    Log.w(TAG, "Recording was not active when trying to stop")
                }

                currentRecording = null
                updateNotification("Автозапись уроков активна")
            } ?: Log.d(TAG, "No active recording to stop")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping current recording", e)
            currentRecording = null // Сбрасываем состояние при ошибке
        }
    }

    private fun saveLectureNote(session: LectureRecordingSession, audioPath: String) {
        launch {
            try {
                val database = AppDatabase.getDatabase(this@AutomaticLectureRecordingService)
                if (database != null) {
                    // Проверяем, существует ли файл
                    val audioFile = java.io.File(audioPath)
                    if (!audioFile.exists() || audioFile.length() == 0L) {
                        Log.e(TAG, "Audio file does not exist or is empty: $audioPath")
                        return@launch
                    }
 
                    val topic = session.event.lessonTheme ?: "Урок"
                    val roomInfo = session.event.roomName?.let { "Кабинет: $it" } ?: ""
                    val teacherInfo = session.event.authorName?.let { "Учитель: $it" } ?: ""
                    val additionalInfo = listOf(roomInfo, teacherInfo).filter { it.isNotBlank() }.joinToString(", ")
                    val duration = (System.currentTimeMillis() - session.startTime) / 1000 // в секундах
 
                    val note = LectureNoteEntity(
                        subjectName = session.event.subjectName ?: "Неизвестный предмет",
                        topic = topic,
                        date = session.startTime,
                        content = "Автоматическая запись урока (длительность: ${duration}с). ${additionalInfo}".trim(),
                        audioPath = audioPath,
                        isAiGenerated = false,
                        keyPoints = null,
                        generatedQuestions = null,
                        images = null
                    )
 
                    val noteId = database.lectureNoteDao().insertNote(note)
                    Log.i(TAG, "Lecture note saved to database with ID: $noteId, duration: ${duration}s, file: ${audioFile.length()} bytes")
                } else {
                    Log.e(TAG, "Database is not available, cannot save lecture note")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save lecture note", e)
            }
        }
    }

    private fun shouldRecordAutomatically(): Boolean {
        val prefs = getSharedPreferences("ai_prefs", Context.MODE_PRIVATE)
        val autoRecordEnabled = prefs.getBoolean("auto_record_lectures", false)

        if (!autoRecordEnabled) {
            Log.v(TAG, "Auto recording is disabled in settings")
            return false
        }

        // Проверяем режим экономии батареи
        if (BatteryMonitor.isBatterySaverActive(this)) {
            Log.d(TAG, "Battery saver active, skipping automatic recording")
            return false
        }

        // Проверяем уровень заряда
        val batteryLevel = BatteryMonitor.getCurrentBatteryLevel(this)
        val minBatteryLevel = prefs.getInt("auto_record_min_battery", 20) // Настраиваемый минимум

        if (batteryLevel < minBatteryLevel) {
            Log.d(TAG, "Battery level too low: $batteryLevel% (minimum: $minBatteryLevel%)")
            return false
        }

        // Проверяем, подключено ли зарядное устройство
        val isCharging = BatteryMonitor.isCharging(this)
        val requireCharging = prefs.getBoolean("auto_record_require_charging", false)

        if (requireCharging && !isCharging) {
            Log.d(TAG, "Charging required for auto recording but device is not charging")
            return false
        }

        return true
    }

    private fun handleBatterySaverChange(isBatterySaver: Boolean) {
        if (isBatterySaver && currentRecording != null) {
            Log.d(TAG, "Battery saver activated, stopping current recording")
            stopCurrentRecording()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Автозапись уроков",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Уведомления об автоматической записи лекций"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("OctoDiary")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification(text))
    }

    private fun updateRecordingNotification(event: Event) {
        val text = "Запись: ${event.subjectName}"
        updateNotification(text)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineJob.cancel()
        batteryMonitor.stopMonitoring()
        stopCurrentRecording()
        Log.d(TAG, "AutomaticLectureRecordingService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private data class LectureRecordingSession(
        val event: Event,
        val audioPath: String,
        val startTime: Long
    )
}