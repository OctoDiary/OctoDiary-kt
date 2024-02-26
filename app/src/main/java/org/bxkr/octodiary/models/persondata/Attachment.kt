package org.bxkr.octodiary.models.persondata


import com.google.gson.annotations.SerializedName

data class Attachment(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("uploaded_at")
    val uploadedAt: String
)