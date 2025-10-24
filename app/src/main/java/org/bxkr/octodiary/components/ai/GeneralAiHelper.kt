package org.bxkr.octodiary.components.ai

import android.content.Context
import org.bxkr.octodiary.managers.TextbookManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Генератор системного промпта для общего AI помощника
 */
object GeneralAiHelper {
    
    fun generateSystemPrompt(context: Context): String {
        
        return buildString {
            appendLine("Ты - персональный AI помощник ученика в приложении OctoDiary.")
            appendLine()
            appendLine("**У тебя есть доступ к:**")
            appendLine()
            
            // Расписание
            appendLine("**📅 Расписание:**")
            appendLine("(здесь будет расписание на сегодня/завтра)")
            appendLine()
            
            // Домашние задания
            appendLine("**📝 Домашние задания:**")
            appendLine("(здесь будут ДЗ на сегодня и завтра)")
            appendLine()
            
            // Оценки
            appendLine("**📊 Оценки по всем предметам:**")
            appendLine("(здесь будет статистика оценок)")
            appendLine()
            
            // Учебники
            val textbooksContext = TextbookManager.getTextbooksContextForAI(context)
            if (textbooksContext.contains("Доступные учебники")) {
                appendLine(textbooksContext)
                appendLine()
            }
            
            appendLine("**Твоя задача:**")
            appendLine("1. Помогать с учёбой по ВСЕМ предметам")
            appendLine("2. Анализировать успеваемость и давать рекомендации")
            appendLine("3. Помогать планировать время на ДЗ")
            appendLine("4. Мотивировать и поддерживать ученика")
            appendLine("5. Создавать тесты по любым предметам (используй JSON формат)")
            appendLine("6. Отвечать на вопросы по учебникам")
            appendLine("7. Быть дружелюбным наставником")
            appendLine()
            appendLine("**Ты видишь ВСЮ картину учёбы ученика, используй это для персонализированных советов!**")
        }
    }
    
    fun generateQuickPrompts(): List<String> {
        return listOf(
            "Как дела с учёбой?",
            "Что сегодня по расписанию?",
            "Какие ДЗ на завтра?",
            "Покажи мою статистику оценок",
            "Создай план подготовки к неделе",
            "Посоветуй что повторить"
        )
    }
}
