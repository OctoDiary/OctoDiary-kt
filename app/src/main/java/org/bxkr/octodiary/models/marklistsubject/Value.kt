package org.bxkr.octodiary.models.marklistsubject


import com.google.gson.annotations.SerializedName

data class Value(
    @SerializedName("grade")
    val grade: Grade,
    @SerializedName("grade_system_id")
    val gradeSystemId: Any?,
    @SerializedName("grade_system_type")
    val gradeSystemType: String,
    @SerializedName("name")
    val name: Any?
)