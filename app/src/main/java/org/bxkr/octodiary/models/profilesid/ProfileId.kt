package org.bxkr.octodiary.models.profilesid


import com.google.gson.annotations.SerializedName

data class ProfileId(
    @SerializedName("id")
    val id: Int,
    @SerializedName("type")
    val type: String
)