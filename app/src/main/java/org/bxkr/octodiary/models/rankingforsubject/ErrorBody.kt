package org.bxkr.octodiary.models.rankingforsubject

data class ErrorBody(
    val code: Int,
    val message: String,
    val errorText: String
)
