package org.bxkr.octodiary.models.daysbalanceinfo


import com.google.gson.annotations.SerializedName

data class Transaction(
    @SerializedName("items")
    val items: List<TransactionItem>,
    @SerializedName("sum")
    val sum: Int,
    @SerializedName("type")
    val type: String
)