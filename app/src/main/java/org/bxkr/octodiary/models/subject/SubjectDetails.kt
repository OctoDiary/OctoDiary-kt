package org.bxkr.octodiary.models.subject

data class SubjectDetails(
    val description: String,
    val groupReportsPlot: GroupReportsPlot,
    val marks: List<Mark>,
    val messengerEntryPoint: Any?,
    val mobileSubscriptionStatus: String,
    val period: Period,
    val rating: Rating,
    val reportsPlot: ReportsPlot,
    val subject: SubjectXXX,
    val type: String
)