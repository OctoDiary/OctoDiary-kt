package org.bxkr.octodiary.models.rating

data class RatingClass(
    val history: History?,
    val rating: Rating,
    val subjectTop: SubjectTop,
    val ratingDescription: RatingDescription,
    val type: String,
    val description: String,
    val mobileSubscriptionStatus: String
)