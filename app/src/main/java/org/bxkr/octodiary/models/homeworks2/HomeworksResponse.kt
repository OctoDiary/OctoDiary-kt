package org.bxkr.octodiary.models.homeworks2



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class HomeworksResponse(
    @SerializedName("payload")
    val payload: List<Homework>
)


