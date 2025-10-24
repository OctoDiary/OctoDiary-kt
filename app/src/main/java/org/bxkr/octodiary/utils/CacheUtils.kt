package org.bxkr.octodiary.utils

import android.app.ActivityManager
import android.content.Context
import android.util.Log

/**
 * Утилиты для управления кэшем и памятью
 */
object CacheUtils {

    private const val TAG = "CacheUtils"

    /**
     * Проверяет, доступно ли достаточно памяти для кэширования
     */
    fun isMemoryAvailableForCache(context: Context, requiredMemoryMB: Int = 50): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val availableMemoryMB = memoryInfo.availMem / (1024 * 1024)
        val isLowMemory = memoryInfo.lowMemory

        Log.d(TAG, "Available memory: ${availableMemoryMB}MB, Low memory: $isLowMemory")

        return !isLowMemory && availableMemoryMB > requiredMemoryMB
    }

    /**
     * Очищает кэш при низком уровне памяти
     */
    fun clearCacheIfLowMemory(context: Context, cacheMap: MutableMap<*, *>) {
        if (!isMemoryAvailableForCache(context, 30)) {
            Log.d(TAG, "Clearing cache due to low memory")
            cacheMap.clear()
        }
    }

    /**
     * Получает рекомендуемый размер кэша на основе доступной памяти
     */
    fun getRecommendedCacheSize(context: Context): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalMemoryMB = memoryInfo.totalMem / (1024 * 1024)

        return when {
            totalMemoryMB > 4096 -> 200 // > 4GB RAM
            totalMemoryMB > 2048 -> 100 // > 2GB RAM
            totalMemoryMB > 1024 -> 50  // > 1GB RAM
            else -> 20 // <= 1GB RAM
        }
    }
}