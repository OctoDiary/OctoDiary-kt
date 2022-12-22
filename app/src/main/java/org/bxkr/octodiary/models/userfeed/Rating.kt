package org.bxkr.octodiary.models.userfeed

data class Rating(
    val rankingPlaces: List<RankingPlace>,
    val rankingPosition: RankingPosition
)