package org.bxkr.octodiary.models.classmembers


import com.google.gson.annotations.SerializedName

data class School(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("short_name")
    val shortName: String
)