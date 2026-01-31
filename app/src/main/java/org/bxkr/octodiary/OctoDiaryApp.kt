package org.bxkr.octodiary


import android.app.Application
import android.util.Log
import org.bxkr.octodiary.ai.AIManager // Import AIManager

class OctoDiaryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("OctoDiaryApp", "Application onCreate called")
        
        // Инициализация основных компонентов
        try {
            // DataService это object (синглтон), не нужно его инициализировать
            // просто установим subsystem
            DataService.subsystem = Diary.MES

            // Инициализация AIManager
            AIManager.initialize(applicationContext) // Pass application context
        } catch (e: Exception) {
            Log.e("OctoDiaryApp", "Failed to initialize components", e)
        }
    }
    
    companion object {
        const val TAG = "OctoDiaryApp"
    }
}


