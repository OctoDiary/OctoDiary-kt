package org.bxkr.octodiary.models.mark


import com.google.gson.annotations.SerializedName

data class Teacher(
    @SerializedName("birth_date")
    val birthDate: Any?,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("middle_name")
    val middleName: String,
    @SerializedName("sex")
    val sex: Any?,
    @SerializedName("user_id")
    val userId: Any?
)