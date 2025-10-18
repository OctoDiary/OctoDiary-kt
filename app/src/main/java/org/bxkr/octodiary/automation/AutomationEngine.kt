package org.bxkr.octodiary.automation

import android.content.Context
import android.util.Log
import android.webkit.WebView
import kotlinx.coroutines.delay

/**
 * Основной движок автоматизации (аналог Droidrun)
 * Цикл: Скриншот → AI анализ → Действие → Повтор
 */
class AutomationEngine(
    private val context: Context,
    private val webView: WebView,
    private val task: String,
    private val onProgress: (String) -> Unit = {},
    private val onComplete: (String) -> Unit = {}
) {
    private val TAG = "AutomationEngine"
    private val actionHistory = mutableListOf<String>()
    private var isRunning = false
    private var maxIterations = 50 // Максимум итераций для предотвращения бесконечного цикла

    /**
     * Запустить автоматизацию
     */
    suspend fun start() {
        if (isRunning) {
            Log.w(TAG, "Automation already running")
            return
        }

        isRunning = true
        onProgress("🚀 Запуск автоматизации...")
        
        try {
            var iteration = 0
            while (isRunning && iteration < maxIterations) {
                iteration++
                onProgress("📸 Итерация $iteration/$maxIterations: Захват скриншота...")

                // 1. Захватить скриншот
                val screenshot = WebViewAutomator.captureScreenshot(webView)
                
                // 2. Отправить в AI для анализа
                onProgress("🤖 Анализ изображения через AI...")
                val action = VisionAIClient.getNextAction(
                    context = context,
                    screenshot = screenshot,
                    task = task,
                    history = actionHistory
                )

                if (action == null) {
                    onProgress("❌ AI не вернул действие")
                    delay(2000)
                    continue
                }

                // 3. Логирование действия
                val actionDescription = describeAction(action)
                actionHistory.add(actionDescription)
                onProgress("⚡ Действие: $actionDescription")

                // 4. Проверка на завершение
                if (action is AutomationAction.Finish) {
                    onComplete(action.message)
                    break
                }

                // 5. Выполнить действие
                val success = WebViewAutomator.executeAction(webView, action)
                if (!success) {
                    onProgress("⚠️ Не удалось выполнить действие")
                }

                // 6. Пауза между итерациями
                delay(1500)
            }

            if (iteration >= maxIterations) {
                onComplete("⏱️ Достигнут лимит итераций ($maxIterations)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during automation", e)
            onComplete("❌ Ошибка: ${e.message}")
        } finally {
            isRunning = false
        }
    }

    /**
     * Остановить автоматизацию
     */
    fun stop() {
        isRunning = false
        onProgress("🛑 Автоматизация остановлена")
    }

    private fun describeAction(action: AutomationAction): String {
        return when (action) {
            is AutomationAction.Click -> {
                when {
                    action.text != null -> "Клик по кнопке '${action.text}'"
                    action.x != null && action.y != null -> "Клик по координатам (${action.x}, ${action.y})"
                    action.selector != null -> "Клик по элементу '${action.selector}'"
                    else -> "Клик"
                }
            }
            is AutomationAction.Type -> "Ввод текста: '${action.text}'"
            is AutomationAction.Scroll -> "Прокрутка ${action.direction.name.lowercase()} на ${action.amount}px"
            is AutomationAction.Swipe -> "Свайп от (${action.fromX},${action.fromY}) к (${action.toX},${action.toY})"
            is AutomationAction.Drag -> "Перетаскивание от (${action.fromX},${action.fromY}) к (${action.toX},${action.toY})"
            is AutomationAction.Wait -> "Ожидание ${action.milliseconds}ms"
            is AutomationAction.Finish -> "Завершение: ${action.message}"
        }
    }
}
