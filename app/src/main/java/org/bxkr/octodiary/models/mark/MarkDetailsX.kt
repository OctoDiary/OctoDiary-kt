package org.bxkr.octodiary.models.mark

data class MarkDetailsX(
    val isFinal: Boolean,
    val isImportant: Boolean,
    val markType: String,
    val markTypeText: String,
    val marks: List<Mark>
)