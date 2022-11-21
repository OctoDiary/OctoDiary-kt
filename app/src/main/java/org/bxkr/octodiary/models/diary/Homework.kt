package org.bxkr.octodiary.models.diary

import org.bxkr.octodiary.models.lesson.Attachment

data class Homework(
    val attachments: List<Attachment>,
    val isCompleted: Boolean,
    val text: String,
    val workIsAttachRequired: Boolean
)