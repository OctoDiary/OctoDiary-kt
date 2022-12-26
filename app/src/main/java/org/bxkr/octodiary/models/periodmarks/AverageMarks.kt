package org.bxkr.octodiary.models.periodmarks

data class AverageMarks(
    val averageMark: String?,
    val averageMarkByImportantWork: String?,
    val averageMarkMood: String,
    val averageMarkTrend: String,
    val averagemarkByImportantWorkTrend: String,
    val indicator: Any?,
    val weightedAverageMark: Any?,
    val weightedAverageMarkTrend: String
)