package org.bxkr.octodiary.models.auth



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class SchoolAuthBody(
    @SerializedName("user_authentication_for_mobile_request")
    val userAuthenticationForMobileRequest: UserAuthenticationForMobileRequest
)


