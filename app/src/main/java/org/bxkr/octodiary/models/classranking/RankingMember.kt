package org.bxkr.octodiary.models.classranking


import com.google.gson.annotations.SerializedName

data class RankingMember(
    @SerializedName("imageId")
    val imageId: Long,
    @SerializedName("personId")
    val personId: String,
    @SerializedName("previousRank")
    val previousRank: PreviousRank,
    @SerializedName("rank")
    val rank: Rank
)