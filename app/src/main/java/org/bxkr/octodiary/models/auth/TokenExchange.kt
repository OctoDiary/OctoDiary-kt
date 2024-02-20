package org.bxkr.octodiary.models.auth


import com.google.gson.annotations.SerializedName

data class TokenExchange(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("expires_in")
    val expiresIn: Int,
    @SerializedName("id_token")
    val idToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("scope")
    val scope: String,
    @SerializedName("token_type")
    val tokenType: String,
) {
    data class Refresh(
        @SerializedName("access_token")
        val accessToken: String,
        @SerializedName("expires_in")
        val expiresIn: Int,
        @SerializedName("refresh_token")
        val refreshToken: String,
        @SerializedName("scope")
        val scope: String,
        @SerializedName("token_type")
        val tokenType: String,
    )
}