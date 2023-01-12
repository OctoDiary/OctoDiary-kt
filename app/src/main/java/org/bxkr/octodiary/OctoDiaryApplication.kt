package org.bxkr.octodiary

import android.app.Application
import androidx.annotation.StyleRes
import com.google.android.material.color.DynamicColors

class OctoDiaryApplication : Application() {
    private var themeRes: Int = 0
    private var themeCallback = ThemeCallback(this)

    @Suppress("unused")
    enum class ThemeChoice(@StyleRes val resId: Int, val preferenceString: String) {
        Dynamic(0, "dynamic"),
        Red(R.style.Theme_Red, "red"),
        Green(R.style.Theme_Green, "green"),
        Blue(R.style.Theme_Blue, "blue"),
        Yellow(R.style.Theme_Yellow, "yellow"),
        Purple(R.style.Theme_Purple, "purple"),
        Cyan(R.style.Theme_Cyan, "cyan"),
    }

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        updateTheme()
    }

    fun updateTheme() {
        unregisterActivityLifecycleCallbacks(themeCallback)
        themeCallback = ThemeCallback(this)
        registerActivityLifecycleCallbacks(themeCallback)
    }
}