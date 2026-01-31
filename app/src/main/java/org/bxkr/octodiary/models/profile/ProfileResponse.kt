package org.bxkr.octodiary.models.profile



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("children")
    val children: List<Children>,
    @SerializedName("hash")
    val hash: String,
    @SerializedName("profile")
    val profile: Profile
)


