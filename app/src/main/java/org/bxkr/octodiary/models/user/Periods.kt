package org.bxkr.octodiary.models.user

data class Periods(
    val id: Long,
    val number: Int,
    val type: String,
    val studyYear: Int,
    val isCurrent: Boolean,
    val dateStart: Long,
    val dateFinish: Long
)