package org.bxkr.octodiary.models.mark

data class Category(
    val markNumber: Int,
    val mood: String,
    val percent: Double,
    val studentCount: Int,
    val value: String
)