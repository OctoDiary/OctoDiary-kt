package org.bxkr.octodiary.models.shared

data class RankingPosition(
    val place: Int,
    val placeTrend: String,
    val trendDescription: String
)