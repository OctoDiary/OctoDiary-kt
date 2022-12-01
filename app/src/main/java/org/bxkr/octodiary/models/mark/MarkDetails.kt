package org.bxkr.octodiary.models.mark

data class MarkDetails(
    val averageMarks: AverageMarks,
    val categories: List<Category>,
    val date: Int,
    val description: String,
    val groupReportsPlot: GroupReportsPlot,
    val lessonId: Long,
    val markDetails: MarkDetailsX,
    val messengerEntryPoint: Any?,
    val mobileSubscriptionStatus: String,
    val period: Period,
    val reportsPlot: ReportsPlot,
    val studentPlace: StudentPlace,
    val subject: Subject,
    val summativeMarks: Any?,
    val type: String
)