package org.bxkr.octodiary.models.visits



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class VisitsResponse(
    @SerializedName("payload")
    val payload: List<Payload>
)


