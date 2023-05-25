package org.bxkr.octodiary.models.subject

data class RankingPlace(
    val averageMark: String,
    val imageUrl: String,
    val isContextUser: Boolean,
    val place: Int,
    val trend: String
)