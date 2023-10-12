package org.bxkr.octodiary.models.events


import com.google.gson.annotations.SerializedName

data class Mark(
    @SerializedName("comment")
    val comment: String?,
    @SerializedName("comment_exists")
    val commentExists: Boolean,
    @SerializedName("control_form_name")
    val controlFormName: String,
    @SerializedName("criteria")
    val criteria: List<Criteria>,
    @SerializedName("id")
    val id: Int,
    @SerializedName("is_exam")
    val isExam: Boolean,
    @SerializedName("is_point")
    val isPoint: Boolean,
    @SerializedName("original_grade_system_type")
    val originalGradeSystemType: String,
    @SerializedName("point_date")
    val pointDate: Any?,
    @SerializedName("value")
    val value: String,
    @SerializedName("values")
    val values: List<Value>,
    @SerializedName("weight")
    val weight: Int
)