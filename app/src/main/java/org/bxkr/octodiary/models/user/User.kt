package org.bxkr.octodiary.models.user

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
    val ranking: Ranking,
    val schoolAvatarUrl: String,
    val schoolGeoPosition: List<Any>,
    val schoolId: Long,
    val schoolName: String,
    val sex: String,
    val userId: Long
)