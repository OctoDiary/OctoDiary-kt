package org.bxkr.octodiary.models.lessonschedule


import com.google.gson.annotations.SerializedName

data class LessonHomework(
    @SerializedName("additional_materials")
    val additionalMaterials: List<AdditionalMaterialX>,
    @SerializedName("attachments")
    val attachments: List<Any>,
    @SerializedName("date_assigned_on")
    val dateAssignedOn: String,
    @SerializedName("date_prepared_for")
    val datePreparedFor: String,
    @SerializedName("homework")
    val homework: String,
    @SerializedName("homework_created_at")
    val homeworkCreatedAt: String,
    @SerializedName("homework_entry_id")
    val homeworkEntryId: Long,
    @SerializedName("homework_entry_student_id")
    val homeworkEntryStudentId: Long,
    @SerializedName("homework_id")
    val homeworkId: Int,
    @SerializedName("homework_updated_at")
    val homeworkUpdatedAt: String,
    @SerializedName("is_done")
    val isDone: Boolean,
    @SerializedName("materials")
    val materials: List<MaterialX>,
    @SerializedName("written_answer")
    val writtenAnswer: Any?
)