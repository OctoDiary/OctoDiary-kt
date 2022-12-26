package org.bxkr.octodiary.models.periodmarks

import org.bxkr.octodiary.models.shared.Mark

data class RecentMark(
    val date: Int,
    val isNew: Boolean,
    val marks: List<Mark>
)