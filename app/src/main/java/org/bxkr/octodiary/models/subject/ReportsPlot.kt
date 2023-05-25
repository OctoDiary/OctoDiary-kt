package org.bxkr.octodiary.models.subject

data class ReportsPlot(
    val averageMarks: AverageMarks,
    val knowledgeAreaGroup: String,
    val knowledgeAreaGroupName: String,
    val plotOptions: PlotOptionsX,
    val reportingPeriodsReports: List<ReportingPeriodsReportX>,
    val subject: SubjectX
)