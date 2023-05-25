package org.bxkr.octodiary.models.subject

data class WeekAverage(
    val isCurrentWeek: Boolean,
    val number: Int,
    val value: String?
)