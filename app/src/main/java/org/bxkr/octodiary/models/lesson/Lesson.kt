package org.bxkr.octodiary.models.lesson

import org.bxkr.octodiary.models.shared.File

data class Lesson(
    val status: String,
    val subject: Subject,
    val startTime: Int,
    val endTime: Int,
    val hours: Hours,
    val number: Int,
    val theme: String,
    val teacher: Teacher,
    val importantWorks: List<ImportantWork>,
    val averageMarks: AverageMarks,
    val lessonDetailsMarks: List<LessonDetailsMarks>,
    val homework: Homework?,
    val attachments: List<File>,
    val lessonDescription: String,
    val type: String,
    val description: String,
    val mobileSubscriptionStatus: String
)
