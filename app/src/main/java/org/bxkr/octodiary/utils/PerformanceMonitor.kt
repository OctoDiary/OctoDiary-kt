package org.bxkr.octodiary.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*

/**
 * Мониторинг производительности ключевых компонентов приложения
 */
object PerformanceMonitor {

    private const val TAG = "PerformanceMonitor"
    private var isInitialized = false
    private lateinit var context: Context
    private val mainHandler = Handler(Looper.getMainLooper())
    private val performanceMetrics = mutableMapOf<String, MutableList<Long>>()
    private val maxMetricsPerComponent = 100

    fun initialize(context: Context) {
        if (isInitialized) return

        this.context = context.applicationContext
        isInitialized = true

        Log.d(TAG, "Performance Monitor initialized")
    }

    /**
     * Измеряет время выполнения операции
     */
    fun <T> measureTime(component: String, operation: String, block: () -> T): T {
        if (!isInitialized) return block()

        val startTime = System.nanoTime()
        return try {
            block()
        } finally {
            val durationNs = System.nanoTime() - startTime
            val durationMs = durationNs / 1_000_000
            recordMetric("$component:$operation", durationMs)
        }
    }

    /**
     * Асинхронно измеряет время выполнения операции
     */
    suspend fun <T> measureTimeAsync(component: String, operation: String, block: suspend () -> T): T {
        if (!isInitialized) return block()

        val startTime = System.nanoTime()
        return try {
            block()
        } finally {
            val durationNs = System.nanoTime() - startTime
            val durationMs = durationNs / 1_000_000
            recordMetric("$component:$operation", durationMs)
        }
    }

    private fun recordMetric(key: String, durationMs: Long) {
        val metrics = performanceMetrics.getOrPut(key) { mutableListOf() }

        synchronized(metrics) {
            metrics.add(durationMs)
            if (metrics.size > maxMetricsPerComponent) {
                metrics.removeAt(0) // Удаляем самый старый
            }
        }

        // Логируем только если время превышает порог
        if (durationMs > 1000) { // > 1 секунды
            Log.w(TAG, "$key took ${durationMs}ms")
        } else if (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            Log.v(TAG, "$key: ${durationMs}ms")
        }
    }

    /**
     * Получает среднее время выполнения для компонента
     */
    fun getAverageTime(component: String, operation: String): Double {
        val metrics = performanceMetrics["$component:$operation"] ?: return 0.0
        synchronized(metrics) {
            return metrics.average()
        }
    }

    /**
     * Получает статистику производительности
     */
    fun getPerformanceStats(): Map<String, PerformanceStats> {
        return performanceMetrics.mapValues { (_, metrics) ->
            synchronized(metrics) {
                val avg = metrics.average()
                val max = metrics.maxOrNull() ?: 0L
                val min = metrics.minOrNull() ?: 0L
                PerformanceStats(avg, max, min, metrics.size)
            }
        }
    }

    /**
     * Проверяет, не превышает ли время выполнения порог
     */
    fun checkPerformanceThreshold(component: String, operation: String, thresholdMs: Long): Boolean {
        val avgTime = getAverageTime(component, operation)
        return avgTime > thresholdMs
    }

    /**
     * Очищает метрики для компонента
     */
    fun clearMetrics(component: String) {
        performanceMetrics.keys.removeIf { it.startsWith("$component:") }
    }

    /**
     * Composable для мониторинга жизненного цикла компонента
     */
    @Composable
    fun TrackComposablePerformance(componentName: String) {
        if (!isInitialized) return

        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val composer = androidx.compose.runtime.currentComposer
        val startTime = remember { System.nanoTime() }

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        // Сбрасываем время при resume
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        val durationNs = System.nanoTime() - startTime
                        val durationMs = durationNs / 1_000_000
                        recordMetric("$componentName:lifecycle", durationMs)
                    }
                    else -> {}
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                val durationNs = System.nanoTime() - startTime
                val durationMs = durationNs / 1_000_000
                recordMetric("$componentName:total_lifetime", durationMs)
            }
        }
    }

    data class PerformanceStats(
        val averageMs: Double,
        val maxMs: Long,
        val minMs: Long,
        val sampleCount: Int
    )
}

/**
 * Вспомогательная функция для измерения времени выполнения
 */
fun <T> measurePerformance(component: String, operation: String, block: () -> T): T {
    return PerformanceMonitor.measureTime(component, operation, block)
}

/**
 * Вспомогательная suspend функция для измерения времени выполнения
 */
suspend fun <T> measurePerformanceAsync(component: String, operation: String, block: suspend () -> T): T {
    return PerformanceMonitor.measureTimeAsync(component, operation, block)
}