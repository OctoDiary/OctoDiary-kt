package org.bxkr.octodiary.models.visits


import com.google.gson.annotations.SerializedName

data class Payload(
    @SerializedName("date")
    val date: String,
    @SerializedName("visits")
    val visits: List<Visit>
)