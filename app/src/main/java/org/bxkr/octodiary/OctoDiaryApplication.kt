package org.bxkr.octodiary

import android.app.Application
import com.google.android.material.color.DynamicColors

class OctoDiaryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}