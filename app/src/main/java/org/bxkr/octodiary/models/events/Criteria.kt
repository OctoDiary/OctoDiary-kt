package org.bxkr.octodiary.models.events



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class Criteria(
    @SerializedName("name")
    val name: String,
    @SerializedName("value")
    val value: String
)


