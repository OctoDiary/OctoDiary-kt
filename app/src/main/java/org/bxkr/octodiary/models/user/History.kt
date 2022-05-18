package org.bxkr.octodiary.models.user

data class History(
    val historyItems: List<HistoryItem>,
    val rankingPosition: RankingPosition
)