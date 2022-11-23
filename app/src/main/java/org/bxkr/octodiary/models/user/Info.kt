package org.bxkr.octodiary.models.user

data class Info(
    val sex: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val avatarUrl: String,
    val currentCultureCode: String,
    val userId: Long,
    val personId: Long
)