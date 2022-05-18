package org.bxkr.octodiary.models.user

data class Ranking(
    val description: String,
    val history: History,
    val mobileSubscriptionStatus: String,
    val rating: Rating,
    val ratingDescription: RatingDescription,
    val subjectTop: SubjectTop,
    val type: String
)