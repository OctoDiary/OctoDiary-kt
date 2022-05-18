package org.bxkr.octodiary.models.diary

data class Group(
    val id: Long,
    val name: String,
    val parentId: Any,
    val parentName: Any
)