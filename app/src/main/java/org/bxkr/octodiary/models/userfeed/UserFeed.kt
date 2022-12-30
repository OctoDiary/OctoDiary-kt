package org.bxkr.octodiary.models.userfeed

import org.bxkr.octodiary.models.shared.RatingShared

data class UserFeed(
    val childLocationInfo: ChildLocationInfo,
    val description: String,
    val feed: List<PeriodMark>,
    val homeworksProgress: Any?,
    val mobileSubscriptionStatus: String,
    val rating: RatingShared,
    val recentMarks: List<RecentMark>,
    val schedule: Schedule,
    val type: FeedType
)