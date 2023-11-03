package org.bxkr.octodiary.models.classmembers


import com.google.gson.annotations.SerializedName

data class ClassUnit(
    @SerializedName("home_based")
    val homeBased: Boolean,
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String
)