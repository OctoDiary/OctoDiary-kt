package org.bxkr.octodiary.models.lessonschedule


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Attachment
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.NoteAlt
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector
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
) {
    private enum class Type(val serializedName: String, val icon: ImageVector) {
        Test("test_spec_binding", Icons.Rounded.Checklist),
        Attachment("attachments", Icons.Rounded.Attachment),
        LessonTemplate("lesson_template", Icons.Rounded.NoteAlt),
        AtomicObject("atomic_object", Icons.Rounded.Book),
        GameApp("game_app", Icons.Rounded.SportsEsports)
    }

    val icon
        get() =
            Type.values().firstOrNull { type == it.serializedName }?.icon
                ?: Icons.Rounded.QuestionMark
}