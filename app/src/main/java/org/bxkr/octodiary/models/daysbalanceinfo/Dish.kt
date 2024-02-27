package org.bxkr.octodiary.models.daysbalanceinfo


import com.google.gson.annotations.SerializedName

data class Dish(
    @SerializedName("amount")
    val amount: Int?,
    @SerializedName("price")
    val price: Int?,
    @SerializedName("title")
    val title: String
)