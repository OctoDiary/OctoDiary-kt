package org.bxkr.octodiary.models.mark

data class AverageMarksX(
    val averageMark: String,
    val averageMarkMood: String,
    val averageMarkTrend: String,
    val indicator: Any?,
    val weightedAverageMark: Any?,
    val weightedAverageMarkTrend: String
)