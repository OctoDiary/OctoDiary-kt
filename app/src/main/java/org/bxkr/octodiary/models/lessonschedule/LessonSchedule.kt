package org.bxkr.octodiary.models.lessonschedule


import com.google.gson.annotations.SerializedName
import org.bxkr.octodiary.models.events.Mark

data class LessonSchedule(
    @SerializedName("begin_time")
    val beginTime: String,
    @SerializedName("begin_utc")
    val beginUtc: Int,
    @SerializedName("building_name")
    val buildingName: String,
    @SerializedName("comment")
    val comment: Any?,
    @SerializedName("control")
    val control: Any?,
    @SerializedName("course_lesson_type")
    val courseLessonType: Any?,
    @SerializedName("created_date_time")
    val createdDateTime: Any?,
    @SerializedName("date")
    val date: String,
    @SerializedName("details")
    val details: Details,
    @SerializedName("disease_status_type")
    val diseaseStatusType: Any?,
    @SerializedName("end_time")
    val endTime: String,
    @SerializedName("end_utc")
    val endUtc: Int,
    @SerializedName("esz_field_id")
    val eszFieldId: Any?,
    @SerializedName("evaluation")
    val evaluation: Any?,
    @SerializedName("field_name")
    val fieldName: Any?,
    @SerializedName("homework_presence_status_id")
    val homeworkPresenceStatusId: Int,
    @SerializedName("homework_to_give")
    val homeworkToGive: Any?,
    @SerializedName("id")
    val id: Int,
    @SerializedName("is_missed_lesson")
    val isMissedLesson: Boolean,
    @SerializedName("is_virtual")
    val isVirtual: Boolean,
    @SerializedName("lesson_education_type")
    val lessonEducationType: String,
    @SerializedName("lesson_homeworks")
    val lessonHomeworks: List<LessonHomework>,
    @SerializedName("lesson_type")
    val lessonType: String,
    @SerializedName("marks")
    val marks: List<Mark>,
    @SerializedName("plan_id")
    val planId: Long,
    @SerializedName("remote_lesson")
    val remoteLesson: Any?,
    @SerializedName("room_name")
    val roomName: String,
    @SerializedName("room_number")
    val roomNumber: String,
    @SerializedName("subject_id")
    val subjectId: Long,
    @SerializedName("subject_name")
    val subjectName: String,
    @SerializedName("teacher")
    val teacher: Teacher,
)