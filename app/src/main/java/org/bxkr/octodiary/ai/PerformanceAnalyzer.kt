package org.bxkr.octodiary.ai

import android.content.Context
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.models.ai.StudentPerformance
import org.bxkr.octodiary.models.ai.StudyAdvice
import org.json.JSONArray
import org.json.JSONObject

/**
 * Анализатор успеваемости и энергии ученика
 */
object PerformanceAnalyzer {
    
    /**
     * Получить текущую успеваемость ученика
     */
    fun getCurrentPerformance(): StudentPerformance? {
        if (!DataService.hasMarksSubject) return null
        
        val allMarks = mutableListOf<Pair<String, Double>>()
        for (subject in DataService.marksSubject) {
            val currentPeriod = subject.currentPeriod
            if (currentPeriod != null) {
                for (mark in currentPeriod.marks) {
                    val value = mark.value?.toDoubleOrNull()
                    if (value != null) {
                        allMarks.add(subject.subjectName to value)
                    }
                }
            }
        }
        
        if (allMarks.isEmpty()) return null
        
        val averageGrade = allMarks.map { it.second }.average()
        
        val subjectGrades = mutableMapOf<String, Double>()
        val subjectMarksList = allMarks.groupBy { it.first }
        for ((subject, marks) in subjectMarksList) {
            val avg = marks.map { it.second }.average()
            subjectGrades[subject] = avg
        }
        
        val weakSpots = subjectGrades.filter { it.value < 4.0 }.keys.toList()
        val strengths = subjectGrades.filter { it.value >= 4.5 }.keys.toList()
        
        return StudentPerformance(
            averageGrade = averageGrade,
            subjectGrades = subjectGrades,
            weakSpots = weakSpots,
            strengths = strengths
        )
    }
    
    /**
     * Анализ энергии/прокрастинации через AI
     */
    suspend fun analyzeEnergyAndMotivation(
        context: Context,
        performance: StudentPerformance,
        homeworkCount: Int,
        completedHomeworkCount: Int
    ): Result<List<StudyAdvice>> {
        val prompt = """
            Проанализируй состояние ученика и дай советы. Верни JSON массив:
            [
              {
                "type": "energy|procrastination|motivation",
                "title": "Заголовок",
                "description": "Описание",
                "tips": ["совет1", "совет2", "совет3"]
              }
            ]
            
            Статистика:
            - Средний балл: ${"%.1f".format(performance.averageGrade)}
            - Слабые предметы: ${performance.weakSpots.joinToString()}
            - Сильные предметы: ${performance.strengths.joinToString()}
            - Домашних заданий: $homeworkCount
            - Выполнено: $completedHomeworkCount
            
            Дай 2-3 практических совета с учётом загруженности и успеваемости.
        """.trimIndent()
        
        return try {
            val result = GeminiService.sendMessage(context, prompt)
            result.map { response ->
                val jsonArray = JSONArray(response.substringAfter("[").substringBeforeLast("]").let { "[$it]" })
                val advices = mutableListOf<StudyAdvice>()
                
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    advices.add(
                        StudyAdvice(
                            type = obj.getString("type"),
                            title = obj.getString("title"),
                            description = obj.getString("description"),
                            tips = obj.getJSONArray("tips").let { arr ->
                                (0 until arr.length()).map { arr.getString(it) }
                            }
                        )
                    )
                }
                
                advices
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Проверить риск выгорания
     */
    fun checkBurnoutRisk(
        homeworkCount: Int,
        averageGrade: Double,
        recentGrades: List<Double>
    ): String? {
        // Много ДЗ + низкие оценки = риск выгорания
        if (homeworkCount > 5 && averageGrade < 3.5) {
            return "Высокая нагрузка и низкие оценки. Возможно выгорание."
        }
        
        // Падение оценок
        if (recentGrades.size >= 5) {
            val trend = recentGrades.takeLast(3).average() - recentGrades.take(2).average()
            if (trend < -0.5) {
                return "Оценки падают. Нужен отдых или помощь."
            }
        }
        
        return null
    }
}
