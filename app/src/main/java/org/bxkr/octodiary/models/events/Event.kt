package org.bxkr.octodiary.models.events


import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("absence_reason_id")
    val absenceReasonId: Int?,
    @SerializedName("activities")
    val activities: Any?,
    @SerializedName("address")
    val address: Any?,
    @SerializedName("attendances")
    val attendances: Any?,
    @SerializedName("author_id")
    val authorId: String?,
    @SerializedName("author_name")
    val authorName: Any?,
    @SerializedName("building_id")
    val buildingId: Int?,
    @SerializedName("building_name")
    val buildingName: String?,
    @SerializedName("cancelled")
    val cancelled: Boolean?,
    @SerializedName("city_building_name")
    val cityBuildingName: Any?,
    @SerializedName("class_unit_ids")
    val classUnitIds: List<Int>?,
    @SerializedName("class_unit_name")
    val classUnitName: String?,
    @SerializedName("comment")
    val comment: Any?,
    @SerializedName("comment_count")
    val commentCount: Any?,
    @SerializedName("comments")
    val comments: Any?,
    @SerializedName("conference_link")
    val conferenceLink: String?,
    @SerializedName("contact_email")
    val contactEmail: Any?,
    @SerializedName("contact_name")
    val contactName: Any?,
    @SerializedName("contact_phone")
    val contactPhone: Any?,
    @SerializedName("control")
    val control: Any?,
    @SerializedName("course_lesson_type")
    val courseLessonType: Any?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("esz_field_id")
    val eszFieldId: Int?,
    @SerializedName("external_activities_type")
    val externalActivitiesType: Any?,
    @SerializedName("finish_at")
    val finishAt: String,
    @SerializedName("format_name")
    val formatName: Any?,
    @SerializedName("group_id")
    val groupId: Int?,
    @SerializedName("group_name")
    val groupName: String?,
    @SerializedName("health_status")
    val healthStatus: Any?,
    @SerializedName("homework")
    val homework: Homework?,
    @SerializedName("id")
    val id: Int,
    @SerializedName("is_all_day")
    val isAllDay: Boolean?,
    @SerializedName("is_metagroup")
    val isMetagroup: Any?,
    @SerializedName("is_missed_lesson")
    val isMissedLesson: Boolean?,
    @SerializedName("journal_fill")
    val journalFill: Boolean?,
    @SerializedName("lesson_education_type")
    val lessonEducationType: Any?,
    @SerializedName("lesson_name")
    val lessonName: String?,
    @SerializedName("lesson_theme")
    val lessonTheme: String?,
    @SerializedName("lesson_type")
    val lessonType: String?,
    @SerializedName("link_to_join")
    val linkToJoin: Any?,
    @SerializedName("marks")
    val marks: List<Mark>?,
    @SerializedName("materials")
    val materials: List<Material>?,
    @SerializedName("need_document")
    val needDocument: Any?,
    @SerializedName("nonattendance_reason_id")
    val nonattendanceReasonId: Int?,
    @SerializedName("outdoor")
    val outdoor: Boolean?,
    @SerializedName("place")
    val place: String?,
    @SerializedName("place_comment")
    val placeComment: Any?,
    @SerializedName("place_latitude")
    val placeLatitude: Any?,
    @SerializedName("place_longitude")
    val placeLongitude: Any?,
    @SerializedName("place_name")
    val placeName: Any?,
    @SerializedName("registration_end_at")
    val registrationEndAt: Any?,
    @SerializedName("registration_start_at")
    val registrationStartAt: Any?,
    @SerializedName("replaced")
    val replaced: Boolean?,
    @SerializedName("replaced_teacher_id")
    val replacedTeacherId: Int?,
    @SerializedName("room_name")
    val roomName: String?,
    @SerializedName("room_number")
    val roomNumber: String?,
    @SerializedName("source")
    val source: String,
    @SerializedName("source_id")
    val sourceId: String?,
    @SerializedName("start_at")
    val startAt: String,
    @SerializedName("student_count")
    val studentCount: Any?,
    @SerializedName("subject_id")
    val subjectId: Int?,
    @SerializedName("subject_name")
    val subjectName: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("type")
    val type: Any?,
    @SerializedName("types")
    val types: List<Any>?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("url")
    val url: Any?,
    @SerializedName("visible_fake_group")
    val visibleFakeGroup: Any?
)