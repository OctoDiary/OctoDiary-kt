package org.bxkr.octodiary.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

/**
 * Сервис записи аудио для конспектов
 */
class AudioRecordingService(private val context: Context) {
    private val TAG = "AudioRecordingService"
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var currentFilePath: String? = null
    
    /**
     * Начать запись
     */
    fun startRecording(subjectName: String, lessonTopic: String): String? {
        if (isRecording) {
            Log.w(TAG, "Already recording, cannot start new recording")
            return null
        }

        Log.d(TAG, "Starting audio recording for subject: $subjectName, topic: $lessonTopic")

        return try {
            val fileName = "lecture_${System.currentTimeMillis()}.m4a"
            val file = File(context.getExternalFilesDir(null), "lectures/$fileName")
            file.parentFile?.mkdirs()

            currentFilePath = file.absolutePath
            Log.d(TAG, "Recording file path: $currentFilePath")

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(currentFilePath)

                try {
                    Log.d(TAG, "Preparing MediaRecorder...")
                    prepare()
                    Log.d(TAG, "Starting recording...")
                    start()
                    isRecording = true
                    Log.i(TAG, "Audio recording started successfully")
                } catch (e: IOException) {
                    Log.e(TAG, "Error preparing/starting MediaRecorder", e)
                    return null
                }
            }

            currentFilePath
        } catch (e: Exception) {
            Log.e(TAG, "Error starting audio recording", e)
            null
        }
    }
    
    /**
     * Остановить запись
     */
    fun stopRecording(): String? {
        if (!isRecording) {
            Log.w(TAG, "Not recording, cannot stop recording")
            return null
        }

        Log.d(TAG, "Stopping audio recording")

        return try {
            mediaRecorder?.apply {
                Log.d(TAG, "Stopping MediaRecorder...")
                stop()
                Log.d(TAG, "Releasing MediaRecorder...")
                release()
            }
            mediaRecorder = null
            isRecording = false
            Log.i(TAG, "Audio recording stopped successfully")
            currentFilePath
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio recording", e)
            null
        }
    }
    
    /**
     * Пауза/возобновление (Android 7.0+)
     */
    fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isRecording) {
            Log.d(TAG, "Pausing audio recording")
            try {
                mediaRecorder?.pause()
                Log.i(TAG, "Audio recording paused")
            } catch (e: Exception) {
                Log.e(TAG, "Error pausing audio recording", e)
            }
        } else {
            Log.w(TAG, "Cannot pause recording: API level < N or not recording")
        }
    }

    fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isRecording) {
            Log.d(TAG, "Resuming audio recording")
            try {
                mediaRecorder?.resume()
                Log.i(TAG, "Audio recording resumed")
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming audio recording", e)
            }
        } else {
            Log.w(TAG, "Cannot resume recording: API level < N or not recording")
        }
    }

    fun isCurrentlyRecording(): Boolean {
        val recording = isRecording
        Log.v(TAG, "Recording status check: $recording")
        return recording
    }
    
    /**
     * Отменить запись
     */
    fun cancelRecording() {
        if (!isRecording) {
            Log.w(TAG, "Not recording, cannot cancel recording")
            return
        }

        Log.d(TAG, "Cancelling audio recording")

        try {
            mediaRecorder?.apply {
                Log.d(TAG, "Stopping and releasing MediaRecorder for cancellation...")
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false

            currentFilePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    val deleted = file.delete()
                    Log.d(TAG, "Deleted recording file: $path, success: $deleted")
                }
            }
            currentFilePath = null
            Log.i(TAG, "Audio recording cancelled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling audio recording", e)
        }
    }
}
