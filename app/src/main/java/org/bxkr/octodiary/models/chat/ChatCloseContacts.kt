package org.bxkr.octodiary.models.chat

data class ChatCloseContacts(
    val contacts: List<Contact>,
    val description: String,
    val mobileSubscriptionStatus: String,
    val type: String
)