package org.bxkr.octodiary.models.visits


import com.google.gson.annotations.SerializedName

data class Visit(
    @SerializedName("address")
    val address: String?,
    @SerializedName("duration")
    val duration: String,
    @SerializedName("in")
    val inX: String,
    @SerializedName("is_warning")
    val isWarning: Boolean,
    @SerializedName("out")
    val `out`: String,
    @SerializedName("short_name")
    val shortName: String?,
    @SerializedName("type")
    val type: String?
)