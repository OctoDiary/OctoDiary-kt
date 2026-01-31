package org.bxkr.octodiary.models.marklistsubject



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName
import org.bxkr.octodiary.parseFromDay
import java.util.Date

data class MarkListSubjectItem(
    @SerializedName("average")
    val average: String,
    @SerializedName("average_by_all")
    val averageByAll: String,
    @SerializedName("dynamic")
    val `dynamic`: String,
    @SerializedName("periods")
    val periods: List<Period>?,
    @SerializedName("subject_id")
    val subjectId: Long,
    @SerializedName("subject_name")
    val subjectName: String,
    @SerializedName("year_mark")
    val yearMark: String?,
) {
    val currentPeriod: Period?
        get() =
            periods?.firstOrNull {
                val currentDate = Date()
                (it.startIso.parseFromDay() < currentDate) and (it.endIso.parseFromDay() > currentDate)
            }
}


