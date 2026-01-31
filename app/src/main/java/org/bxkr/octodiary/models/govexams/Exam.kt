package org.bxkr.octodiary.models.govexams



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class Exam(
    @SerializedName("approbation")
    val approbation: Boolean,
    @SerializedName("date")
    val date: String,
    @SerializedName("examResult")
    val examResult: ExamResult,
    @SerializedName("examsId")
    val examsId: Int,
    @SerializedName("formaGia")
    val formaGia: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("isCredit")
    val isCredit: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("normalizedMarkBasis")
    val normalizedMarkBasis: String?,
    @SerializedName("normalizedMarkValue")
    val normalizedMarkValue: String,
    @SerializedName("positiveResultThreshold")
    val positiveResultThreshold: Int,
    @SerializedName("primaryMarkBasis")
    val primaryMarkBasis: Int,
    @SerializedName("primaryMarkValue")
    val primaryMarkValue: Int,
    @SerializedName("subjectID")
    val subjectID: Int,
    @SerializedName("timeStamp")
    val timeStamp: Long,
) {
    enum class ExamCategories {
        UnifiedStateExam, // "ЕГЭ", "YeGE".
        BasicStateExam, // "ОГЭ", "OGE".
        Other // Interviews, essays, etc.
    }

    val examCategory
        get() = when (formaGia) {
            "ЕГЭ" -> ExamCategories.UnifiedStateExam
            "ОГЭ" -> ExamCategories.BasicStateExam
            else -> ExamCategories.Other
        }
}


