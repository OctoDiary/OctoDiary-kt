package org.bxkr.octodiary.models.mealbalance


import com.google.gson.annotations.SerializedName

data class Organization(
    @SerializedName("address")
    val address: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String
)