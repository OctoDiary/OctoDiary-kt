package org.bxkr.octodiary.models.periodmarks

data class PeriodMark(
    val averageMarks: AverageMarks,
    val finalMark: Any?,
    val rankingPlace: Int?,
    val recentMarks: List<RecentMark>,
    val subject: Subject,
    val summativeMarks: Any?
)