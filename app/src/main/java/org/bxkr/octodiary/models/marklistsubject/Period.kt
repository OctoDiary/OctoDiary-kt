package org.bxkr.octodiary.models.marklistsubject



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class Period(
    @SerializedName("count")
    val count: Int,
    @SerializedName("dynamic")
    val `dynamic`: String,
    @SerializedName("end")
    val end: String,
    @SerializedName("end_iso")
    val endIso: String,
    @SerializedName("fixed_value")
    val fixedValue: String?,
    @SerializedName("marks")
    val marks: List<Mark>,
    @SerializedName("start")
    val start: String,
    @SerializedName("start_iso")
    val startIso: String,
    @SerializedName("target")
    val target: Target?,
    @SerializedName("title")
    val title: String,
    @SerializedName("value")
    val value: String,
)


