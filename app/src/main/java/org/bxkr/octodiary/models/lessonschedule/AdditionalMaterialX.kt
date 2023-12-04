package org.bxkr.octodiary.models.lessonschedule


import com.google.gson.annotations.SerializedName

data class AdditionalMaterialX(
    @SerializedName("action_id")
    val actionId: Int,
    @SerializedName("action_name")
    val actionName: String,
    @SerializedName("content_type")
    val contentType: Any?,
    @SerializedName("description")
    val description: Any?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("selected_mode")
    val selectedMode: String?,
    @SerializedName("title")
    val title: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("type_name")
    val typeName: String,
    @SerializedName("urls")
    val urls: List<UrlX>,
    @SerializedName("uuid")
    val uuid: String?
)