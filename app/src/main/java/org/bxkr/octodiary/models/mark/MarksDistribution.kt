package org.bxkr.octodiary.models.mark



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class MarksDistribution(
    @SerializedName("mark_value")
    val markValue: MarkValue,
    @SerializedName("number_of_students")
    val numberOfStudents: Int,
    @SerializedName("percentage_of_students")
    val percentageOfStudents: Int,
)


