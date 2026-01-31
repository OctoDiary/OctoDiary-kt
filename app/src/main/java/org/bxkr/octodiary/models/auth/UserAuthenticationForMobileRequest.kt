package org.bxkr.octodiary.models.auth



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class UserAuthenticationForMobileRequest(
    @SerializedName("mos_access_token")
    val mosAccessToken: String
)


