package org.bxkr.octodiary.models.persondata


import com.google.gson.annotations.SerializedName

data class OrganizationX(
    @SerializedName("actual_from")
    val actualFrom: String,
    @SerializedName("actual_to")
    val actualTo: String,
    @SerializedName("constituent_entity_id")
    val constituentEntityId: Int,
    @SerializedName("global_id")
    val globalId: Int,
    @SerializedName("status_id")
    val statusId: Int
)