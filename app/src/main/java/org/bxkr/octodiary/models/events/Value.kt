package org.bxkr.octodiary.models.events


import com.google.gson.annotations.SerializedName

data class Value(
    @SerializedName("grade")
    val grade: Grade,
    @SerializedName("grade_system_id")
    val gradeSystemId: Int,
    @SerializedName("grade_system_type")
    val gradeSystemType: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("nmax")
    val nmax: Double
)