package org.bxkr.octodiary.models.userfeed

data class Item(
    val averageMarks: AverageMarks,
    val date: Int,
    val messengerEntryPoint: Any?,
    val subject: SubjectX,
    val subjectMarks: List<SubjectMark>
)