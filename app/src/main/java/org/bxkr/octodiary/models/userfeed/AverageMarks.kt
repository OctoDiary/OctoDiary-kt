package org.bxkr.octodiary.models.userfeed

data class AverageMarks(
    val averageMark: String,
    val averageMarkByImportantWork: String?,
    val averageMarkMood: String,
    val averageMarkTrend: String,
    val averagemarkByImportantWorkTrend: String,
    val indicator: Any?,
    val weightedAverageMark: Any?,
    val weightedAverageMarkTrend: String
)