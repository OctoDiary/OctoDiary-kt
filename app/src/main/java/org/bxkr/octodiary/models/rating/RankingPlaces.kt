package org.bxkr.octodiary.models.rating

data class RankingPlaces(
    val place: Int,
    val imageUrl: String,
    val averageMark: String,
    val isContextUser: Boolean,
    val trend: String
)