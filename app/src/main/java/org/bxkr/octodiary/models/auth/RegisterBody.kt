package org.bxkr.octodiary.models.auth

import com.google.gson.annotations.SerializedName

data class RegisterBody(
    @SerializedName("software_id")
    val softwareId: String,
    @SerializedName("device_type")
    val deviceType: String,
    @SerializedName("software_statement")
    val softwareStatement: String
)