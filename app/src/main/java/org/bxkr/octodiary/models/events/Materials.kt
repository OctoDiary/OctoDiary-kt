package org.bxkr.octodiary.models.events


import com.google.gson.annotations.SerializedName

data class Materials(
    @SerializedName("count_execute")
    val countExecute: Int,
    @SerializedName("count_learn")
    val countLearn: Int
)