package org.bxkr.octodiary.models.mark



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class MarkValue(
    @SerializedName("five")
    val five: Int,
    @SerializedName("origin")
    val origin: Any?,
    @SerializedName("original_grade_from")
    val originalGradeFrom: Any?,
    @SerializedName("original_grade_to")
    val originalGradeTo: Any?,
)


