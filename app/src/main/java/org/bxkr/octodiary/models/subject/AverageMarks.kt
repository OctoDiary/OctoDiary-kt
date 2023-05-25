package org.bxkr.octodiary.models.subject

data class AverageMarks(
    val averageMark: String?,
    val averageMarkByImportantWork: Any?,
    val averageMarkMood: String,
    val averageMarkTrend: String,
    val averagemarkByImportantWorkTrend: String,
    val indicator: Any?,
    val weightedAverageMark: String?,
    val weightedAverageMarkTrend: String
)