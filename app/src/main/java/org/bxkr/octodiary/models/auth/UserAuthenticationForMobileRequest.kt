package org.bxkr.octodiary.models.auth


import com.google.gson.annotations.SerializedName

data class UserAuthenticationForMobileRequest(
    @SerializedName("mos_access_token")
    val mosAccessToken: String
)