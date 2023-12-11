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

data class MaterialX(
    @SerializedName("action_id")
    val actionId: Int,
    @SerializedName("action_name")
    val actionName: String,
    @SerializedName("items")
    val items: List<ItemX>,
    @SerializedName("type")
    val type: String,
    @SerializedName("type_name")
    val typeName: String
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