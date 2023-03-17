package org.bxkr.octodiary.models.chat

data class ChatCredentials(
    val attachmentHosts: List<String>,
    val description: String,
    val jid: String,
    val mobileSubscriptionStatus: String,
    val token: String,
    val type: String
)