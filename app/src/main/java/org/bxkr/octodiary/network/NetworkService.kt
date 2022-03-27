package org.bxkr.octodiary.network

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import java.math.BigInteger

object NetworkService {
    private const val apiUrl =
        "https://api.bxkr.org/school/" // See https://github.com/OctoDiary/OctoDiary-API

    data class AuthResult(
        val access_token: String, val user_id: BigInteger
    )

    data class Diary(
        val weeks: List<Week>
    )

    data class Week(
        val days: List<Day>,
        val firstWeekDayDate: String,
        val homeworksCount: Int,
        val id: String,
        val lastWeekDayDate: String
    )

    data class Day(
        val date: String,
        val dayHomeworksProgress: DayHomeworksProgress,
        val hasImportantWork: Boolean,
        val lessons: List<Lesson>,
        val messengerEntryPoint: Any
    )

    data class DayHomeworksProgress(
        val completedLessonsWithHomeworksCount: Int,
        val totalLessonsWithHomeworksCount: Int
    )

    data class Lesson(
        val comment: Any,
        val endDateTime: String,
        val group: Group,
        val hasAttachment: Boolean,
        val homework: Homework,
        val id: Long,
        val importantWorks: List<String>,
        val isCanceled: Boolean,
        val isEmpty: Any,
        val number: Int,
        val place: Any,
        val startDateTime: String,
        val subject: Subject,
        val teacher: Teacher,
        val theme: String,
        val workMarks: List<WorkMark>
    )

    data class Group(
        val id: Long,
        val name: String,
        val parentId: Any,
        val parentName: Any
    )

    data class Homework(
        val attachments: List<Any>,
        val isCompleted: Boolean,
        val text: String,
        val workIsAttachRequired: Boolean
    )

    data class Subject(
        val id: Long,
        val knowledgeArea: String,
        val name: String,
        val subjectMood: Any
    )

    data class Teacher(
        val avatarUrl: String,
        val firstName: String,
        val lastName: String,
        val middleName: String,
        val personId: Int
    )

    data class WorkMark(
        val marks: List<Mark>,
        val workId: Long
    )

    data class Mark(
        val id: Long,
        val mood: String,
        val value: String
    )

    interface API {
        @POST("auth")
        fun auth(
            @Query("username") username: String?,
            @Query("password") password: String?,
        ): Call<AuthResult>

        @GET("diary")
        fun diary(
            @Header("Access-Token") access_token: String?,
            @Header("User-ID") user_id: String?,
        ): Call<Diary>
    }

    fun api(): API {
        val retrofit = Retrofit.Builder()
            .baseUrl(apiUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(API::class.java)
    }
}