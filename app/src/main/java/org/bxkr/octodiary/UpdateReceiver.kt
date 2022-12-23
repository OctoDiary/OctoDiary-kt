package org.bxkr.octodiary

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bxkr.octodiary.Utils.toPatternedDate
import org.bxkr.octodiary.models.userfeed.RecentMark
import org.bxkr.octodiary.models.userfeed.UserFeed
import org.bxkr.octodiary.network.BaseCallback
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.ui.activities.MainActivity
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
        val oldMarks = Gson().fromJson<List<RecentMark>>(
            intent.getStringExtra("old_marks"),
            object : TypeToken<List<RecentMark>>() {}.type
        )

        NetworkService.api(NetworkService.Server.values()[server]).userFeed(
            personId,
            groupId,
            accessToken
        ).enqueue(object : BaseCallback<UserFeed>(context, function = { response ->
            response.body()?.recentMarks!!.forEach {
                if (it !in oldMarks) {
                    val pendingIntent: PendingIntent =
                        Intent(context, MainActivity::class.java).let { notificationIntent ->
                            PendingIntent.getActivity(
                                context, 0, notificationIntent,
                                PendingIntent.FLAG_IMMUTABLE
                            )
                        }

                    var markValue = it.marks[0].value
                    if (it.marks.size == 2) {
                        markValue = context.getString(
                            R.string.fractional_mark,
                            it.marks[0].value,
                            it.marks[1].value
                        )
                    }
                    val notification: Notification = Notification.Builder(context, "data_update")
                        .setContentTitle(context.getText(R.string.notification_new_mark_title))
                        .setContentText(
                            context.getString(
                                R.string.notification_new_mark_text,
                                markValue,
                                it.subject.name,
                                it.shortMarkTypeText,
                                toPatternedDate(
                                    "MMM d",
                                    Date(it.date * 1000L),
                                    context.resources.configuration.locales[0]
                                )
                            )
                        )
                        .setSmallIcon(R.drawable.ic_round_menu_book_24)
                        .setContentIntent(pendingIntent)
                        .setTicker(context.getText(R.string.school_out_of_date))
                        .build()

                    val notificationManager = NotificationManagerCompat.from(context)
                    notificationManager.notify(1, notification)
                }
            }
        }) {})
    }
}