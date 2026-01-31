package org.bxkr.octodiary.models.lessonschedule



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class UrlX(
    @SerializedName("type")
    val type: String,
    @SerializedName("url")
    val url: String
)


