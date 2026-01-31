package org.bxkr.octodiary.models.classmembers



import androidx.compose.material.icons.Icons
import androidx.compose.ui.util.fastJoinToString
import com.google.gson.annotations.SerializedName

data class ClassMember(
    @SerializedName("person_id")
    val personId: String?,
    @SerializedName("user")
    val user: User,
    @SerializedName("is_custom")
    val isCustom: Boolean = false,
    @SerializedName("id")
    val studentId: Long? = null,
) {
    val fio
        get() = user.run {
            listOf(lastName, firstName, middleName).fastJoinToString(" ")
        }
}


