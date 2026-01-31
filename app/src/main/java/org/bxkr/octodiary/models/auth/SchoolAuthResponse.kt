package org.bxkr.octodiary.models.auth



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class SchoolAuthResponse(
    @SerializedName("user_authentication_for_mobile_response")
    val userAuthenticationForMobileResponse: UserAuthenticationForMobileResponse
)


