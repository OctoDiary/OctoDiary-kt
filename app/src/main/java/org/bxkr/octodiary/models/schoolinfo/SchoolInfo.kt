package org.bxkr.octodiary.models.schoolinfo


import com.google.gson.annotations.SerializedName

data class SchoolInfo(
    @SerializedName("address")
    val address: Address,
    @SerializedName("branches")
    val branches: List<Branch>,
    @SerializedName("classroom_teachers")
    val classroomTeachers: List<ClassroomTeacher>,
    @SerializedName("email")
    val email: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("principal")
    val principal: String,
    @SerializedName("teachers")
    val teachers: List<Teacher>,
    @SerializedName("type")
    val type: String,
    @SerializedName("website_link")
    val websiteLink: String
)