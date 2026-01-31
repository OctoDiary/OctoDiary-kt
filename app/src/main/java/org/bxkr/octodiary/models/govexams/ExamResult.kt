package org.bxkr.octodiary.models.govexams



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class ExamResult(
    @SerializedName("scoreA")
    val scoreA: Int,
    @SerializedName("scoreB")
    val scoreB: Int,
    @SerializedName("scoreC")
    val scoreC: Int,
    @SerializedName("scoreD")
    val scoreD: Int,
    @SerializedName("variant")
    val variant: Int,
)


