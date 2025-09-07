package org.bxkr.octodiary.models.lesson2


import com.google.gson.annotations.SerializedName

data class Details(
    @SerializedName("additional_materials")
    val additionalMaterials: List<AdditionalMaterial>,
    @SerializedName("content")
    val content: List<Any?>,
    @SerializedName("lessonId")
    val lessonId: Int,
    @SerializedName("lesson_topic")
    val lessonTopic: String,
    @SerializedName("theme")
    val theme: Theme
)