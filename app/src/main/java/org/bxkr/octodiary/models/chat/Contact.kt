package org.bxkr.octodiary.models.chat

data class Contact(
    val avatar: String?,
    val avatarBackground: Any?,
    val classTeacher: String?,
    val created: Boolean,
    val irrelevant: Boolean,
    val isCloseContact: Boolean,
    val isGroupChat: Boolean,
    val isSystem: Boolean,
    val jid: String,
    val name: String,
    val senderShortName: Any?,
    val sex: String?,
    val shortName: String,
    val type: String,
    val unknown: Boolean
)