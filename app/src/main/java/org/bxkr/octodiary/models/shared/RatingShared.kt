package org.bxkr.octodiary.models.shared

data class RatingShared(
    val rankingPlaces: List<RankingPlace>,
    val rankingPosition: RankingPosition
)