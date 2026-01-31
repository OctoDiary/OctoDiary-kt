package org.bxkr.octodiary.models.markold



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class Activity(
    @SerializedName("lesson_topic")
    val lessonTopic: String,
    @SerializedName("schedule_item_id")
    val scheduleItemId: Long,
)


