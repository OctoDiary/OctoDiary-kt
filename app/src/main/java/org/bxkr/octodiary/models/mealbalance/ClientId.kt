package org.bxkr.octodiary.models.mealbalance


import com.google.gson.annotations.SerializedName

data class ClientId(
    @SerializedName("contractId")
    val contractId: Long,
    @SerializedName("personId")
    val personId: String,
    @SerializedName("staffId")
    val staffId: Any?
)