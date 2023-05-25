package org.bxkr.octodiary.models.diary

data class Lesson(
    val comment: Any?,
    val endDateTime: String?,
    val group: Group?,
    val hasAttachment: Boolean?,
    val homework: Homework?,
    val id: Long,
    val importantWorks: List<String>?,
    val isCanceled: Boolean?,
    val isEmpty: Any?,
    val number: Int?,
    val place: Any?,
    val startDateTime: String?,
    val subject: Subject,
    val teacher: Teacher?,
    val theme: String,
    val workMarks: List<WorkMark>
)