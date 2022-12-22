package org.bxkr.octodiary.models.userfeed

data class Schedule(
    val nextLessonDate: Any?,
    val todayLessons: List<TodayLesson>,
    val tomorrowLessons: List<Any>
)