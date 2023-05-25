package org.bxkr.octodiary.models.user

import org.bxkr.octodiary.models.shared.PeriodType

data class Periods(
    val id: Long,
    val number: Int,
    val type: PeriodType,
    val studyYear: Int,
    val isCurrent: Boolean,
    val dateStart: Long,
    val dateFinish: Long
)