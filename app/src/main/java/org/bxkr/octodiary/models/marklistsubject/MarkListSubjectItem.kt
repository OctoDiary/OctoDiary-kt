package org.bxkr.octodiary.models.marklistsubject


import com.google.gson.annotations.SerializedName

data class MarkListSubjectItem(
    @SerializedName("average")
    val average: String?,
    @SerializedName("count")
    val count: Int?,
    @SerializedName("dynamic")
    val `dynamic`: String,
    @SerializedName("end")
    val end: String?,
    @SerializedName("fixed_value")
    val fixedValue: String?,
    @SerializedName("marks")
    val marks: List<Mark>?,
    @SerializedName("period")
    val period: String?,
    @SerializedName("start")
    val start: String?,
    @SerializedName("subject_id")
    val id: Long,
    @SerializedName("subject_name")
    val subjectName: String,
    @SerializedName("target")
    val target: Target?
)