package org.bxkr.octodiary.network

import org.bxkr.octodiary.models.diary.Diary
import org.bxkr.octodiary.models.lesson.Lesson
import org.bxkr.octodiary.models.rating.RatingClass
import org.bxkr.octodiary.models.user.User
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

object NetworkService {
    private const val apiUrl =
        "https://api.school.mosreg.ru/mobile/v6.0/"

    data class AuthResult(
        val credentials: AuthResultCredentials,
    )

    data class AuthResultCredentials(
        val accessToken: String,
        val userId: Long,
    )

    data class AuthRequestBody(
        val username: String,
        val password: String,
        val clientId: String,
        val clientSecret: String,
        val scope: String,
    )

    interface API {
        @POST("authorizations/byCredentials")
        fun auth(
            @Body authRequestBody: AuthRequestBody
        ): Call<AuthResult>

        @GET("persons/{person_id}/schools/{school_id}/groups/{group_id}/diary")
        fun diary(
            @Path("person_id") personId: Long,
            @Path("school_id") schoolId: Long,
            @Path("group_id") groupId: Long,
            @Header("Access-Token") accessToken: String?,
            @Query("id") id: String = "",
            @Query("loadType") loadType: String = "Undefined",
        ): Call<Diary>

        @GET("users/{user_id}/context")
        fun user(
            @Path("user_id") userId: Long,
            @Header("Access-Token") accessToken: String?,
        ): Call<User>

        @GET("persons/{person_id}/groups/{group_id}/rating")
        fun rating(
            @Path("person_id") personId: Long,
            @Path("group_id") groupId: Long,
            @Header("Access-Token") accessToken: String?,
        ): Call<RatingClass>

        @GET("persons/{person_id}/groups/{group_id}/lessons/{lesson_id}/lessonDetails")
        fun lessonDetails(
            @Path("person_id") personId: Long,
            @Path("group_id") groupId: Long,
            @Path("lesson_id") lessonId: Long,
            @Header("Access-Token") accessToken: String?,
        ): Call<Lesson>
    }

    fun api(): API {
        val retrofit = Retrofit.Builder()
            .baseUrl(apiUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(API::class.java)
    }
}