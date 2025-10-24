package org.bxkr.octodiary.ai

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import android.util.Log

/**
 * Фоновый решатель заданий с невидимым WebView
 * Идеальный viewport для точных координат и устойчивости AI
 */
object BackgroundAiSolver {
    
    private var isRunning = false
    
    /**
     * Запустить авто-решение задания в фоне
     */
    suspend fun solveInBackground(
        context: Context,
        url: String,
        onProgress: (String) -> Unit = {}
    ) {
        if (isRunning) {
            Toast.makeText(context, "Уже идёт решение другого задания", Toast.LENGTH_SHORT).show()
            return
        }
        
        isRunning = true
        Log.d("BackgroundAiSolver", "🚀 Запуск фонового решения: $url")
        onProgress("Инициализация...")
        
        try {
            withContext(Dispatchers.Main) {
                // Создаём невидимый WebView с идеальным размером
                val webView = createInvisibleWebView(context)
                
                // Ждём загрузки страницы
                var pageLoaded = false
                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        pageLoaded = true
                        Log.d("BackgroundAiSolver", "✅ Страница загружена")
                    }
                }
                
                webView.loadUrl(url)
                
                // Ждём загрузки (макс 10 сек)
                var waitTime = 0
                while (!pageLoaded && waitTime < 10000) {
                    delay(100)
                    waitTime += 100
                }
                
                if (!pageLoaded) {
                    Toast.makeText(context, "Ошибка загрузки страницы", Toast.LENGTH_SHORT).show()
                    isRunning = false
                    return@withContext
                }
                
                onProgress("Страница загружена")
                delay(1000) // Даём время на рендеринг
                
                // Запускаем итеративный цикл решения
                runSolvingLoop(context, webView, url, onProgress)
            }
        } catch (e: Exception) {
            Log.e("BackgroundAiSolver", "Ошибка фонового решения", e)
            Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            isRunning = false
        }
    }
    
    /**
     * Создать невидимый WebView с правильным viewport
     */
    private fun createInvisibleWebView(context: Context): WebView {
        return WebView(context).apply {
            // КРИТИЧНО: устанавливаем размер как у реальной страницы
            // Стандартный размер для мобильных viewport
            layoutParams = android.widget.FrameLayout.LayoutParams(
                1080,  // Ширина как у страницы
                1920   // Высота
            )
            
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                
                // КРИТИЧНО: отключаем масштабирование
                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false
                
                // Устанавливаем viewport
                setInitialScale(100)  // 100% = нативный размер
            }
            
            Log.d("BackgroundAiSolver", "📱 WebView создан: ${layoutParams.width}x${layoutParams.height}")
        }
    }
    
    /**
     * Итеративный цикл решения
     */
    private suspend fun runSolvingLoop(
        context: Context,
        webView: WebView,
        url: String,
        onProgress: (String) -> Unit
    ) {
        var iterationCount = 0
        val maxIterations = 10
        
        while (iterationCount < maxIterations) {
            iterationCount++
            Log.d("BackgroundAiSolver", "🔄 Итерация $iterationCount")
            onProgress("Итерация $iterationCount/$maxIterations")
            
            // 1. Скриншот
            val screenshot = withContext(Dispatchers.Main) {
                captureWebViewScreenshot(webView)
            }
            
            if (screenshot == null) {
                Toast.makeText(context, "Ошибка создания скриншота", Toast.LENGTH_SHORT).show()
                break
            }
            
            Log.d("BackgroundAiSolver", "📸 Скриншот: ${screenshot.width}x${screenshot.height}")
            
            // 2. Отправляем AI
            onProgress("Анализ AI...")
            val result = AiTaskSolver.solveTaskFromScreenshot(context, url, screenshot)
            
            result.onSuccess { solution ->
                Log.d("BackgroundAiSolver", "🤖 AI: ${solution.steps.size} шагов")
                onProgress("Выполнение ${solution.steps.size} шагов...")
                
                // 3. Выполняем решение
                delay(500)
                withContext(Dispatchers.Main) {
                    solution.steps.forEachIndexed { index, step ->
                        AiTaskSolver.executeStep(webView, step, index + 1, solution.steps.size)
                    }
                }
                
                // 4. Ждём
                val aiDelay = solution.nextIterationDelay?.toLong() ?: 2000L
                delay(aiDelay)
                
                // 5. Проверяем завершение
                if (solution.nextIterationDelay == null) {
                    Log.d("BackgroundAiSolver", "✅ Решение завершено")
                    Toast.makeText(
                        context,
                        "✅ Задание решено за $iterationCount итераций",
                        Toast.LENGTH_LONG
                    ).show()
                    onProgress("Готово!")
                    isRunning = false
                    return
                }
                
                val lastStep = solution.steps.lastOrNull()
                if (lastStep?.action != "scroll") {
                    Log.d("BackgroundAiSolver", "✅ Последний шаг не scroll - завершаем")
                    Toast.makeText(
                        context,
                        "✅ Задание решено за $iterationCount итераций",
                        Toast.LENGTH_LONG
                    ).show()
                    onProgress("Готово!")
                    isRunning = false
                    return
                }
                
            }.onFailure { error ->
                Log.e("BackgroundAiSolver", "Ошибка AI", error)
                Toast.makeText(context, "Ошибка AI: ${error.message}", Toast.LENGTH_SHORT).show()
                isRunning = false
                return
            }
        }
        
        // Лимит итераций
        Log.d("BackgroundAiSolver", "⚠️ Лимит итераций")
        Toast.makeText(context, "⚠️ Достигнут лимит $maxIterations итераций", Toast.LENGTH_SHORT).show()
        onProgress("Лимит итераций")
        isRunning = false
    }
    
    /**
     * Создать скриншот WebView
     */
    private fun captureWebViewScreenshot(webView: WebView): android.graphics.Bitmap? {
        return try {
            val bitmap = android.graphics.Bitmap.createBitmap(
                webView.width,
                webView.height,
                android.graphics.Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            webView.draw(canvas)
            bitmap
        } catch (e: Exception) {
            Log.e("BackgroundAiSolver", "Ошибка скриншота", e)
            null
        }
    }
}
