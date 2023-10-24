package org.bxkr.octodiary.models.schoolinfo


import com.google.gson.annotations.SerializedName

data class Teacher(
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("middle_name")
    val middleName: String,
    @SerializedName("subject_names")
    val subjectNames: List<String>
)