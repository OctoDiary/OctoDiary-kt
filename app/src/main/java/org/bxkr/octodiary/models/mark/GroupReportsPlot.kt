package org.bxkr.octodiary.models.mark

data class GroupReportsPlot(
    val averageMarks: AverageMarksX,
    val knowledgeAreaGroup: String,
    val knowledgeAreaGroupName: String,
    val plotOptions: PlotOptions,
    val reportingPeriodsReports: List<ReportingPeriodsReport>,
    val subject: Subject
)