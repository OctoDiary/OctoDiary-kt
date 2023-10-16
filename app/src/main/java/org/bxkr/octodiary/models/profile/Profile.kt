package org.bxkr.octodiary.models.profile


import com.google.gson.annotations.SerializedName

data class Profile(
    @SerializedName("birth_date")
    val birthDate: String,
    @SerializedName("contract_id")
    val contractId: Any?,
    @SerializedName("email")
    val email: Any?,
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
    val sex: String,
    @SerializedName("snils")
    val snils: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("user_id")
    val userId: Int
)