package org.bxkr.octodiary.models.userfeed

import org.bxkr.octodiary.models.shared.Mark

data class RecentMark(
    val categories: List<Category>,
    val criteriaMarkType: String,
    val date: Int,
    val indicator: Any?,
    val isFinal: Boolean,
    val isImportant: Boolean,
    val isNew: Boolean,
    val lesson: Lesson,
    val markType: String,
    val markTypeText: String,
    val marks: List<Mark>,
    val shortMarkTypeText: String,
    val subject: SubjectX
)