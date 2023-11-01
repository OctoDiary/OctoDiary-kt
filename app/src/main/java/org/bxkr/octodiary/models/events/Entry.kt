package org.bxkr.octodiary.models.events


import com.google.gson.annotations.SerializedName

data class Entry(
    @SerializedName("attachment_ids")
    val attachmentIds: List<Any>,
    @SerializedName("attachments")
    val attachments: List<Any>,
    @SerializedName("date_assigned_on")
    val dateAssignedOn: String,
    @SerializedName("date_prepared_for")
    val datePreparedFor: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("duration")
    val duration: Int,
    @SerializedName("homework_entry_id")
    val homeworkEntryId: Long,
    @SerializedName("materials")
    val materials: String?,
    @SerializedName("student_ids")
    val studentIds: Any?
)