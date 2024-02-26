package org.bxkr.octodiary.models.persondata


import com.google.gson.annotations.SerializedName

data class Addresse(
    @SerializedName("actual_from")
    val actualFrom: String,
    @SerializedName("actual_to")
    val actualTo: String,
    @SerializedName("address")
    val address: Address,
    @SerializedName("address_id")
    val addressId: Int,
    @SerializedName("address_type")
    val addressType: AddressType,
    @SerializedName("address_type_id")
    val addressTypeId: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("created_by")
    val createdBy: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("person_id")
    val personId: String,
    @SerializedName("updated_at")
    val updatedAt: Any?,
    @SerializedName("updated_by")
    val updatedBy: Any?,
    @SerializedName("validated_at")
    val validatedAt: Any?,
    @SerializedName("validation_errors")
    val validationErrors: Any?,
    @SerializedName("validation_state_id")
    val validationStateId: Int
)