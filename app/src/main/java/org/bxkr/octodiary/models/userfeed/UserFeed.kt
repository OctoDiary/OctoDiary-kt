package org.bxkr.octodiary.models.userfeed

data class UserFeed(
    val childLocationInfo: ChildLocationInfo,
    val description: String,
    val feed: List<Feed>,
    val homeworksProgress: Any?,
    val mobileSubscriptionStatus: String,
    val rating: Rating,
    val recentMarks: List<RecentMark>,
    val schedule: Schedule,
    val type: FeedType
)