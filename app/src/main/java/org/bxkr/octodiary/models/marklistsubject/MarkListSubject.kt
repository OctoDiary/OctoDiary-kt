package org.bxkr.octodiary.models.marklistsubject



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class MarkListSubject(
    @SerializedName("payload")
    val payload: List<MarkListSubjectItem>
)


