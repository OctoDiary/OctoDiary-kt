package org.bxkr.octodiary.models.auth


import com.google.gson.annotations.SerializedName

data class RequestParameters(
    @SerializedName("mos_access_token")
    val mosAccessToken: String,
    @SerializedName("mos_id_token")
    val mosIdToken: Any
)