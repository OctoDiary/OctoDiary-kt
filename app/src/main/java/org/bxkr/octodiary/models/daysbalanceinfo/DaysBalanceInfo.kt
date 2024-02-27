package org.bxkr.octodiary.models.daysbalanceinfo


import com.google.gson.annotations.SerializedName

data class DaysBalanceInfo(
    @SerializedName("days")
    val days: List<Day>,
    @SerializedName("has_next_page")
    val hasNextPage: Boolean
)