package org.bxkr.octodiary.models.daysbalanceinfo


import com.google.gson.annotations.SerializedName

data class Day(
    @SerializedName("date")
    val date: String,
    @SerializedName("expense")
    val expense: Int,
    @SerializedName("transactions")
    val transactions: List<Transaction>
)