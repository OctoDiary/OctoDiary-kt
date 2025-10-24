package org.bxkr.octodiary.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.save

class BatteryMonitor(private val context: Context) {
    
    private var receiver: BroadcastReceiver? = null
    private var onBatterySaverChanged: ((Boolean) -> Unit)? = null
    
    fun startMonitoring(callback: (Boolean) -> Unit) {
        onBatterySaverChanged = callback
        
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_BATTERY_CHANGED -> {
                        handleBatteryChange(intent)
                    }
                }
            }
        }
        
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        
        context.registerReceiver(receiver, filter)
    }
    
    fun stopMonitoring() {
        receiver?.let { context.unregisterReceiver(it) }
        receiver = null
    }
    
    private fun handleBatteryChange(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        
        if (level < 0 || scale < 0) return
        
        val batteryPct = (level.toFloat() / scale.toFloat() * 100).toInt()
        
        // Проверяем настройки
        val batterySaverEnabled = context.mainPrefs.get<Boolean>("battery_saver_enabled") ?: true
        val batterySaverThreshold = context.mainPrefs.get<Int>("battery_saver_threshold") ?: 15
        val batterySaverActive = context.mainPrefs.get<Boolean>("battery_saver_active") ?: false
        
        if (!batterySaverEnabled) {
            // Если функция отключена пользователем, снимаем активный режим
            if (batterySaverActive) {
                context.mainPrefs.save("battery_saver_active" to false)
                onBatterySaverChanged?.invoke(false)
            }
            return
        }
        
        // Логика включения/выключения режима экономии
        when {
            batteryPct <= batterySaverThreshold && !batterySaverActive -> {
                // Включаем режим экономии
                context.mainPrefs.save("battery_saver_active" to true)
                onBatterySaverChanged?.invoke(true)
            }
            batteryPct > batterySaverThreshold + 5 && batterySaverActive -> {
                // Выключаем режим экономии (с гистерезисом +5%)
                context.mainPrefs.save("battery_saver_active" to false)
                onBatterySaverChanged?.invoke(false)
            }
        }
    }
    
    companion object {
        fun getCurrentBatteryLevel(context: Context): Int {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        }
        
        fun isCharging(context: Context): Boolean {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
            return status == BatteryManager.BATTERY_STATUS_CHARGING || 
                   status == BatteryManager.BATTERY_STATUS_FULL
        }
        
        fun isBatterySaverActive(context: Context): Boolean {
            return context.mainPrefs.get<Boolean>("battery_saver_active") ?: false
        }
        
        fun shouldReduceAnimations(context: Context): Boolean {
            val active = isBatterySaverActive(context)
            val reduceAnimations = context.mainPrefs.get<Boolean>("battery_saver_reduce_animations") ?: true
            return active && reduceAnimations
        }
        
        fun shouldForceDarkTheme(context: Context): Boolean {
            val active = isBatterySaverActive(context)
            val forceDark = context.mainPrefs.get<Boolean>("battery_saver_force_dark") ?: true
            return active && forceDark
        }
    }
}
