package org.bxkr.octodiary.models.profile


import com.google.gson.annotations.SerializedName

data class Section(
    @SerializedName("id")
    val id: Long,
    @SerializedName("is_fake")
    val isFake: Boolean,
    @SerializedName("name")
    val name: String,
    @SerializedName("subject_id")
    val subjectId: Any?
)