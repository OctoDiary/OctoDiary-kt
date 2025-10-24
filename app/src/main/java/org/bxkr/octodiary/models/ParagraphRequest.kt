package org.bxkr.octodiary.models

data class ParagraphRequest(
    val topic: String,
    val language: String = "ru",
    val maxLength: Int? = null,
    val context: String? = null,
    val preferredSource: String? = null
)