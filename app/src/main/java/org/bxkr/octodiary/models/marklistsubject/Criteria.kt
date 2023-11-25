package org.bxkr.octodiary.models.marklistsubject


import com.google.gson.annotations.SerializedName

data class Criteria(
    @SerializedName("name")
    val name: Any?,
    @SerializedName("value")
    val value: String
)