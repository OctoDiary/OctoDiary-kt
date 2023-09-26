package org.bxkr.octodiary.models.auth


import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("client_id")
    val clientId: String,
    @SerializedName("client_secret")
    val clientSecret: String,
    @SerializedName("client_secret_expires_at")
    val clientSecretExpiresAt: Int,
    @SerializedName("device_type")
    val deviceType: String,
    @SerializedName("grant_types")
    val grantTypes: List<String>,
    @SerializedName("redirect_uris")
    val redirectUris: List<String>,
    @SerializedName("registration_access_token")
    val registrationAccessToken: String,
    @SerializedName("registration_client_uri")
    val registrationClientUri: String,
    @SerializedName("response_types")
    val responseTypes: List<String>,
    @SerializedName("scope")
    val scope: String,
    @SerializedName("software_id")
    val softwareId: String,
    @SerializedName("software_version")
    val softwareVersion: String,
    @SerializedName("token_endpoint_auth_method")
    val tokenEndpointAuthMethod: String
)