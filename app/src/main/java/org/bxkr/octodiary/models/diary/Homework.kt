package org.bxkr.octodiary.models.diary

import org.bxkr.octodiary.models.shared.File

data class Homework(
    val attachments: List<File>,
    val isCompleted: Boolean,
    val text: String,
    val workIsAttachRequired: Boolean
)