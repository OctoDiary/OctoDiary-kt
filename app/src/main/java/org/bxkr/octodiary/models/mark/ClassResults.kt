package org.bxkr.octodiary.models.mark



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class ClassResults(
    @SerializedName("marks_distributions")
    val marksDistributions: List<MarksDistribution>,
    @SerializedName("total_students")
    val totalStudents: Int,
)


