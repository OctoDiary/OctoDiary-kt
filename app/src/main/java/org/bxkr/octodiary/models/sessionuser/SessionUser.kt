package org.bxkr.octodiary.models.sessionuser


import com.google.gson.annotations.SerializedName

data class SessionUser(
    @SerializedName("authentication_token")
    val authenticationToken: String,
    @SerializedName("date_of_birth")
    val dateOfBirth: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("guid")
    val guid: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("middle_name")
    val middleName: String,
    @SerializedName("password_change_required")
    val passwordChangeRequired: Boolean,
    @SerializedName("person_id")
    val personId: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    @SerializedName("profiles")
    val profiles: List<Profile>,
    @SerializedName("regional_auth")
    val regionalAuth: String,
    @SerializedName("sex")
    val sex: String,
    @SerializedName("snils")
    val snils: String
) {
    data class Body(
        @SerializedName("auth_token")
        val accessToken: String,
    )
}