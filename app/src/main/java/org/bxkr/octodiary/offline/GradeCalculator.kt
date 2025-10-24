package org.bxkr.octodiary.offline

import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * Локальный калькулятор оценок без использования API.
 * Вычисляет средний балл, GPA и прогнозы на основе локальных данных.
 */
object GradeCalculator {

    /**
     * Вычисляет средний балл по списку оценок.
     */
    fun calculateAverageGrade(grades: List<Int>): Double {
        if (grades.isEmpty()) return 0.0
        return grades.average()
    }

    /**
     * Вычисляет средний балл по предмету.
     */
    fun calculateSubjectAverage(subjectGrades: Map<String, List<Int>>): Map<String, Double> {
        return subjectGrades.mapValues { calculateAverageGrade(it.value) }
    }

    /**
     * Вычисляет GPA по стандартной шкале (4.0).
     * Шкала: 5=A, 4=B, 3=C, 2=D, 1=F
     */
    fun calculateGPA(grades: List<Int>): Double {
        if (grades.isEmpty()) return 0.0
        val points = grades.map { grade ->
            when (grade) {
                5 -> 4.0
                4 -> 3.0
                3 -> 2.0
                2 -> 1.0
                else -> 0.0
            }
        }
        return points.average()
    }

    /**
     * Прогноз финальной оценки на основе текущих оценок.
     * Использует линейную регрессию для прогноза.
     */
    fun predictFinalGrade(currentGrades: List<Int>, targetGrade: Int): Double {
        if (currentGrades.isEmpty()) return targetGrade.toDouble()

        val average = calculateAverageGrade(currentGrades)
        val trend = currentGrades.zipWithNext { a, b -> b - a }.average()

        // Простая модель прогноза: текущий средний + тренд
        val predicted = average + trend * 2 // Предполагаем 2 оставшиеся оценки

        return predicted.coerceIn(1.0, 5.0)
    }

    /**
     * Анализ прогресса по предметам.
     */
    data class ProgressReport(
        val subject: String,
        val currentAverage: Double,
        val trend: Double, // Изменение оценки за период
        val prediction: Double,
        val recommendation: String
    )

    fun generateProgressReport(
        subject: String,
        grades: List<Int>,
        dates: List<LocalDate>
    ): ProgressReport {
        val currentAverage = calculateAverageGrade(grades)
        val trend = calculateTrend(grades, dates)
        val prediction = predictFinalGrade(grades, 4) // Цель - 4

        val recommendation = when {
            trend > 0.5 -> "Отличная динамика! Продолжайте в том же духе."
            trend > 0 -> "Успехи растут, но можно улучшить."
            trend > -0.5 -> "Стабильные результаты, есть потенциал для роста."
            else -> "Нужна дополнительная работа над предметом."
        }

        return ProgressReport(subject, currentAverage, trend, prediction, recommendation)
    }

    /**
     * Вычисляет тренд оценок по времени.
     */
    private fun calculateTrend(grades: List<Int>, dates: List<LocalDate>): Double {
        if (grades.size < 2 || dates.size != grades.size) return 0.0

        val sortedPairs = dates.zip(grades).sortedBy { it.first }
        val recent = sortedPairs.takeLast(minOf(5, sortedPairs.size))
        val earlier = sortedPairs.take(sortedPairs.size - recent.size)

        if (earlier.isEmpty()) return 0.0

        val recentAvg = recent.map { it.second }.average()
        val earlierAvg = earlier.map { it.second }.average()

        return recentAvg - earlierAvg
    }

    /**
     * Генерирует сводный отчёт по всем предметам.
     */
    fun generateSummaryReport(
        allGrades: Map<String, List<Int>>,
        allDates: Map<String, List<LocalDate>>
    ): SummaryReport {
        val subjectReports = allGrades.map { (subject, grades) ->
            val dates = allDates[subject] ?: emptyList()
            generateProgressReport(subject, grades, dates)
        }

        val overallAverage = subjectReports.map { it.currentAverage }.average()
        val overallGPA = calculateGPA(allGrades.values.flatten())

        val bestSubject = subjectReports.maxByOrNull { it.currentAverage }
        val needsImprovement = subjectReports.filter { it.currentAverage < 3.5 }

        return SummaryReport(
            overallAverage,
            overallGPA,
            subjectReports,
            bestSubject?.subject ?: "",
            needsImprovement.map { it.subject }
        )
    }

    data class SummaryReport(
        val overallAverage: Double,
        val overallGPA: Double,
        val subjectReports: List<ProgressReport>,
        val bestSubject: String,
        val subjectsNeedingImprovement: List<String>
    )
}