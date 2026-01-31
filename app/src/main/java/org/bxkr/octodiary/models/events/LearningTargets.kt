package org.bxkr.octodiary.models.events



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class LearningTargets(
    @SerializedName("forHome")
    val forHome: Boolean,
    @SerializedName("forLesson")
    val forLesson: Boolean
)


