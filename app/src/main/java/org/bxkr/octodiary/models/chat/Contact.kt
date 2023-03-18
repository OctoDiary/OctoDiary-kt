package org.bxkr.octodiary.models.chat

data class Contact(
    val avatar: String?,
    val avatarBackground: String?,
    val classTeacher: String?,
    val created: Boolean,
    val irrelevant: Boolean,
    val isCloseContact: Boolean?,
    val isGroupChat: Boolean,
    val isSystem: Boolean,
    val jid: String,
    val name: String,
    val senderShortName: String?,
    val sex: String?,
    val shortName: String,
    val type: ContactType,
    val unknown: Boolean,
    var lastMessage: String?,
    var sender: String?
)