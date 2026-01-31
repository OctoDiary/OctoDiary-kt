package org.bxkr.octodiary.models.rankingforsubject



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class PreviousRank(
    @SerializedName("averageMarkFive")
    val averageMarkFive: Double,
    @SerializedName("rankPlace")
    val rankPlace: Int
)


