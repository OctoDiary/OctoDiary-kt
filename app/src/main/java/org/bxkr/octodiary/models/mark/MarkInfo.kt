package org.bxkr.octodiary.models.mark


import com.google.gson.annotations.SerializedName

data class MarkInfo(
    @SerializedName("activity")
    val activity: Activity,
    @SerializedName("comment")
    val comment: String?,
    @SerializedName("comment_exists")
    val commentExists: Boolean,
    @SerializedName("control_form_name")
    val controlFormName: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("criteria")
    val criteria: Any?,
    @SerializedName("date")
    val date: String,
    @SerializedName("history")
    val history: List<Any>,
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
    @SerializedName("teacher")
    val teacher: Teacher,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("value")
    val value: String,
    @SerializedName("values")
    val values: Any?,
    @SerializedName("weight")
    val weight: Int
)