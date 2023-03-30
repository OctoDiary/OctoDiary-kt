package org.bxkr.octodiary.models.user

data class ContextPersons(
    val sex: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val avatarUrl: String?,
    val school: School,
    val group: Group,
    val reportingPeriodGroup: ReportingPeriodGroup,
    val userId: Long,
    val personId: Long
)