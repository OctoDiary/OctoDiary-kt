package org.bxkr.octodiary.models.lessonschedule


import com.google.gson.annotations.SerializedName

data class ThemeFrame(
    @SerializedName("average_mark")
    val averageMark: String,
    @SerializedName("ege_task_name")
    val egeTaskName: Any?,
    @SerializedName("id")
    val id: Int,
    @SerializedName("oge_task_name")
    val ogeTaskName: Any?,
    @SerializedName("theme_frames")
    val themeFrames: List<ThemeFrameX>,
    @SerializedName("themeIntegrationId")
    val themeIntegrationId: Int,
    @SerializedName("title")
    val title: String
)