package org.bxkr.octodiary.models.subjectranking



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class Rank(
    @SerializedName("averageMarkFive")
    val averageMarkFive: Double,
    @SerializedName("rankPlace")
    val rankPlace: Int,
    @SerializedName("rankStatus")
    val rankStatus: String,
    @SerializedName("trend")
    val trend: String
)


