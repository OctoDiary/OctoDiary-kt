package org.bxkr.octodiary.models.homeworks


import com.google.gson.annotations.SerializedName

data class Material(
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("selected_mode")
    val selectedMode: String
)