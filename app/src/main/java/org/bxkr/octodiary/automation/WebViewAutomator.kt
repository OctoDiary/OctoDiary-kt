package org.bxkr.octodiary.automation

import android.graphics.Bitmap
import android.util.Log
import android.view.MotionEvent
import android.webkit.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Выполняет действия в WebView (аналог Droidrun)
 */
object WebViewAutomator {
    private const val TAG = "WebViewAutomator"

    /**
     * Выполнить действие в WebView
     */
    suspend fun executeAction(webView: WebView, action: AutomationAction): Boolean {
        return withContext(Dispatchers.Main) {
            try {
                when (action) {
                    is AutomationAction.Click -> executeClick(webView, action)
                    is AutomationAction.Type -> executeType(webView, action)
                    is AutomationAction.Scroll -> executeScroll(webView, action)
                    is AutomationAction.Swipe -> executeSwipe(webView, action)
                    is AutomationAction.Drag -> executeDrag(webView, action)
                    is AutomationAction.Wait -> {
                        delay(action.milliseconds)
                        true
                    }
                    is AutomationAction.Finish -> true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error executing action: $action", e)
                false
            }
        }
    }

    /**
     * Получить скриншот WebView
     */
    suspend fun captureScreenshot(webView: WebView): Bitmap {
        return withContext(Dispatchers.Main) {
            val bitmap = Bitmap.createBitmap(
                webView.width,
                webView.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            webView.draw(canvas)
            bitmap
        }
    }

    private fun executeClick(webView: WebView, action: AutomationAction.Click): Boolean {
        return when {
            action.x != null && action.y != null -> {
                // Клик по координатам через MotionEvent
                val downEvent = MotionEvent.obtain(
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    MotionEvent.ACTION_DOWN,
                    action.x.toFloat(),
                    action.y.toFloat(),
                    0
                )
                val upEvent = MotionEvent.obtain(
                    System.currentTimeMillis() + 50,
                    System.currentTimeMillis() + 50,
                    MotionEvent.ACTION_UP,
                    action.x.toFloat(),
                    action.y.toFloat(),
                    0
                )
                webView.dispatchTouchEvent(downEvent)
                webView.dispatchTouchEvent(upEvent)
                downEvent.recycle()
                upEvent.recycle()
                true
            }
            action.text != null -> {
                // Клик по элементу с текстом через JavaScript
                val js = """
                    (function() {
                        function findElementByText(text) {
                            var elements = document.querySelectorAll('button, a, [role="button"], [onclick], input[type="submit"], input[type="button"]');
                            for (var i = 0; i < elements.length; i++) {
                                var el = elements[i];
                                var elText = (el.innerText || el.textContent || '').trim();
                                if (elText.toLowerCase().includes(text.toLowerCase())) {
                                    return el;
                                }
                            }
                            return null;
                        }
                        
                        var element = findElementByText('${action.text}');
                        if (element) {
                            element.click();
                            return true;
                        }
                        return false;
                    })();
                """.trimIndent()
                webView.evaluateJavascript(js, null)
                true
            }
            action.selector != null -> {
                // Клик по селектору
                val js = """
                    (function() {
                        var element = document.querySelector('${action.selector}');
                        if (element) {
                            element.click();
                            return true;
                        }
                        return false;
                    })();
                """.trimIndent()
                webView.evaluateJavascript(js, null)
                true
            }
            else -> false
        }
    }

    private fun executeType(webView: WebView, action: AutomationAction.Type): Boolean {
        val selector = action.selector ?: "input:focus, textarea:focus"
        val js = """
            (function() {
                var element = document.querySelector('$selector');
                if (element) {
                    element.value = '${action.text.replace("'", "\\'")}';
                    element.dispatchEvent(new Event('input', { bubbles: true }));
                    element.dispatchEvent(new Event('change', { bubbles: true }));
                    return true;
                }
                return false;
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
        return true
    }

    private fun executeScroll(webView: WebView, action: AutomationAction.Scroll): Boolean {
        val (x, y) = when (action.direction) {
            AutomationAction.Scroll.Direction.UP -> 0 to -action.amount
            AutomationAction.Scroll.Direction.DOWN -> 0 to action.amount
            AutomationAction.Scroll.Direction.LEFT -> -action.amount to 0
            AutomationAction.Scroll.Direction.RIGHT -> action.amount to 0
        }
        
        val js = "window.scrollBy($x, $y);"
        webView.evaluateJavascript(js, null)
        return true
    }

    private suspend fun executeSwipe(webView: WebView, action: AutomationAction.Swipe): Boolean {
        return withContext(Dispatchers.Main) {
            val startTime = System.currentTimeMillis()
            val downEvent = MotionEvent.obtain(
                startTime,
                startTime,
                MotionEvent.ACTION_DOWN,
                action.fromX.toFloat(),
                action.fromY.toFloat(),
                0
            )
            webView.dispatchTouchEvent(downEvent)
            downEvent.recycle()

            // Генерируем промежуточные MOVE события для плавности
            val steps = 10
            val stepDuration = action.durationMs / steps
            for (i in 1..steps) {
                val progress = i.toFloat() / steps
                val currentX = action.fromX + (action.toX - action.fromX) * progress
                val currentY = action.fromY + (action.toY - action.fromY) * progress
                val currentTime = startTime + stepDuration * i
                
                val moveEvent = MotionEvent.obtain(
                    startTime,
                    currentTime,
                    MotionEvent.ACTION_MOVE,
                    currentX,
                    currentY,
                    0
                )
                webView.dispatchTouchEvent(moveEvent)
                moveEvent.recycle()
                
                delay(stepDuration)
            }

            val upEvent = MotionEvent.obtain(
                startTime,
                startTime + action.durationMs,
                MotionEvent.ACTION_UP,
                action.toX.toFloat(),
                action.toY.toFloat(),
                0
            )
            webView.dispatchTouchEvent(upEvent)
            upEvent.recycle()
            
            true
        }
    }

    private suspend fun executeDrag(webView: WebView, action: AutomationAction.Drag): Boolean {
        // Drag - это долгий swipe с небольшой задержкой в начале
        delay(200) // Имитация long press
        return executeSwipe(
            webView,
            AutomationAction.Swipe(action.fromX, action.fromY, action.toX, action.toY, 500)
        )
    }
}
