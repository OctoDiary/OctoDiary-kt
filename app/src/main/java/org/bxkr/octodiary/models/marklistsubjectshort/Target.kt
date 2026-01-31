package org.bxkr.octodiary.models.marklistsubjectshort



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class Target(
    @SerializedName("paths")
    val paths: List<Path>,
    @SerializedName("remain")
    val remain: Int,
    @SerializedName("round")
    val round: String?,
    @SerializedName("value")
    val value: Int,
)


