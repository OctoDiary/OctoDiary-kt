package org.bxkr.octodiary.models.schoolinfo


import com.google.gson.annotations.SerializedName

data class Branch(
    @SerializedName("address")
    val address: String,
    @SerializedName("is_main_building")
    val isMainBuilding: Boolean,
    @SerializedName("is_student_building")
    val isStudentBuilding: Boolean,
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String?
)