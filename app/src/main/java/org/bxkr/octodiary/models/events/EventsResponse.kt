package org.bxkr.octodiary.models.events


import com.google.gson.annotations.SerializedName

data class EventsResponse(
    @SerializedName("errors")
    val errors: Any?,
    @SerializedName("response")
    val response: List<Event>,
    @SerializedName("total_count")
    val totalCount: Int
)