package org.bxkr.octodiary.models.lessonschedule


import com.google.gson.annotations.SerializedName

data class ItemX(
    @SerializedName("accepted_at")
    val acceptedAt: Any?,
    @SerializedName("author")
    val author: String?,
    @SerializedName("average_rating")
    val averageRating: Any?,
    @SerializedName("binding_id")
    val bindingId: Any?,
    @SerializedName("class_level_ids")
    val classLevelIds: Any?,
    @SerializedName("content_type")
    val contentType: Any?,
    @SerializedName("created_at")
    val createdAt: Any?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("file_size")
    val fileSize: Int?,
    @SerializedName("for_home")
    val forHome: Any?,
    @SerializedName("for_lesson")
    val forLesson: Any?,
    @SerializedName("full_cover_url")
    val fullCoverUrl: Any?,
    @SerializedName("icon_url")
    val iconUrl: Any?,
    @SerializedName("id")
    val id: Int,
    @SerializedName("is_hidden_from_students")
    val isHiddenFromStudents: Boolean,
    @SerializedName("is_necessary")
    val isNecessary: Boolean,
    @SerializedName("link")
    val link: String?,
    @SerializedName("partner_response")
    val partnerResponse: Any?,
    @SerializedName("selected_mode")
    val selectedMode: String?,
    @SerializedName("title")
    val title: String,
    @SerializedName("updated_at")
    val updatedAt: Any?,
    @SerializedName("urls")
    val urls: List<Url>,
    @SerializedName("user_name")
    val userName: Any?,
    @SerializedName("uuid")
    val uuid: String?,
    @SerializedName("views")
    val views: Any?
)