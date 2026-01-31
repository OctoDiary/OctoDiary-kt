package org.bxkr.octodiary.models.auth



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class RequestParameters(
    @SerializedName("mos_access_token")
    val mosAccessToken: String,
    @SerializedName("mos_id_token")
    val mosIdToken: Any
)


