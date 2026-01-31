package org.bxkr.octodiary.models.marklistsubjectshort



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class Criteria(
    @SerializedName("name")
    val name: Any?,
    @SerializedName("value")
    val value: String
)


