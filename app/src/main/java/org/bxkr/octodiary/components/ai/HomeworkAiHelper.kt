package org.bxkr.octodiary.components.ai


import androidx.compose.material.icons.Icons
import android.content.Context
import org.bxkr.octodiary.managers.TextbookManager
import org.bxkr.octodiary.models.homeworks2.Homework

/**
 * Генератор системного промпта для помощи с ДЗ
 */
object HomeworkAiHelper {
    fun generateSystemPrompt(
        context: Context,
        homework: Homework,
        previousTopic: String? = null,
        nextTopic: String? = null,
        marks: List<Any>? = null,
        subjectMarksHistory: String? = null
    ): String {
        return buildString {
            appendLine("Ты - умный помощник для школьника.")
            appendLine()
            appendLine("**Контекст текущего домашнего задания:**")
            appendLine("- Предмет: ${homework.subjectName}")
            appendLine("- Задание: ${homework.description}")
            appendLine("- Полное описание: ${homework.homework}")
            
            if (previousTopic != null) {
                appendLine("- Предыдущая тема урока: $previousTopic")
            }
            
            if (nextTopic != null) {
                appendLine("- Следующая тема урока: $nextTopic")
            }
            
            if (subjectMarksHistory != null) {
                appendLine()
                appendLine("**История оценок ученика по этому предмету:**")
                appendLine(subjectMarksHistory)
            }
            
            if (homework.materials.isNotEmpty()) {
                appendLine()
                appendLine("**Прикреплённые материалы:**")
                homework.materials.forEach { material ->
                    appendLine("- ${material.title} (${material.typeName})")
                }
            }
            
            // Добавляем учебники
            val textbooksContext = TextbookManager.getTextbooksContextForAI(context, homework.subjectName)
            if (textbooksContext.isNotBlank()) {
                appendLine()
                appendLine(textbooksContext)
                appendLine("Ты можешь ссылаться на учебник при объяснении")
            }
            
            if (marks != null && marks.isNotEmpty()) {
                appendLine()
                appendLine("**Уровень знаний ученика (оценки):** $marks")
                appendLine("Адаптируй сложность объяснения: если много 2/3 - объясняй максимально подробно и просто. Если 4/5 - можно давать более сложные задания.")
            }
            
            appendLine()
            appendLine("**Твоя задача:**")
            appendLine("1. Помогай школьнику понять задание")
            appendLine("2. Объясняй решения пошагово, не давай готовый ответ сразу")
            appendLine("3. Проверяй правильность выполнения")
            appendLine("4. Давай подсказки, наводящие вопросы")
            appendLine("5. Отвечай на русском языке, используй дружелюбный тон и эмодзи 😊")
            appendLine("6. Говори просто, избегай канцелярита и заумных фраз")
            appendLine()
            appendLine("**ВАЖНО: Если ученик просит тест/викторину/проверочную работу:**")
            appendLine()
            appendLine("**ИСПОЛЬЗУЙ JSON ФОРМАТ для надёжности!**")
            appendLine()
            appendLine("**Типы вопросов:**")
            appendLine("1. **single_choice** - выбор из вариантов (любое количество вариантов)")
            appendLine("2. **text_input** - ввод текста")
            appendLine("3. **matching** - соединение пар")
            appendLine()
            appendLine("**JSON ФОРМАТ:**")
            appendLine("```json")
            appendLine("{")
            appendLine("  \"title\": \"Тест по алгебре\",")
            appendLine("  \"questions\": [")
            appendLine("    {")
            appendLine("      \"id\": 1,")
            appendLine("      \"type\": \"single_choice\",")
            appendLine("      \"text\": \"Найдите вершину параболы y = x² + 4x + 3\",")
            appendLine("      \"options\": [\"(-2, -1)\", \"(2, 15)\", \"(-4, 3)\", \"(0, 3)\"],")
            appendLine("      \"correctAnswer\": \"(-2, -1)\"")
            appendLine("    },")
            appendLine("    {")
            appendLine("      \"id\": 2,")
            appendLine("      \"type\": \"text_input\",")
            appendLine("      \"text\": \"Напишите формулу квадрата суммы\",")
            appendLine("      \"correctAnswer\": \"(a+b)² = a² + 2ab + b²\"")
            appendLine("    },")
            appendLine("    {")
            appendLine("      \"id\": 3,")
            appendLine("      \"type\": \"matching\",")
            appendLine("      \"text\": \"Соедините формулу с её названием\",")
            appendLine("      \"pairs\": [")
            appendLine("        [\"(a+b)²\", \"квадрат суммы\"],")
            appendLine("        [\"a² - b²\", \"разность квадратов\"],")
            appendLine("        [\"(a-b)²\", \"квадрат разности\"]")
            appendLine("      ]")
            appendLine("    }")
            appendLine("  ]")
            appendLine("}")
            appendLine("```")
            appendLine()
            appendLine("**После JSON добавь текст:**")
            appendLine("\"Ответы будут проверены AI, я дам подробную обратную связь и помогу разобраться с ошибками!\"")
            appendLine()
            appendLine("**Если ошибки - предложи пересдать похожий тест**")
        }
    }
    
    fun generateQuickPrompts(homework: Homework): List<String> {
        return when {
            homework.subjectName.contains("математ", ignoreCase = true) -> listOf(
                "Создай тест на 5 вопросов",
                "Объясни как решать эту задачу",
                "Проверь моё решение",
                "Какая формула здесь нужна?"
            )
            homework.subjectName.contains("русс", ignoreCase = true) -> listOf(
                "Создай тест на 5 вопросов",
                "Проверь грамматику",
                "Помоги с сочинением",
                "Объясни правило"
            )
            homework.subjectName.contains("англ", ignoreCase = true) ||
            homework.subjectName.contains("немец", ignoreCase = true) ||
            homework.subjectName.contains("франц", ignoreCase = true) -> listOf(
                "Создай тест на 5 вопросов",
                "Переведи текст",
                "Проверь грамматику",
                "Помоги с временами"
            )
            homework.subjectName.contains("физик", ignoreCase = true) -> listOf(
                "Создай тест на 5 вопросов",
                "Объясни явление",
                "Помоги с задачей",
                "Какую формулу использовать?"
            )
            homework.subjectName.contains("хим", ignoreCase = true) -> listOf(
                "Создай тест на 5 вопросов",
                "Помоги с уравнением",
                "Объясни реакцию",
                "Проверь баланс"
            )
            else -> listOf(
                "Создай тест на 5 вопросов",
                "Объясни тему",
                "Помоги разобраться",
                "Проверь ответ"
            )
        }
    }
}



