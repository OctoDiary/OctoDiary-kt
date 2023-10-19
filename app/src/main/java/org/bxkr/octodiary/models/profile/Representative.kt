package org.bxkr.octodiary.models.profile


import com.google.gson.annotations.SerializedName

data class Representative(
    @SerializedName("birth_date")
    val birthDate: Any?,
    @SerializedName("contract_id")
    val contractId: Any?,
    @SerializedName("email")
    val email: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("middle_name")
    val middleName: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("sex")
    val sex: Any?,
    @SerializedName("snils")
    val snils: String,
    @SerializedName("type")
    val type: Any?,
    @SerializedName("user_id")
    val userId: Any?
)