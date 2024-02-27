package org.bxkr.octodiary.models.daysbalanceinfo


import com.google.gson.annotations.SerializedName

data class ComplexInfo(
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("is_customizable")
    val isCustomizable: Boolean,
    @SerializedName("price")
    val price: Int,
    @SerializedName("type")
    val type: String
)