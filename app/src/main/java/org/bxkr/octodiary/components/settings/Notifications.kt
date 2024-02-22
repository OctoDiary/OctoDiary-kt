package org.bxkr.octodiary.components.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import org.bxkr.octodiary.LocalActivity
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.SwitchPreference
import org.bxkr.octodiary.get
import org.bxkr.octodiary.notificationPrefs
import org.bxkr.octodiary.save

@Composable
fun Notifications() {
    val activity = LocalActivity.current
    val notifyWithValue = remember {
        mutableStateOf(
            !(activity.notificationPrefs.get<Boolean>("_hide_mark_value") ?: false)
        )
    }
    if (Manifest.permission.POST_NOTIFICATIONS.let {
            activity.checkCallingOrSelfPermission(it)
        } == PackageManager.PERMISSION_GRANTED) {
        SwitchPreference(
            title = stringResource(R.string.show_mark_value_in_notification),
            listenState = notifyWithValue
        ) {
            notifyWithValue.value = it
            activity.notificationPrefs.save("_hide_mark_value" to !it)
        }
    }
}