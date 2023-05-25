package org.bxkr.octodiary.models.subject

data class Mark(
    val categories: List<Category>,
    val criteriaMarkType: String,
    val date: Int,
    val isFinal: Boolean,
    val isImportant: Boolean,
    val isNew: Boolean,
    val lesson: Lesson,
    val markType: String,
    val markTypeText: String,
    val marks: List<org.bxkr.octodiary.models.shared.Mark>,
    val shortMarkTypeText: String?,
    val subject: SubjectX
)