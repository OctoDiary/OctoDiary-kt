package org.bxkr.octodiary.models.shared

data class Mark(
    val id: Long,
    val mood: MarkMood?,
    val value: String
)