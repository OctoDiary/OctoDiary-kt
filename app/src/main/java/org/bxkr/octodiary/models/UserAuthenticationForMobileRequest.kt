package org.bxkr.octodiary.models


import com.google.gson.annotations.SerializedName

data class UserAuthenticationForMobileRequest(
    @SerializedName("mos_access_token")
    val mosAccessToken: String
)