package org.bxkr.octodiary.models.diary

data class Day(
    val date: String,
    val dayHomeworksProgress: Any,
    val hasImportantWork: Boolean,
    val lessons: List<Lesson>,
    val messengerEntryPoint: Any
)