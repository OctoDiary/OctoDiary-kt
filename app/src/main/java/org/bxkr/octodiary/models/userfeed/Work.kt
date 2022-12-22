package org.bxkr.octodiary.models.userfeed

data class Work(
    val averageMarks: AverageMarksX?,
    val date: Int,
    val knowledgeArea: String,
    val lessonId: Long,
    val lessonNumber: Int,
    val messengerEntryPoint: Any?,
    val subject: SubjectX,
    val summativeMarks: Any?,
    val workTypeName: String
)