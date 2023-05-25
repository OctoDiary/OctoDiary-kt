package org.bxkr.octodiary.models.subject

data class GroupReportsPlot(
    val averageMarks: AverageMarks,
    val knowledgeAreaGroup: String,
    val knowledgeAreaGroupName: String,
    val plotOptions: PlotOptions,
    val reportingPeriodsReports: List<ReportingPeriodsReport>,
    val subject: Subject
)