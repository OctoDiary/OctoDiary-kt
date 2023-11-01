package org.bxkr.octodiary.models.auth


import com.google.gson.annotations.SerializedName

data class RegionalCredentialsResponse(
    @SerializedName("authentication_token")
    val authenticationToken: String,
    @SerializedName("date_of_birth")
    val dateOfBirth: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("guid")
    val guid: String,
    @SerializedName("id")
    val id: Long,
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
    val profiles: List<RegionalProfile>,
    @SerializedName("regional_auth")
    val regionalAuth: String,
    @SerializedName("snils")
    val snils: String,
    @SerializedName("sso_id")
    val ssoId: String
) {
    data class Body(
        @SerializedName("login")
        val login: String,
        @SerializedName("password_plain")
        val password: String
    )
}