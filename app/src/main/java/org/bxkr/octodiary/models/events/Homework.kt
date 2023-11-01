package org.bxkr.octodiary.models.events


import com.google.gson.annotations.SerializedName

data class Homework(
    @SerializedName("descriptions")
    val descriptions: List<String>,
    @SerializedName("entries")
    val entries: List<Entry>?,
    @SerializedName("execute_count")
    val executeCount: Int?,
    @SerializedName("link_types")
    val linkTypes: Any?,
    @SerializedName("materials")
    val materials: Materials?,
    @SerializedName("presence_status_id")
    val presenceStatusId: Long,
    @SerializedName("total_count")
    val totalCount: Int
)