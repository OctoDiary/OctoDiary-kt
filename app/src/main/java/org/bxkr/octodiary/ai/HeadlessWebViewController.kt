package org.bxkr.octodiary.ai

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HeadlessWebViewController(
    private val context: Context
) {
    private var webView: WebView? = null
    private var running = false
    private var loopJob: Job? = null
    private var handler: Handler? = null
    private var onDoneCallback: ((String) -> Unit)? = null
    private var onAiResponseCallback: ((String) -> Unit)? = null

    fun start(url: String, aiPrompt: String, onDone: (String) -> Unit, onAiResponse: ((String) -> Unit)? = null) {
        android.util.Log.d("HeadlessWebView", "=== START CALLED ===")
        if (running) stop()
        running = true
        onDoneCallback = onDone
        onAiResponseCallback = onAiResponse
        handler = Handler(Looper.getMainLooper())
        android.util.Log.d("HeadlessWebView", "Starting headless automation for URL: $url")
        android.util.Log.d("HeadlessWebView", "AI Prompt: $aiPrompt")
        
        try {
            handler?.post {
                android.util.Log.d("HeadlessWebView", "Creating WebView in handler")
                webView = WebView(context).apply {
                    android.util.Log.d("HeadlessWebView", "WebView created, setting up")
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            android.util.Log.d("HeadlessWebView", "Page finished loading: $url")
                            // Небольшая задержка для стабилизации страницы
                            handler?.postDelayed({
                                android.util.Log.d("HeadlessWebView", "Starting AI loop after delay")
                                launchAIClickerLoop(aiPrompt)
                            }, 2000)
                        }
                        
                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            android.util.Log.d("HeadlessWebView", "Page started loading: $url")
                        }
                        
                    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                        android.util.Log.d("HeadlessWebView", "History updated: $url, reload: $isReload")
                        // Перезапускаем цикл при навигации
                        if (running && !isReload && handler != null) {
                            try {
                                handler.postDelayed({
                                    launchAIClickerLoop(aiPrompt)
                                }, 1500)
                            } catch (e: Exception) {
                                android.util.Log.e("HeadlessWebView", "Error in doUpdateVisitedHistory: ${e.message}")
                            }
                        }
                    }
                        
                        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                            android.util.Log.e("HeadlessWebView", "WebView error: $errorCode, $description, URL: $failingUrl")
                        }
                    }
                    
                    android.util.Log.d("HeadlessWebView", "Loading URL: $url")
                    loadUrl(url)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HeadlessWebView", "Exception in start: ${e.message}", e)
            onDone("Ошибка создания WebView: ${e.message}")
        }
    }

    fun stop() {
        android.util.Log.d("HeadlessWebView", "Stopping headless automation")
        running = false
        loopJob?.cancel()
        try {
            handler?.post {
                webView?.destroy()
                webView = null
            }
            handler = null
        } catch (e: Exception) {
            android.util.Log.e("HeadlessWebView", "Error in stop: ${e.message}")
        }
    }

    private fun launchAIClickerLoop(aiPrompt: String) {
        if (!running || webView == null) return
        android.util.Log.d("HeadlessWebView", "Starting AI clicker loop")
        
        // Отменяем предыдущий цикл если он есть
        loopJob?.cancel()
        
        loopJob = CoroutineScope(Dispatchers.Main).launch {
            var iteration = 0
            while (running) {
                iteration++
                android.util.Log.d("HeadlessWebView", "AI loop iteration: $iteration")
                
                val wv = webView ?: break
                
                // Делаем скриншот
                val bitmap = wv.drawToBitmapSafe()
                if (bitmap == null) {
                    android.util.Log.e("HeadlessWebView", "Failed to capture screenshot")
                    onDoneCallback?.invoke("Ошибка: не удалось получить скриншот")
                    stop(); break
                }
                
                android.util.Log.d("HeadlessWebView", "Screenshot captured, sending to AI...")
                
                // Отправляем в ИИ
                val step = OpenAiClient.completeWithScreenshot(context, "gpt-4o", aiPrompt, bitmap)
                android.util.Log.d("HeadlessWebView", "AI response: x=${step.x}, y=${step.y}, wait=${step.waitMs}, done=${step.done}")
                
                // Отправляем ответ в DebugWebView
                val responseText = if (step.done == true) "DONE: ${step.message ?: "Задача завершена"}" 
                    else "CLICK: (${step.x}, ${step.y}) wait=${step.waitMs}ms"
                onAiResponseCallback?.invoke(responseText)
                
                // Проверяем на завершение
                if (step.done == true) {
                    android.util.Log.d("HeadlessWebView", "Task completed by AI")
                    onDoneCallback?.invoke(step.message ?: "Завершено по done=true")
                    stop(); break
                }
                
                // Кликаем если есть координаты
                if (step.x != null && step.y != null) {
                    android.util.Log.d("HeadlessWebView", "Clicking at coordinates: ${step.x}, ${step.y}")
                    wv.simulateClick(step.x, step.y)
                } else {
                    android.util.Log.w("HeadlessWebView", "No coordinates provided by AI")
                }
                
                // Ждём перед следующим циклом
                val waitMs = step.waitMs.coerceIn(200, 15000)
                android.util.Log.d("HeadlessWebView", "Waiting ${waitMs}ms before next iteration")
                delay(waitMs.toLong())
                
                // Ограничиваем количество итераций для безопасности
                if (iteration > 50) {
                    android.util.Log.w("HeadlessWebView", "Reached max iterations (50), stopping")
                    onDoneCallback?.invoke("Достигнуто максимальное количество итераций")
                    stop(); break
                }
            }
        }
    }

    private fun WebView.drawToBitmapSafe(): Bitmap? = try {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        draw(canvas)
        bmp
    } catch (e: Exception) { null }

    private fun WebView.simulateClick(x: Int, y: Int) {
        val now = System.currentTimeMillis()
        val down = android.view.MotionEvent.obtain(now, now, android.view.MotionEvent.ACTION_DOWN, x.toFloat(), y.toFloat(), 0)
        val up = android.view.MotionEvent.obtain(now+40, now+40, android.view.MotionEvent.ACTION_UP, x.toFloat(), y.toFloat(), 0)
        this.dispatchTouchEvent(down)
        this.dispatchTouchEvent(up)
        down.recycle()
        up.recycle()
    }
}
