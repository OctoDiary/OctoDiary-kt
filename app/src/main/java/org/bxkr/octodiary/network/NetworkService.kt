package org.bxkr.octodiary.network

import org.bxkr.octodiary.models.Diary
import org.bxkr.octodiary.models.User
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

        @GET("user")
        fun user(
            @Header("Access-Token") access_token: String?,
            @Header("User-ID") user_id: String?,
        ): Call<User>
    }

    fun api(): API {
        val retrofit = Retrofit.Builder()
            .baseUrl(apiUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(API::class.java)
    }
}