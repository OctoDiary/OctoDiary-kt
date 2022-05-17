package org.bxkr.octodiary.models

data class User(
    val avatarUrl: String,
    val classLetter: String,
    val className: String,
    val classNumber: Int,
    val firstName: String,
    val groupId: Long,
    val lastName: String,
    val middleName: String,
    val personId: Long,
    val rankingHistory: List<RankingHistory>,
    val rankingPlace: Int,
    val schoolAvatarUrl: String,
    val schoolGeoPosition: List<Any>,
    val schoolId: Long,
    val schoolName: String,
    val sex: String,
    val userId: Long
)

data class RankingHistory(
    val date: String,
    val place: Int
)