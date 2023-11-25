package org.bxkr.octodiary.models.marklistsubject


import com.google.gson.annotations.SerializedName

data class Path(
    @SerializedName("remain")
    val remain: Int,
    @SerializedName("value")
    val value: Int,
    @SerializedName("weight")
    val weight: Int
)