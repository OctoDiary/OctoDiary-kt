package org.bxkr.octodiary.models.user

data class Subject(
    val id: Long,
    val knowledgeArea: String,
    val name: String,
    val subjectMood: String
)