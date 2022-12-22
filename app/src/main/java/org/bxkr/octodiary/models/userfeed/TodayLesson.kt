package org.bxkr.octodiary.models.userfeed

data class TodayLesson(
    val endTime: Int,
    val hours: Hours,
    val id: Long,
    val number: Int,
    val place: Any?,
    val startTime: Int,
    val subject: SubjectX
)