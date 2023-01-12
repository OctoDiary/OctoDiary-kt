package org.bxkr.octodiary

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.preference.PreferenceManager

class ThemeCallback(val context: Context) : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?
    ) {
        val themeRes =
            OctoDiaryApplication.ThemeChoice.values().first {
                it.preferenceString == PreferenceManager.getDefaultSharedPreferences(context)
                    .getString("theme", "dynamic")
            }.takeIf { it.resId != 0 }?.resId ?: return
        activity.setTheme(themeRes)
    }

    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
