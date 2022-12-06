package org.bxkr.octodiary.models.lesson

data class Lesson(
    val status: String,
    val subject: Subject,
    val startTime: Int,
    val endTime: Int,
    val hours: Hours,
    val number: Int,
    val theme: String,
    val teacher: Teacher,
    val importantWorks: List<String>,
    val averageMarks: AverageMarks,
    val lessonDetailsMarks: List<LessonDetailsMarks>,
    val homework: Homework,
    val attachments: List<Attachments>,
    val lessonDescription: String,
    val type: String,
    val description: String,
    val mobileSubscriptionStatus: String
)
