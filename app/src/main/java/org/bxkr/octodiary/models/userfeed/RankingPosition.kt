package org.bxkr.octodiary.models.userfeed

data class RankingPosition(
    val place: Int,
    val placeTrend: String,
    val trendDescription: String
)