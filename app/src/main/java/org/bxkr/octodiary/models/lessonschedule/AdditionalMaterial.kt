package org.bxkr.octodiary.models.lessonschedule


import com.google.gson.annotations.SerializedName

data class AdditionalMaterial(
    @SerializedName("action_id")
    val actionId: Int,
    @SerializedName("action_name")
    val actionName: String,
    @SerializedName("content_type")
    val contentType: Any?,
    @SerializedName("description")
    val description: Any?,
    @SerializedName("id")
    val id: Any?,
    @SerializedName("selected_mode")
    val selectedMode: Any?,
    @SerializedName("title")
    val title: Any?,
    @SerializedName("type")
    val type: Any?,
    @SerializedName("type_name")
    val typeName: String,
    @SerializedName("urls")
    val urls: List<Any>,
    @SerializedName("uuid")
    val uuid: String
)