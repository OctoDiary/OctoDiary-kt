package org.bxkr.octodiary.models.homeworks


import com.google.gson.annotations.SerializedName

data class Homework(
    @SerializedName("date")
    val date: String,
    @SerializedName("date_assigned_on")
    val dateAssignedOn: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("has_teacher_answer")
    val hasTeacherAnswer: Boolean,
    @SerializedName("has_written_answer")
    val hasWrittenAnswer: Boolean,
    @SerializedName("homework_entry_student_id")
    val homeworkEntryStudentId: Long,
    @SerializedName("is_done")
    val isDone: Boolean,
    @SerializedName("materials_count")
    val materialsCount: List<Material>,
    @SerializedName("subject_id")
    val subjectId: Int,
    @SerializedName("subject_name")
    val subjectName: String,
    @SerializedName("type")
    val type: String
)