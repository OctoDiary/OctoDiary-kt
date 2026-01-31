package org.bxkr.octodiary.models.lessonschedule



import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.SportsEsports
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
        Test("test_spec_binding", Icons.Default.Checklist),
        Attachment("attachments", Icons.Default.Attachment),
        LessonTemplate("lesson_template", Icons.Default.NoteAlt),
        AtomicObject("atomic_object", Icons.Default.Book),
        GameApp("game_app", Icons.Default.SportsEsports)
    }

    val icon
        get() =
            Type.values().firstOrNull { type == it.serializedName }?.icon
                ?: Icons.Default.QuestionMark
}


