package org.bxkr.octodiary.models.classranking


import com.google.gson.annotations.SerializedName

data class PreviousRank(
    @SerializedName("averageMarkFive")
    val averageMarkFive: Double,
    @SerializedName("rankPlace")
    val rankPlace: Int
)