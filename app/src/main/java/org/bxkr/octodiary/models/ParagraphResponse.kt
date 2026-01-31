package org.bxkr.octodiary.models


import androidx.compose.material.icons.Icons
data class ParagraphResponse(
    val requestId: String,
    val title: String,
    val content: String,
    val source: String,
    val language: String,
    val wordCount: Int,
    val estimatedReadTime: Int,
    val tags: List<String> = emptyList(),
    val success: Boolean,
    val errorMessage: String? = null
)


