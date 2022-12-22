package org.bxkr.octodiary.models.userfeed

data class Feed(
    val content: Content,
    val timeStamp: Int,
    val type: String
)