package org.bxkr.octodiary.models.lesson

import org.bxkr.octodiary.models.shared.Mark

data class LessonDetailsMarks(
    val isFinal: Boolean,
    val isImportant: Boolean,
    val markShortTypeText: String,
    val markType: String,
    val markTypeText: String,
    val marks: List<Mark>,
    val periodId: Long,
    val periodNumber: Int,
    val periodType: String
)

