package org.bxkr.octodiary.models.profilesid



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class ProfileId(
    @SerializedName("id")
    val id: Long,
    @SerializedName("type")
    val type: String
)


