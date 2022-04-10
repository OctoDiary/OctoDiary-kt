package org.bxkr.octodiary.models

data class Diary(
    val weeks: List<Week>
)

data class Week(
    val days: List<Day>,
    val firstWeekDayDate: String,
    val homeworksCount: Int,
    val id: String,
    val lastWeekDayDate: String
)

data class Day(
    val date: String,
    val dayHomeworksProgress: DayHomeworksProgress,
    val hasImportantWork: Boolean,
    val lessons: List<Lesson>,
    val messengerEntryPoint: Any
)

data class DayHomeworksProgress(
    val completedLessonsWithHomeworksCount: Int,
    val totalLessonsWithHomeworksCount: Int
)

data class Lesson(
    val comment: Any,
    val endDateTime: String,
    val group: Group,
    val hasAttachment: Boolean,
    val homework: Homework,
    val id: Long,
    val importantWorks: List<String>,
    val isCanceled: Boolean,
    val isEmpty: Any,
    val number: Int,
    val place: Any,
    val startDateTime: String,
    val subject: Subject,
    val teacher: Teacher,
    val theme: String,
    val workMarks: List<WorkMark>
)

data class Group(
    val id: Long,
    val name: String,
    val parentId: Any,
    val parentName: Any
)

data class Homework(
    val attachments: List<Any>,
    val isCompleted: Boolean,
    val text: String,
    val workIsAttachRequired: Boolean
)

data class Subject(
    val id: Long,
    val knowledgeArea: String,
    val name: String,
    val subjectMood: Any
)

data class Teacher(
    val avatarUrl: String,
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val personId: Int
)

data class WorkMark(
    val marks: List<Mark>,
    val workId: Long
)

data class Mark(
    val id: Long,
    val mood: String,
    val value: String
)
