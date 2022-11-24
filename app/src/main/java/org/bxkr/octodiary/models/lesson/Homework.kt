package org.bxkr.octodiary.models.lesson

data class Homework(
    val workIsAttachRequired: Boolean,
    val attachments: List<String>,
    val text: String,
    val isCompleted: Boolean
)