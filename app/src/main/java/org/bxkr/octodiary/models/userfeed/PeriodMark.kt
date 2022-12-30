package org.bxkr.octodiary.models.userfeed

data class PeriodMark(
    val content: Content,
    val timeStamp: Int,
    val type: FeedType
)