package org.bxkr.octodiary.models.schoolinfo


import com.google.gson.annotations.SerializedName

data class Address(
    @SerializedName("address")
    val address: String?,
    @SerializedName("county")
    val county: String?,
    @SerializedName("district")
    val district: String?
)