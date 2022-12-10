package org.bxkr.octodiary.models.diary

import org.bxkr.octodiary.models.shared.Mark

data class WorkMark(
    val marks: List<Mark>,
    val workId: Long
)