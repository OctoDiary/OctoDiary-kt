package org.bxkr.octodiary.models.auth


import com.google.gson.annotations.SerializedName

data class UserAuthenticationForMobileResponse(
    @SerializedName("mesh_access_token")
    val meshAccessToken: String,
    @SerializedName("mesh_id")
    val meshId: String,
    @SerializedName("obr_id")
    val obrId: Long,
    @SerializedName("request_parameters")
    val requestParameters: RequestParameters,
    @SerializedName("sso_id")
    val ssoId: String,
    @SerializedName("user_authentication_result")
    val userAuthenticationResult: String
)