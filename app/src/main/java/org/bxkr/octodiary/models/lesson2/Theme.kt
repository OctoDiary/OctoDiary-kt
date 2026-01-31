package org.bxkr.octodiary.models.lesson2



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class Theme(
    @SerializedName("average_mark")
    val averageMark: Any?,
    @SerializedName("ege_task_name")
    val egeTaskName: Any?,
    @SerializedName("id")
    val id: Any?,
    @SerializedName("oge_task_name")
    val ogeTaskName: Any?,
    @SerializedName("theme_frames")
    val themeFrames: List<ThemeFrame>,
    @SerializedName("themeIntegrationId")
    val themeIntegrationId: Any?,
    @SerializedName("title")
    val title: Any?
)


