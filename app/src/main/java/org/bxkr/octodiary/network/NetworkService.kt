package org.bxkr.octodiary.network

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.bxkr.octodiary.R
import org.bxkr.octodiary.models.diary.Diary
import org.bxkr.octodiary.models.lesson.Lesson
import org.bxkr.octodiary.models.mark.MarkDetails
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
    enum class Server(
        @StringRes val serverName: Int,
        @DrawableRes val drawableRes: Int,
        val url: String
    ) {
        SCHOOL_MOSREG(
            R.string.school_mosreg_name,
            R.drawable.ic_round_location_city_24,
            "https://api.school.mosreg.ru/mobile/v6.0/"
        ),
        DNEVNIK_RU(
            R.string.dnevnik_ru,
            R.drawable.ic_round_waving_hand_24,
            "https://api.dnevnik.ru/mobile/v6.0/"
        )
    }

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

        @GET("persons/{person_id}/groups/{group_id}/marks/{lesson_id}/markDetails")
        fun markDetails(
            @Path("person_id") personId: Long,
            @Path("group_id") groupId: Long,
            @Path("mark_id") markId: Long,
            @Header("Access-Token") accessToken: String?,
        ): Call<MarkDetails>
    }

    fun api(server: Server): API {
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(API::class.java)
    }
}