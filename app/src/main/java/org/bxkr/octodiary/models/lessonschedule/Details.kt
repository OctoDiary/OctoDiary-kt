package org.bxkr.octodiary.models.lessonschedule


import com.google.gson.annotations.SerializedName

data class Details(
    @SerializedName("additional_materials")
    val additionalMaterials: List<AdditionalMaterial>,
    @SerializedName("content")
    val content: List<Any>,
    @SerializedName("lessonId")
    val lessonId: Int,
    @SerializedName("lesson_topic")
    val lessonTopic: String,
    @SerializedName("materials")
    val materials: List<Material>,
    @SerializedName("theme")
    val theme: Theme
)