package org.bxkr.octodiary.models.persondata


import com.google.gson.annotations.SerializedName

data class Address(
    @SerializedName("address")
    val address: String,
    @SerializedName("fias_id")
    val fiasId: String,
    @SerializedName("flat")
    val flat: String,
    @SerializedName("global_id")
    val globalId: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("unom")
    val unom: Int,
    @SerializedName("validated_at")
    val validatedAt: Any?,
    @SerializedName("validation_errors")
    val validationErrors: Any?,
    @SerializedName("validation_state_id")
    val validationStateId: Int
)