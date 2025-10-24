package org.bxkr.octodiary.ai

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.delay

/**
 * Решение задания от AI
 */
data class AiTaskSolution(
    @SerializedName("short_solution")
    val shortSolution: String, // Краткое решение для WebView
    
    @SerializedName("detailed_solution")
    val detailedSolution: String, // Полное решение для истории
    
    @SerializedName("steps")
    val steps: List<TaskStep>, // Шаги для автоматического выполнения
    
    @SerializedName("estimated_time")
    val estimatedTime: Int, // Оценочное время в секундах
    
    @SerializedName("next_iteration_delay")
    val nextIterationDelay: Int? = null // Задержка перед следующей итерацией (в мс), null если это финальная итерация
)

/**
 * Шаг выполнения задания
 */
data class TaskStep(
    @SerializedName("action")
    val action: String, // "tap", "input", "scroll", "swipe", "drag", "wait"
    
    @SerializedName("selector")
    val selector: String?, // CSS селектор элемента
    
    @SerializedName("value")
    val value: String?, // Значение для ввода
    
    @SerializedName("x")
    val x: Float?, // Координата X (для tap, drag)
    
    @SerializedName("y")
    val y: Float?, // Координата Y (для tap, drag)
    
    @SerializedName("duration")
    val duration: Int? // Длительность в миллисекундах (для wait, scroll)
)

/**
 * Сервис автоматического решения заданий через AI
 */
object AiTaskSolver {
    
