package org.bxkr.octodiary.models


import androidx.compose.material.icons.Icons
data class ParagraphRequest(
    val topic: String,
    val language: String = "ru",
    val maxLength: Int? = null,
    val context: String? = null,
    val preferredSource: String? = null
)


