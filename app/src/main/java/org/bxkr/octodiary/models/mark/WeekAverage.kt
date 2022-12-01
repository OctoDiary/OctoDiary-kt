package org.bxkr.octodiary.models.mark

data class WeekAverage(
    val isCurrentWeek: Boolean,
    val number: Int,
    val value: String?
)