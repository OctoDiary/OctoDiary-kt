package org.bxkr.octodiary.models.lessonschedule


import com.google.gson.annotations.SerializedName

data class Material(
    @SerializedName("action_id")
    val actionId: Int,
    @SerializedName("action_name")
    val actionName: String,
    @SerializedName("items")
    val items: List<Item>,
    @SerializedName("type")
    val type: String,
    @SerializedName("type_name")
    val typeName: String
)