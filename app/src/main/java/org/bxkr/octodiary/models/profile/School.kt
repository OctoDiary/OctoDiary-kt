package org.bxkr.octodiary.models.profile


import com.google.gson.annotations.SerializedName

data class School(
    @SerializedName("county")
    val county: String,
    @SerializedName("global_school_id")
    val globalSchoolId: Long,
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("principal")
    val principal: String,
    @SerializedName("short_name")
    val shortName: String
)