package org.bxkr.octodiary.models.diary

data class Diary(
    val description: String,
    val mobileSubscriptionStatus: String,
    val type: String,
    val weeks: List<Week>
)