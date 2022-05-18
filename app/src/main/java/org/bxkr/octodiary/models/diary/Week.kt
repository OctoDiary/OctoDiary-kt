package org.bxkr.octodiary.models.diary

data class Week(
    val days: List<Day>,
    val firstWeekDayDate: String,
    val homeworksCount: Int,
    val id: String,
    val lastWeekDayDate: String
)