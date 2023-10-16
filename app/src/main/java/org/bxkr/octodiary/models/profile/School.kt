package org.bxkr.octodiary.models.profile


import com.google.gson.annotations.SerializedName

data class School(
    @SerializedName("county")
    val county: String,
    @SerializedName("global_school_id")
    val globalSchoolId: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("principal")
    val principal: String,
    @SerializedName("short_name")
    val shortName: String
)