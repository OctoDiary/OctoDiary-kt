package org.bxkr.octodiary.models.periodmarks

data class Subject(
    val id: Long,
    val knowledgeArea: String,
    val name: String,
    val subjectMood: String
)