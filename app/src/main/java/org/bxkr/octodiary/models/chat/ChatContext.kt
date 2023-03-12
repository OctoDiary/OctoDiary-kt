package org.bxkr.octodiary.models.chat

data class ChatContext(
    val description: String,
    val mobileSubscriptionStatus: String,
    val mongooseBoshHost: String,
    val mongooseTCPHost: String,
    val mongooseWSHost: String,
    val multiUserChatHost: String,
    val type: String
)
