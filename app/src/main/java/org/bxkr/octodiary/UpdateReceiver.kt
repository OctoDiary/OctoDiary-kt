package org.bxkr.octodiary

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.network.interfaces.MainSchoolAPI
import java.text.SimpleDateFormat
import java.util.Date

private const val TAG = "Notifications"

class UpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        with(context) {
            val subsystem = authPrefs.get<Int>("subsystem")
            val token = authPrefs.get<String>("access_token")
            val studentId =
                notificationPrefs.get<Long>("student_id")
            val date = Date().formatToDay()
            if (
                subsystem != null &&
                token != null &&
                studentId != null
            ) {
                Log.d(TAG, "Listening for new marks...")
                NetworkService.mainSchoolApi(
                    MainSchoolAPI.getBaseUrl(
                        Diary.values()[subsystem]
                    )
                ).markList(
                    accessToken = token,
                    studentId = studentId,
                    fromDate = date,
                    toDate = date
                ).baseEnqueue(
                    errorFunction = { errorBody, httpCode, _ ->
                        errorReceiver(
                            "Error",
                            "$httpCode: ${errorBody.string()}"
                        )
                    },
                    noConnectionFunction = { t, _ ->
                        errorReceiver(
                            "Client error",
                            t.message ?: "NULL_MESSAGE"
                        )
                    }
                ) { response ->
                    Log.d(TAG, "| Got new marks response")
                    val currentMarks = response.payload
                    val pastMarkIds = Gson().fromJson<List<Long>>(
                        notificationPrefs.get<String>("mark_ids"),
                        object : TypeToken<List<Long>>() {}.type
                    ).toMutableList()

                    currentMarks.filter { it.id !in pastMarkIds }.forEach { mark ->
                        mark.run {
                            Log.d(TAG, "| Sent mark id ${mark.id}")
                            sendNotification(
                                value,
                                weight,
                                subjectName,
                                controlFormName,
                                lessonDate
                            )
                        }
                        pastMarkIds.apply {
                            if (size >= 10) {
                                removeAt(0)
                            }
                            add(mark.id)
                        }
                    }

                    notificationPrefs.save(
                        "mark_ids" to Gson().toJson(pastMarkIds)
                    )
                }
            }
        }
    }

    private fun Context.sendNotification(
        value: String,
        weight: Int,
        subjectName: String,
        controlFormName: String,
        lessonDate: String
    ) {
        val totalCount = notificationPrefs.get<Int>("total_count") ?: 0
        val suffix = if (weight != 1) "^$weight" else ""
        val notification: Notification = Notification.Builder(this, "data_update")
            .setContentTitle(getText(R.string.notification_new_mark_title))
            .setContentText(
                getString(
                    R.string.notification_new_mark_text,
                    value + suffix,
                    subjectName,
                    controlFormName,
                    SimpleDateFormat(
                        "dd LLL",
                        resources.configuration.locales[0]
                    ).format(lessonDate.parseFromDay())
                )
            )
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_round_menu_book_24)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    1,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setTicker(getText(R.string.school_out_of_date))
            .build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationPrefs.save("total_count" to totalCount + 1)
        NotificationManagerCompat.from(this).notify(totalCount + 1, notification)
    }

    private fun errorReceiver(type: String, message: String) {
        Log.e(
            TAG,
            "$type occurred! Trace:\n$message"
        )
    }
}