package org.bxkr.octodiary.models.subject

import org.bxkr.octodiary.models.shared.PeriodType

data class Period(
    val dateFinish: Int,
    val dateStart: Int,
    val id: Long,
    val isCurrent: Boolean,
    val number: Int,
    val studyYear: Int,
    val type: PeriodType
)