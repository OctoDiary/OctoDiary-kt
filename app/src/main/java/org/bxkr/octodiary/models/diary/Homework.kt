package org.bxkr.octodiary.models.diary

data class Homework(
    val attachments: List<Any>,
    val isCompleted: Boolean,
    val text: String,
    val workIsAttachRequired: Boolean
)