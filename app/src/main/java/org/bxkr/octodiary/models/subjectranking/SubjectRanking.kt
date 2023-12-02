package org.bxkr.octodiary.models.subjectranking


import com.google.gson.annotations.SerializedName

data class SubjectRanking(
    @SerializedName("rank")
    val rank: Rank,
    @SerializedName("subjectId")
    val subjectId: Long,
    @SerializedName("subjectName")
    val subjectName: String
)