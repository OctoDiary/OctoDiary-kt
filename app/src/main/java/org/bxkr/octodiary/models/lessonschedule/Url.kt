package org.bxkr.octodiary.models.lessonschedule


import com.google.gson.annotations.SerializedName

data class Url(
    @SerializedName("url")
    val url: String,
    @SerializedName("url_type")
    val urlType: String
)