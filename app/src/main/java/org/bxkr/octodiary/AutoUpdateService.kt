package org.bxkr.octodiary


import androidx.compose.material.icons.Icons
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.content.edit
import org.bxkr.octodiary.utils.BatteryMonitor

class AutoUpdateService : Service() {

    private val handler = Handler(Looper.getMainLooper())

    // Функция для определения типа сетевого подключения
    private fun isOnWiFi(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private val updateRunnable = object : Runnable {
            override fun run() {
                 // Проверяем условия перед обновлением
                 val authPrefs = AuthPrefs(applicationContext)

                 // Проверки энергоэффективности
                 val batteryLevel = BatteryMonitor.getCurrentBatteryLevel(applicationContext)
                 val isCharging = BatteryMonitor.isCharging(applicationContext)
                 val isBatterySaverActive = BatteryMonitor.isBatterySaverActive(applicationContext)
                 val isOnWiFi = isOnWiFi(applicationContext)

                 // Используем значения по умолчанию, так как нет доступа к MainActivity
                 val minBatteryLevel = 20 // по умолчанию 20%
                 val wifiOnly = true // по умолчанию true
                 val chargingOnly = false // по умолчанию false

                 val batteryOk = batteryLevel >= minBatteryLevel || isCharging || isBatterySaverActive
                 val networkOk = !wifiOnly || isOnWiFi
                 val chargingOk = !chargingOnly || isCharging

                 if (authPrefs.get<Boolean>("auth") == true &&
                     authPrefs.get<String>("access_token") != null &&
                     DataService.loadedEverything.value &&
                     batteryOk && networkOk && chargingOk) {

                     // Обновляем данные в фоне без индикатора загрузки
                     try {
                         DataService.updateAll(applicationContext, silent = true)
                         Log.d("AutoUpdateService", "Data updated automatically")
                     } catch (e: Exception) {
                         Log.e("AutoUpdateService", "Error during auto update", e)
                     }
                 } else {
                     val skipReason = when {
                         !batteryOk -> "low battery ($batteryLevel% < $minBatteryLevel%), not charging, or battery saver active"
                         !networkOk -> "not on Wi-Fi (Wi-Fi required)"
                         !chargingOk -> "not charging (charging required)"
                         else -> "conditions not met or disabled"
                     }
                     Log.d("AutoUpdateService", "Skipping auto update - $skipReason")
                 }

            // Планируем следующее обновление через 30 секунд
            handler.postDelayed(this, 30000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Начинаем автоматическое обновление
        handler.post(updateRunnable)
        Log.d("AutoUpdateService", "Auto update service started")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Останавливаем обновления
        handler.removeCallbacks(updateRunnable)
        Log.d("AutoUpdateService", "Auto update service stopped")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}