    /**
     * Получить решение задания от AI на основе скриншота
     */
    suspend fun solveTaskFromScreenshot(
        context: Context,
        taskUrl: String,
        screenshot: android.graphics.Bitmap
    ): Result<AiTaskSolution> {
        return try {
            val prompt = buildPromptForScreenshot(taskUrl, screenshot.width, screenshot.height)
            
            // Конвертируем bitmap в base64
            val base64Image = bitmapToBase64(screenshot)
            
            Log.d("AiTaskSolver", "Отправляю скриншот AI: ${screenshot.width}x${screenshot.height}, base64 size: ${base64Image.length}")
            
            // Отправляем запрос к AI с изображением
            val result = GeminiService.sendMessage(context, prompt, base64Image)
            
            result.mapCatching { response ->
                Log.d("AiTaskSolver", "Получен ответ от AI: $response")
                
                // Парсим JSON ответ
                val solution = parseTaskSolution(response)
                    ?: throw Exception("Не удалось распарсить решение. Ответ AI: ${response.take(500)}")
                
                Log.d("AiTaskSolver", "Решение распарсено: ${solution.steps.size} шагов")
                
                // Детальное логирование всех шагов
                solution.steps.forEachIndexed { index, step ->
                    Log.d("AiTaskSolver", "Шаг ${index + 1}: ${step.action} " +
                        "(x=${step.x}, y=${step.y}, value=${step.value}, duration=${step.duration})")
                }
                
                // Добавляем реалистичные задержки между шагами
                addRealisticDelays(solution)
            }
        } catch (e: Exception) {
            Log.e("AiTaskSolver", "Ошибка решения задания: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Получить решение задания от AI (устаревший метод)
     */
    suspend fun solveTask(
        context: Context,
        taskUrl: String,
        taskContent: String
    ): Result<AiTaskSolution> {
        return try {
            val prompt = buildPrompt(taskUrl, taskContent)
            
            // Отправляем запрос к AI
            val result = GeminiService.sendMessage(context, prompt)
            
            result.mapCatching { response ->
                Log.d("AiTaskSolver", "Получен ответ от AI: $response")
                
                // Парсим JSON ответ
                val solution = parseTaskSolution(response)
                    ?: throw Exception("Не удалось распарсить решение. Ответ AI: ${response.take(500)}")
                
                Log.d("AiTaskSolver", "Решение распарсено: ${solution.steps.size} шагов")
                
                // Добавляем реалистичные задержки между шагами
                addRealisticDelays(solution)
            }
        } catch (e: Exception) {
            Log.e("AiTaskSolver", "Ошибка решения задания: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Построить промпт для AI на основе скриншота
     */
    private fun buildPromptForScreenshot(taskUrl: String, width: Int, height: Int): String {
        return """
            Ты - эксперт по решению образовательных заданий. Перед тобой скриншот задания размером ${width}x${height} пикселей.
            
            **URL задания**: $taskUrl
            
            **ВАЖНО**: Это может быть длинная страница с множеством вопросов! 
            
            **Твоя задача**:
            1. Проанализируй ВЕСЬ видимый контент на скриншоте
            2. Найди ВСЕ поля ввода, кнопки, чекбоксы, радиокнопки на ТЕКУЩЕЙ видимой части
            3. Определи ТОЧНЫЕ координаты (X, Y) ЦЕНТРА каждого элемента
            4. Для КАЖДОГО поля ввода создай ДВА действия: TAP (клик) → INPUT (ввод текста)
            5. **КРИТИЧЕСКИ ВАЖНО**: После ответа на видимые вопросы ОБЯЗАТЕЛЬНО добавь SCROLL вниз
            6. Продолжай добавлять циклы "ответить на вопросы → SCROLL вниз" пока не решишь ВСЕ задания
            7. В конце найди и нажми кнопку "Отправить" / "Сохранить" / "Далее"
            
            Верни результат строго в формате JSON:
            ```json
            {
              "short_solution": "Задача 1: 42, Задача 2: ответ, Задача 3: выбран вариант А",
              "detailed_solution": "Задача 1: правильный ответ 42, потому что... Задача 2: ответ, потому что...",
              "steps": [
                {"action": "tap", "x": 540, "y": 320},
                {"action": "input", "x": 540, "y": 320, "value": "42"},
                {"action": "tap", "x": 540, "y": 520},
                {"action": "input", "x": 540, "y": 520, "value": "ответ"},
                {"action": "scroll", "duration": 600},
                {"action": "tap", "x": 540, "y": 400},
                {"action": "scroll", "duration": 700},
                {"action": "tap", "x": 540, "y": 1800}
              ],
              "estimated_time": 90,
              "next_iteration_delay": 2000
            }
            ```
            
            **next_iteration_delay** - время в миллисекундах перед следующей итерацией:
            - Если последний шаг = scroll: укажи 1500-3000 (время на загрузку следующего контента)
            - Если все задания решены, но ещё не отправлены: укажи 2000-3000 (время на финальную проверку)
            - Если последний шаг = кнопка отправки: укажи null (финальная итерация, цикл завершится)
            - Чем сложнее задание, тем больше delay
            
            **ФИНАЛЬНАЯ ПРОВЕРКА ПЕРЕД ОТПРАВКОЙ**:
            После решения всех заданий создай ещё одну итерацию для проверки:
            1. Scroll вверх (отрицательный duration, например -1500)
            2. Scroll вниз обратно (положительный duration, например 1500)
            3. Tap на кнопку "Отправить" / "Проверить" / "Сохранить"
            4. next_iteration_delay = null (это финал)
            
            **ОБРАТИ ВНИМАНИЕ**:
            - КАЖДОЕ поле ввода = 2 действия: tap + input
            - Координаты tap и input ОДИНАКОВЫЕ
            - Между разными полями можно добавить scroll если нужно
            - В конце обязательно нажать кнопку "Отправить" / "Сохранить"
            
            **ФОРМАТ ПОЛЕЙ**:
            - tap: только "action", "x", "y" (без selector, value, duration)
            - input: только "action", "x", "y", "value" (без selector, duration)
            - scroll: только "action", "duration" (без x, y, selector, value)
            - wait НЕ НУЖЕН - система сама добавит задержки между шагами!
            
            **КРИТИЧЕСКИ ВАЖНО**:
            1. X, Y - координаты в пикселях от левого верхнего угла
            2. X: от 0 до $width, Y: от 0 до $height
            3. **ПЕРЕД КАЖДЫМ INPUT ОБЯЗАТЕЛЬНО ДОЛЖЕН БЫТЬ TAP** по тем же координатам!
            4. Для текстовых полей последовательность: TAP (x, y) → INPUT (x, y, value)
            5. **Добавляй scroll (500-800px) если нужно увидеть больше контента!**
            
            **ПРАВИЛЬНАЯ последовательность для ввода текста**:
            
            ✅ ПРАВИЛЬНО - для ОДНОГО поля:
            {"action": "tap", "x": 150, "y": 320}
            {"action": "input", "x": 150, "y": 320, "value": "42"}
            
            ✅ ПРАВИЛЬНО - для ДВУХ полей:
            {"action": "tap", "x": 150, "y": 320}
            {"action": "input", "x": 150, "y": 320, "value": "42"}
            {"action": "tap", "x": 150, "y": 520}
            {"action": "input", "x": 150, "y": 520, "value": "ответ"}
            
            ❌ НЕПРАВИЛЬНО (input без tap):
            {"action": "input", "x": 150, "y": 320, "value": "42"}
            
            ❌ НЕПРАВИЛЬНО (только один tap для двух полей):
            {"action": "tap", "x": 150, "y": 320}
            {"action": "input", "x": 150, "y": 320, "value": "42"}
            {"action": "input", "x": 150, "y": 520, "value": "ответ"}
            
            **Типы действий**:
            - tap: прямой клик по координатам (x, y) - как автокликер
            - input: ввод текста (x, y + value)
            - scroll: прокрутка вниз (duration = количество пикселей, например 500)
            - wait: задержка (duration в миллисекундах)
            
            **Признаки длинной страницы (нужен scroll)**:
            - Текст обрезан внизу экрана
            - Виден скроллбар справа
            - Есть кнопка "Далее" / "Продолжить" внизу
            - Видны только первые N вопросов из большего количества
            - Есть индикатор прогресса "1 из 10"
            
            **Примеры координат**:
            - Центр экрана: x: ${width/2}, y: ${height/2}
            - Верх страницы: x: ${width/2}, y: 200
            - Низ страницы: x: ${width/2}, y: ${height - 100}
            
            **Пример решения длинной страницы**:
            
            Итерация 1 (видим вопросы 1-2, нужен scroll):
            ```json
            {
              "short_solution": "Задача 1: ответ1, Задача 2: ответ2",
              "steps": [
                {"action": "tap", "x": 540, "y": 700},
                {"action": "input", "x": 540, "y": 700, "value": "ответ1"},
                {"action": "tap", "x": 540, "y": 900},
                {"action": "input", "x": 540, "y": 900, "value": "ответ2"},
                {"action": "scroll", "duration": 700}
              ],
              "estimated_time": 45,
              "next_iteration_delay": 2000
            }
            ```
            
            Итерация 2 (после scroll, видим вопросы 3-4):
            ```json
            {
              "short_solution": "Задача 3: ответ3, Задача 4: ответ4",
              "steps": [
                {"action": "tap", "x": 540, "y": 800},
                {"action": "input", "x": 540, "y": 800, "value": "ответ3"},
                {"action": "tap", "x": 540, "y": 950},
                {"action": "input", "x": 540, "y": 950, "value": "ответ4"},
                {"action": "scroll", "duration": 700}
              ],
              "estimated_time": 50,
              "next_iteration_delay": 2000
            }
            ```
            
            Итерация 3 (финальная проверка и отправка):
            ```json
            {
              "short_solution": "Проверка: все ответы на месте",
              "steps": [
                {"action": "scroll", "duration": -1500},
                {"action": "scroll", "duration": 1500},
                {"action": "tap", "x": 540, "y": 1800}
              ],
              "estimated_time": 15,
              "next_iteration_delay": null
            }
            ```
            
            **КРИТИЧЕСКИ ВАЖНО - ЦИКЛ РЕШЕНИЯ**:
            1. Ответь на ВСЕ ВИДИМЫЕ вопросы (обычно 1-2)
            2. SCROLL вниз (700-800px)
            3. ПОВТОРЯЙ пункты 1-2 пока не закончатся вопросы
            4. ПЕРЕД ОТПРАВКОЙ:
               - Scroll к началу страницы (scroll вверх с отрицательным duration)
               - Проверь что ВСЕ поля заполнены
               - Если есть незаполненные - заполни их
               - Scroll к концу страницы
            5. В КОНЦЕ найди и нажми кнопку "Отправить" / "Проверить" / "Сохранить"
            
            Верни ТОЛЬКО валидный JSON, без markdown разметки!
        """.trimIndent()
    }
    
    /**
     * Конвертировать Bitmap в Base64
     */
    private fun bitmapToBase64(bitmap: android.graphics.Bitmap): String {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
        return android.util.Base64.encodeToString(
            outputStream.toByteArray(),
            android.util.Base64.NO_WRAP
        )
    }
    
    /**
     * Построить промпт для AI (устаревший метод)
     */
    private fun buildPrompt(taskUrl: String, taskContent: String): String {
        return """
            Ты - эксперт по решению образовательных заданий. Твоя задача - проанализировать HTML страницу задания и предоставить решение с КОНКРЕТНЫМИ CSS селекторами для автоматического выполнения.
            
            **URL задания**: $taskUrl
            **Содержимое страницы**: 
            $taskContent
            
            **ВАЖНО**: Проанализируй HTML код выше и найди РЕАЛЬНЫЕ CSS селекторы элементов (input, button, radio, checkbox и т.д.)
            
            Верни результат строго в формате JSON:
            ```json
            {
              "short_solution": "Ответы: 1-A, 2-B, 3-C",
              "detailed_solution": "1. Вопрос 1: правильный ответ A, потому что...",
              "steps": [
                {"action": "tap", "selector": "input[name='q1'][value='A']", "value": null, "x": null, "y": null, "duration": null},
                {"action": "wait", "selector": null, "value": null, "x": null, "y": null, "duration": 800},
                {"action": "input", "selector": "#answer_text", "value": "42", "x": null, "y": null, "duration": null},
                {"action": "tap", "selector": "button[type='submit']", "value": null, "x": null, "y": null, "duration": null}
              ],
              "estimated_time": 45
            }
            ```
            
            **КРИТИЧЕСКИ ВАЖНО**:
            1. Используй ТОЛЬКО CSS селекторы из HTML кода выше (input[name='...'], #id, .class, button, etc)
            2. Для radio buttons используй: input[name='question1'][value='A']
            3. Для checkboxes используй: input[type='checkbox'][name='...']
            4. Для текстовых полей: input[type='text'], textarea, input#id
            5. Для кнопок: button.submit, input[type='submit'], button#next
            6. Добавляй wait (800-1500ms) между каждым действием для реалистичности
            7. estimated_time = количество_шагов * 1.5 секунд (минимум 30 сек)
            
            **Примеры правильных селекторов**:
            - Радио кнопка: "input[name='q1'][value='A']"
            - Чекбокс: "input[type='checkbox']#option1"
            - Текстовое поле: "input[name='answer']" или "#answer_text"
            - Кнопка отправки: "button[type='submit']" или "button.submit-btn"
            
            **Формат действий**:
            - tap: клик по элементу (ОБЯЗАТЕЛЬНО selector)
            - input: ввод текста (ОБЯЗАТЕЛЬНО selector + value)
            - wait: задержка (ОБЯЗАТЕЛЬНО duration в миллисекундах)
            - scroll: прокрутка (duration = количество пикселей)
            
            Верни ТОЛЬКО валидный JSON, без markdown разметки и дополнительного текста!
        """.trimIndent()
    }
    
    /**
     * Парсинг решения из JSON
     */
    private fun parseTaskSolution(jsonString: String): AiTaskSolution? {
        return try {
            // Извлекаем JSON из ответа (если есть обёртка)
            val jsonStart = jsonString.indexOf('{')
            val jsonEnd = jsonString.lastIndexOf('}')
            
            if (jsonStart == -1 || jsonEnd == -1) {
                return null
            }
            
            val cleanJson = jsonString.substring(jsonStart, jsonEnd + 1)
            Gson().fromJson(cleanJson, AiTaskSolution::class.java)
        } catch (e: Exception) {
            Log.e("AiTaskSolver", "Ошибка парсинга: ${e.message}")
            null
        }
    }
    
    /**
     * Добавить реалистичные задержки между шагами
     */
    private fun addRealisticDelays(solution: AiTaskSolution): AiTaskSolution {
        val stepsWithDelays = mutableListOf<TaskStep>()
        
        solution.steps.forEachIndexed { index, step ->
            stepsWithDelays.add(step)
            
            // Добавляем wait после каждого действия (кроме последнего и если это не wait)
            if (index < solution.steps.size - 1 && step.action != "wait") {
                val delay = when (step.action) {
                    "tap" -> {
                        // Если следующий шаг - input, даём больше времени на фокус
                        val nextStep = solution.steps.getOrNull(index + 1)
                        if (nextStep?.action == "input") {
                            (700..1000).random()  // Ещё больше времени для надёжного фокуса
                        } else {
                            (300..800).random()
                        }
                    }
                    "input" -> (800..1500).random()  // Больше задержки после ввода для реалистичности
                    "scroll" -> (200..500).random()
                    "swipe" -> (400..800).random()
                    "drag" -> (500..1200).random()
                    else -> (300..700).random()
                }
                
                stepsWithDelays.add(
                    TaskStep(
                        action = "wait",
                        selector = null,
                        value = null,
                        x = null,
                        y = null,
                        duration = delay
                    )
                )
            }
        }
        
        return solution.copy(steps = stepsWithDelays)
    }
    
    /**
     * Выполнить действие в WebView
     */
    fun executeStep(webView: android.webkit.WebView, step: TaskStep, stepNumber: Int = 0, totalSteps: Int = 0) {
        if (totalSteps > 0) {
            Log.d("AiTaskSolver", "Выполняется шаг $stepNumber/$totalSteps: ${step.action} (selector: ${step.selector}, value: ${step.value})")
        }
        
        // Выполняем на UI потоке
        webView.post {
            when (step.action) {
                "tap" -> {
                    if (step.x != null && step.y != null) {
                        // НАСТОЯЩИЙ клик по координатам через MotionEvent (как автокликер)
                        Log.d("AiTaskSolver", "Tap по координатам: (${step.x}, ${step.y})")
                        
                        val downTime = android.os.SystemClock.uptimeMillis()
                        val eventTime = android.os.SystemClock.uptimeMillis()
                        
                        // DOWN событие
                        val motionEventDown = android.view.MotionEvent.obtain(
                            downTime,
                            eventTime,
                            android.view.MotionEvent.ACTION_DOWN,
                            step.x.toFloat(),
                            step.y.toFloat(),
                            0
                        )
                        
                        // UP событие
                        val motionEventUp = android.view.MotionEvent.obtain(
                            downTime,
                            eventTime + 50,
                            android.view.MotionEvent.ACTION_UP,
                            step.x.toFloat(),
                            step.y.toFloat(),
                            0
                        )
                        
                        try {
                            webView.dispatchTouchEvent(motionEventDown)
                            android.os.SystemClock.sleep(50)
                            webView.dispatchTouchEvent(motionEventUp)
                            Log.d("AiTaskSolver", "✓ Tap выполнен: (${step.x}, ${step.y})")
                            
                            // Принудительно фокусируем элемент через JavaScript для надёжности
                            android.os.SystemClock.sleep(100) // Даём время на обработку tap
                            
                            // Логируем размеры WebView
                            Log.d("AiTaskSolver", "WebView размеры: ${webView.width}x${webView.height}, координаты: (${step.x}, ${step.y})")
                            
                            val jsFocus = """
                                (function() {
                                    var pageWidth = window.innerWidth || document.documentElement.clientWidth;
                                    var pageHeight = window.innerHeight || document.documentElement.clientHeight;
                                    
                                    // Масштабируем координаты: AI дал для скриншота ${webView.width}x${webView.height},
                                    // но реальная страница имеет размер pageWidth x pageHeight
                                    var scaleX = pageWidth / ${webView.width};
                                    var scaleY = pageHeight / ${webView.height};
                                    
                                    var scaledX = ${step.x} * scaleX;
                                    var scaledY = ${step.y} * scaleY;
                                    
                                    console.log('Страница: ' + pageWidth + 'x' + pageHeight);
                                    console.log('Scale: ' + scaleX.toFixed(2) + 'x' + scaleY.toFixed(2));
                                    console.log('Координаты: (${step.x}, ${step.y}) → (' + scaledX.toFixed(0) + ', ' + scaledY.toFixed(0) + ')');
                                    
                                    // Проверяем границы масштабированных координат
                                    if (scaledX < 0 || scaledX > pageWidth || scaledY < 0 || scaledY > pageHeight) {
                                        return 'ERROR: Scaled coords out of bounds (' + scaledX.toFixed(0) + ',' + scaledY.toFixed(0) + ') page=' + pageWidth + 'x' + pageHeight;
                                    }
                                    
                                    var element = document.elementFromPoint(scaledX, scaledY);
                                    
                                    if (!element) {
                                        // Попробуем найти ВСЕ input на странице
                                        var inputs = document.querySelectorAll('input[type="text"], input[type="number"], textarea');
                                        if (inputs.length > 0) {
                                            var firstInput = inputs[0];
                                            firstInput.click();  // КРИТИЧНО: click перед focus
                                            firstInput.focus();
                                            var rect = firstInput.getBoundingClientRect();
                                            return 'FALLBACK_FIRST_INPUT: ' + firstInput.tagName + ' at (' + rect.left + ',' + rect.top + ')';
                                        }
                                        return 'ERROR: No element at (' + ${step.x} + ',' + ${step.y} + ') page=' + pageWidth + 'x' + pageHeight;
                                    }
                                    
                                    // Детальная информация об элементе
                                    var info = 'Element: ' + element.tagName;
                                    if (element.id) info += ' id=' + element.id;
                                    if (element.className) info += ' class=' + element.className;
                                    if (element.type) info += ' type=' + element.type;
                                    
                                    console.log('Элемент по координатам: ' + info);
                                    
                                    // Проверяем, может это родитель input?
                                    var input = element.querySelector('input, textarea');
                                    if (input) {
                                        input.click();  // КРИТИЧНО: сначала click для активации
                                        input.focus();
                                        return 'FOCUSED_CHILD: ' + input.tagName;
                                    }
                                    
                                    if (element.tagName === 'INPUT' || element.tagName === 'TEXTAREA') {
                                        element.click();  // КРИТИЧНО: сначала click для активации
                                        element.focus();
                                        return 'FOCUSED: ' + element.tagName;
                                    }
                                    
                                    // Если это не input, попробуем найти ПУСТОЙ input (не первый!)
                                    var allInputs = document.querySelectorAll('input[type="text"], input[type="number"], textarea');
                                    if (allInputs.length > 0) {
                                        // Ищем первый ПУСТОЙ input
                                        var emptyInput = null;
                                        for (var i = 0; i < allInputs.length; i++) {
                                            if (!allInputs[i].value || allInputs[i].value.trim() === '') {
                                                emptyInput = allInputs[i];
                                                break;
                                            }
                                        }
                                        
                                        if (emptyInput) {
                                            emptyInput.click();  // КРИТИЧНО: click перед focus
                                            emptyInput.focus();
                                            return 'FALLBACK_TO_EMPTY: ' + info + ', focused empty input';
                                        } else {
                                            allInputs[0].click();  // КРИТИЧНО: click перед focus
                                            allInputs[0].focus();
                                            return 'FALLBACK_TO_FIRST: ' + info + ', all inputs filled';
                                        }
                                    }
                                    
                                    return info;
                                })();
                            """.trimIndent()
                            webView.evaluateJavascript(jsFocus) { result ->
                                Log.d("AiTaskSolver", "Focus result: $result")
                            }
                        } catch (e: Exception) {
                            Log.e("AiTaskSolver", "Ошибка tap: ${e.message}")
                        } finally {
                            motionEventDown.recycle()
                            motionEventUp.recycle()
                        }
                    } else if (step.selector != null) {
                        // Клик по селектору (запасной вариант)
                        val js = """
                            (function() {
                                var element = document.querySelector('${step.selector}');
                                if (element) {
                                    console.log('Кликаю по элементу: ${step.selector}');
                                    element.click();
                                    return 'SUCCESS: Clicked on ' + element.tagName;
                                }
                                console.error('Элемент не найден: ${step.selector}');
                                return 'ERROR: Element not found';
                            })();
                        """.trimIndent()
                        webView.evaluateJavascript(js) { result ->
                            Log.d("AiTaskSolver", "Tap '${step.selector}' result: $result")
                        }
                    }
                }
                
                "input" -> {
                    if (step.value != null) {
                        if (step.x != null && step.y != null) {
                            // НАСТОЯЩИЙ клавиатурный ввод через события (как автокликер)
                            Log.d("AiTaskSolver", "Keyboard input: '${step.value}'")
                            
                            // Имитируем ввод каждого символа
                            step.value.forEach { char ->
                                val keyCode = char.code
                                
                                // KeyDown событие
                                webView.dispatchKeyEvent(
                                    android.view.KeyEvent(
                                        android.view.KeyEvent.ACTION_DOWN,
                                        android.view.KeyEvent.KEYCODE_UNKNOWN
                                    )
                                )
                                
                                // Через JavaScript триггерим события клавиатуры для каждого символа
                                val js = """
                                    (function() {
                                        var element = document.activeElement;
                                        if (element && (element.tagName === 'INPUT' || element.tagName === 'TEXTAREA')) {
                                            // ОЧИЩАЕМ поле только при первом символе
                                            if ('$char' === '${step.value.first()}') {
                                                element.value = '';
                                            }
                                            // Добавляем символ
                                            element.value = element.value + '$char';
                                            
                                            // Триггерим события для каждого символа
                                            element.dispatchEvent(new KeyboardEvent('keydown', { 
                                                key: '$char', 
                                                keyCode: $keyCode,
                                                bubbles: true 
                                            }));
                                            element.dispatchEvent(new KeyboardEvent('keypress', { 
                                                key: '$char', 
                                                keyCode: $keyCode,
                                                bubbles: true 
                                            }));
                                            element.dispatchEvent(new Event('input', { bubbles: true }));
                                            element.dispatchEvent(new KeyboardEvent('keyup', { 
                                                key: '$char', 
                                                keyCode: $keyCode,
                                                bubbles: true 
                                            }));
                                            
                                            return true;
                                        }
                                        return false;
                                    })();
                                """.trimIndent()
                                
                                webView.evaluateJavascript(js, null)
                                
                                // KeyUp событие
                                webView.dispatchKeyEvent(
                                    android.view.KeyEvent(
                                        android.view.KeyEvent.ACTION_UP,
                                        android.view.KeyEvent.KEYCODE_UNKNOWN
                                    )
                                )
                                
                                // Небольшая задержка между символами (реалистично)
                                android.os.SystemClock.sleep((30..80).random().toLong())
                            }
                            
                            // Финальное событие change после завершения ввода
                            val jsFinish = """
                                (function() {
                                    var element = document.activeElement;
                                    if (element && (element.tagName === 'INPUT' || element.tagName === 'TEXTAREA')) {
                                        element.dispatchEvent(new Event('change', { bubbles: true }));
                                        return 'SUCCESS: Typed "' + element.value + '"';
                                    }
                                    return 'ERROR: No active input';
                                })();
                            """.trimIndent()
                            
                            webView.evaluateJavascript(jsFinish) { result ->
                                Log.d("AiTaskSolver", "✓ Keyboard input '${step.value}' result: $result")
                            }
                        } else if (step.selector != null) {
                            // Ввод текста по селектору (запасной вариант)
                            val escapedValue = step.value.replace("'", "\\'").replace("\n", "\\n")
                            val js = """
                                (function() {
                                    var element = document.querySelector('${step.selector}');
                                    if (element) {
                                        console.log('Ввожу текст в элемент: ${step.selector}');
                                        element.value = '$escapedValue';
                                        element.dispatchEvent(new Event('input', { bubbles: true }));
                                        element.dispatchEvent(new Event('change', { bubbles: true }));
                                        return 'SUCCESS: Input set to ' + element.value;
                                    }
                                    console.error('Элемент не найден: ${step.selector}');
                                    return 'ERROR: Element not found';
                                })();
                            """.trimIndent()
                            webView.evaluateJavascript(js) { result ->
                                Log.d("AiTaskSolver", "Input '${step.selector}' = '${step.value}' result: $result")
                            }
                        }
                    }
                }
                
                "scroll" -> {
                    val scrollAmount = step.duration ?: 500
                    val direction = if (scrollAmount < 0) "вверх" else "вниз"
                    Log.d("AiTaskSolver", "Scroll $direction на ${kotlin.math.abs(scrollAmount)} px")
                    
                    // Реальный свайп через MotionEvent (как человек)
                    val startX = (webView.width / 2).toFloat()
                    // Если scroll отрицательный - свайпаем вверх (startY меньше, endY больше)
                    val startY = if (scrollAmount < 0) {
                        (webView.height * 0.3).toFloat()  // Начинаем снизу
                    } else {
                        (webView.height * 0.7).toFloat()  // Начинаем сверху
                    }
                    val endY = startY - scrollAmount.toFloat()
                    
                    val downTime = android.os.SystemClock.uptimeMillis()
                    
                    try {
                        // DOWN
                        val eventDown = android.view.MotionEvent.obtain(
                            downTime, downTime,
                            android.view.MotionEvent.ACTION_DOWN,
                            startX, startY, 0
                        )
                        webView.dispatchTouchEvent(eventDown)
                        eventDown.recycle()
                        
                        // MOVE (плавный свайп)
                        val steps = 20
                        for (i in 1..steps) {
                            val currentY = startY - (scrollAmount.toFloat() * i / steps)
                            val eventMove = android.view.MotionEvent.obtain(
                                downTime, downTime + (i * 10L),
                                android.view.MotionEvent.ACTION_MOVE,
                                startX, currentY, 0
                            )
                            webView.dispatchTouchEvent(eventMove)
                            eventMove.recycle()
                            android.os.SystemClock.sleep(10)
                        }
                        
                        // UP
                        val eventUp = android.view.MotionEvent.obtain(
                            downTime, downTime + 200,
                            android.view.MotionEvent.ACTION_UP,
                            startX, endY, 0
                        )
                        webView.dispatchTouchEvent(eventUp)
                        eventUp.recycle()
                        
                        Log.d("AiTaskSolver", "✓ Scroll выполнен: $scrollAmount px")
                    } catch (e: Exception) {
                        Log.e("AiTaskSolver", "Ошибка scroll: ${e.message}")
                    }
                }
                
                "wait" -> {
                    // Задержка обрабатывается вызывающим кодом
                    Log.d("AiTaskSolver", "Waiting ${step.duration}ms")
                }
                
                else -> {
                    Log.w("AiTaskSolver", "Unknown action: ${step.action}")
                }
            }
        }
    }
}
