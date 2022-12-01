package org.bxkr.octodiary.models.mark

data class ReportingPeriodsReport(
    val isCurrent: Boolean,
    val periodId: Long,
    val periodNumber: Int,
    val periodType: String,
    val weekAverages: List<WeekAverage>
)