package org.bxkr.octodiary.models.usersettings

import com.google.gson.annotations.SerializedName

data class AuthSettings(
    @SerializedName("client_id")
    val clientId: String,
    @SerializedName("client_secret")
    val clientSecret: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("access_token")
    val accessToken: String,
)
