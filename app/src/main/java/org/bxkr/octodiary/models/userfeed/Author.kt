package org.bxkr.octodiary.models.userfeed

data class Author(
    val avatarUrl: String,
    val firstName: String,
    val jid: String,
    val lastName: String,
    val middleName: Any?,
    val userId: Long
)