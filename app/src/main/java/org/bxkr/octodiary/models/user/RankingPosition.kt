package org.bxkr.octodiary.models.user

data class RankingPosition(
    val backgroundImageUrl: String,
    val description: String,
    val place: Int,
    val placeTrend: String,
    val trendDescription: String
)