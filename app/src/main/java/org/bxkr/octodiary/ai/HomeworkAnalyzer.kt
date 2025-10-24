package org.bxkr.octodiary.ai

import android.content.Context
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.models.homeworks2.Homework
import org.bxkr.octodiary.models.ai.HomeworkDifficulty
import org.bxkr.octodiary.models.ai.StudyPlan
import org.bxkr.octodiary.models.ai.StudyTask
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Анализатор домашних заданий
 * Определяет сложность, составляет план
 */
object HomeworkAnalyzer {
    
    /**
     * Анализировать сложность ДЗ через AI
     */
    suspend fun analyzeDifficulty(
        context: Context,
        homework: Homework
    ): Result<HomeworkDifficulty> {
        val prompt = """
            Проанализируй сложность домашнего задания и верни JSON:
            {
              "difficulty": 1-5,
              "estimatedTimeMinutes": число,
              "requiredKnowledge": ["тема1", "тема2"],
              "tips": ["совет1", "совет2"]
            }
            
            Предмет: ${homework.subjectName}
            Задание: ${homework.description}
            Описание: ${homework.homework}
        """.trimIndent()
        
        return try {
            val result = GeminiService.sendMessage(context, prompt)
            result.map { response ->
                val json = JSONObject(response.substringAfter("{").substringBeforeLast("}").let { "{$it}" })
                HomeworkDifficulty(
                    homeworkId = homework.homeworkEntryStudentId,
                    difficulty = json.getInt("difficulty"),
                    estimatedTimeMinutes = json.getInt("estimatedTimeMinutes"),
                    requiredKnowledge = json.getJSONArray("requiredKnowledge").let { arr ->
                        (0 until arr.length()).map { arr.getString(it) }
                    },
                    tips = json.getJSONArray("tips").let { arr ->
                        (0 until arr.length()).map { arr.getString(it) }
                    }
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Составить персональный план учёбы
     */
    suspend fun generateStudyPlan(
        context: Context,
        homeworks: List<Homework>,
        bedTime: String = "21:00",
        currentTime: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    ): Result<StudyPlan> {
        // Получаем средний балл ученика
        val averageGrade = if (DataService.hasMarksSubject) {
            val allMarks = mutableListOf<Double>()
            for (subject in DataService.marksSubject) {
                val currentPeriod = subject.currentPeriod
                if (currentPeriod != null) {
                    for (mark in currentPeriod.marks) {
                        val value = mark.value?.toDoubleOrNull()
                        if (value != null) {
                            allMarks.add(value)
                        }
                    }
                }
            }
            if (allMarks.isNotEmpty()) allMarks.average() else 4.0
        } else {
            4.0
        }
        
        val homeworksInfo = homeworks.joinToString("\n") { hw ->
            "- ${hw.subjectName}: ${hw.description}"
        }
        
        val prompt = """
            Составь персональный план выполнения домашних заданий.
            
            **ВАЖНО: Верни ТОЛЬКО JSON массив, без дополнительного текста!**
            
            Формат:
            [
              {
                "homeworkId": ${homeworks.firstOrNull()?.homeworkEntryStudentId ?: 0},
                "startTime": "14:30",
                "estimatedMinutes": 45,
                "priority": 5
              }
            ]
            
            **Контекст:**
            - Текущее время: $currentTime
            - Время отхода ко сну: $bedTime
            - Средний балл ученика: ${"%.1f".format(averageGrade)}
            
            **Домашние задания (используй ID из списка):**
            ${homeworks.joinToString("\n") { "- ID: ${it.homeworkEntryStudentId}, ${it.subjectName}: ${it.description}" }}
            
            **Правила:**
            1. Сложные задания - в начале
            2. Чередуй предметы
            3. Делай перерывы каждые 45 минут
            4. Оставь время на отдых перед сном
            
            Верни ТОЛЬКО валидный JSON массив!
        """.trimIndent()
        
        return try {
            val result = GeminiService.sendMessage(context, prompt)
            result.map { response ->
                android.util.Log.d("HomeworkAnalyzer", "Plan response: $response")
                
                // Извлекаем JSON из ответа
                val jsonText = when {
                    response.contains("[") && response.contains("]") -> {
                        val start = response.indexOf("[")
                        val end = response.lastIndexOf("]") + 1
                        response.substring(start, end)
                    }
                    else -> {
                        android.util.Log.e("HomeworkAnalyzer", "JSON не найден в ответе")
                        "[]"
                    }
                }
                
                val jsonArray = JSONArray(jsonText)
                val tasks = mutableListOf<StudyTask>()
                
                for (i in 0 until jsonArray.length()) {
                    try {
                        val obj = jsonArray.getJSONObject(i)
                        val hwId = obj.getLong("homeworkId")
                        val hw = homeworks.find { it.homeworkEntryStudentId == hwId }
                        
                        if (hw != null) {
                            tasks.add(
                                StudyTask(
                                    homeworkId = hwId,
                                    subjectName = hw.subjectName,
                                    description = hw.description,
                                    estimatedMinutes = obj.getInt("estimatedMinutes"),
                                    priority = obj.getInt("priority"),
                                    startTime = obj.getString("startTime")
                            )
                        )
                    }
                    } catch (e: Exception) {
                        android.util.Log.e("HomeworkAnalyzer", "Ошибка парсинга задачи $i", e)
                    }
                }
                
                StudyPlan(
                    date = System.currentTimeMillis(),
                    tasks = tasks,
                    bedTime = bedTime,
                    totalEstimatedMinutes = tasks.sumOf { it.estimatedMinutes }
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
