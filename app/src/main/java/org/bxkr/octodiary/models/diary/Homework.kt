package org.bxkr.octodiary.models.diary

import org.bxkr.octodiary.models.lesson.Attachments

data class Homework(
    val attachments: List<Attachments>,
    val isCompleted: Boolean,
    val text: String,
    val workIsAttachRequired: Boolean
)