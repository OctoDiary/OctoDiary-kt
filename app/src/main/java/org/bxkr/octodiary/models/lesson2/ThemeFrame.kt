package org.bxkr.octodiary.models.lesson2


import com.google.gson.annotations.SerializedName

data class ThemeFrame(
    @SerializedName("average_mark")
    val averageMark: Any?,
    @SerializedName("ege_task_name")
    val egeTaskName: Any?,
    @SerializedName("id")
    val id: Int,
    @SerializedName("oge_task_name")
    val ogeTaskName: Any?,
    @SerializedName("theme_frames")
    val themeFrames: List<Any?>,
    @SerializedName("themeIntegrationId")
    val themeIntegrationId: Any?,
    @SerializedName("title")
    val title: String
)