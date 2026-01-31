package org.bxkr.octodiary.models.marklistdate



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class MarkListDate(
    @SerializedName("payload")
    val payload: List<Mark>
)


