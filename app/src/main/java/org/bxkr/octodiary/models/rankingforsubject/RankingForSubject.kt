package org.bxkr.octodiary.models.rankingforsubject


import com.google.gson.annotations.SerializedName

data class RankingForSubject(
    @SerializedName("imageId")
    val imageId: Int,
    @SerializedName("personId")
    val personId: String,
    @SerializedName("previousRank")
    val previousRank: PreviousRank,
    @SerializedName("rank")
    val rank: Rank
)