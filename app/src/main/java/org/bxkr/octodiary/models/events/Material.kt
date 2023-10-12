package org.bxkr.octodiary.models.events


import com.google.gson.annotations.SerializedName

data class Material(
    @SerializedName("isHiddenFromStudents")
    val isHiddenFromStudents: Boolean,
    @SerializedName("learningTargets")
    val learningTargets: LearningTargets,
    @SerializedName("uuid")
    val uuid: String
)