package org.bxkr.octodiary

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bxkr.octodiary.Utils.toPatternedDate
import org.bxkr.octodiary.models.userfeed.RecentMark
import org.bxkr.octodiary.models.userfeed.UserFeed
import org.bxkr.octodiary.network.BaseCallback
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.ui.activities.MarkActivity
import java.util.Date

class UpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val accessToken = intent.getStringExtra("access_token")
        val personId = intent.getLongExtra("person_id", 0L)
        val groupId = intent.getLongExtra("group_id", 0L)
        val server = intent.getIntExtra("server", 0)
        val prefs = context.getSharedPreferences(
            context.getString(R.string.saved_data_key),
            Context.MODE_PRIVATE
        )
        val oldMarks = Gson().fromJson<List<RecentMark>>(
            prefs.getString("old_marks", null),
            object : TypeToken<List<RecentMark>>() {}.type
        )

        NetworkService.api(NetworkService.Server.values()[server]).userFeed(
            personId,
            groupId,
            accessToken
        ).enqueue(object : BaseCallback<UserFeed>(context, function = { response ->
            response.body()?.recentMarks?.forEach { recentMark ->
                if (recentMark.marks[0].id !in oldMarks.map { it.marks[0].id }) {

                    val notificationManager =
                        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    var notificationId = 1
                    if (notificationManager.activeNotifications.isNotEmpty()) {
                        notificationId =
                            notificationManager.activeNotifications.maxOf { it.id } + 1
                    }

                    val pendingIntent: PendingIntent =
                        Intent(context, MarkActivity::class.java).let { notificationIntent ->
                            notificationIntent.action = "notification_$notificationId"
                            notificationIntent.putExtra("person_id", personId)
                            notificationIntent.putExtra("group_id", groupId)
                            notificationIntent.putExtra("mark_id", recentMark.marks[0].id)
                            notificationIntent.putExtra("notification_id", notificationId)
                            PendingIntent.getActivity(
                                context, notificationId, notificationIntent,
                                PendingIntent.FLAG_IMMUTABLE
                            )
                        }

                    var markValue = recentMark.marks[0].value
                    if (recentMark.marks.size == 2) {
                        markValue = context.getString(
                            R.string.fractional_mark,
                            recentMark.marks[0].value,
                            recentMark.marks[1].value
                        )
                    }
                    val notification: Notification = Notification.Builder(context, "data_update")
                        .setContentTitle(context.getText(R.string.notification_new_mark_title))
                        .setContentText(
                            context.getString(
                                R.string.notification_new_mark_text,
                                markValue,
                                recentMark.subject.name,
                                recentMark.shortMarkTypeText,
                                toPatternedDate(
                                    "MMM d",
                                    Date(recentMark.date * 1000L),
                                    context.resources.configuration.locales[0]
                                )
                            )
                        )
                        .setSmallIcon(R.drawable.ic_round_menu_book_24)
                        .setContentIntent(pendingIntent)
                        .setTicker(context.getText(R.string.school_out_of_date))
                        .build()

                    val notificationManagerCompat = NotificationManagerCompat.from(context)
                    notificationManagerCompat.notify(notificationId, notification)

                    prefs.edit {
                        putString("old_marks", Gson().toJson(response.body()?.recentMarks))
                    }
                }
            }
        }) {})
    }
}