package org.bxkr.octodiary.models.mark

data class AverageMarks(
    val averageMark: String?,
    val averageMarkByImportantWork: String?,
    val averageMarkMood: String,
    val averageMarkTrend: String,
    val averageMarkByImportantWorkTrend: String,
    val indicator: Any?,
    val weightedAverageMark: Any?,
    val weightedAverageMarkTrend: String
)