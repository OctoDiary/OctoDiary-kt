package org.bxkr.octodiary.models.user

data class Experiments(
    val notes: Boolean,
    val withoutSupportButton: Boolean,
    val mobileChatExperiment: Boolean,
    val kidTrackerValueTest: Boolean,
    val fullDataLocalValidationExperiment: Boolean,
    val ratingsTurnOffExperiment: Boolean,
    val weekViewLessonListExperiment: Boolean,
    val blockerLessonsListExperiment: Boolean,
    val blockerMarkExperiment: Boolean,
    val homeworkPushMobileExperiment: Boolean,
    val messengerMongooseMobileTeacherExperiment: Boolean,
    val communityInvitationExperiment: Boolean
)