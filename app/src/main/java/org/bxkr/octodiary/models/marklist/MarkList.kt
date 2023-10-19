package org.bxkr.octodiary.models.marklist


import com.google.gson.annotations.SerializedName

data class MarkList(
    @SerializedName("payload")
    val payload: List<Mark>
)