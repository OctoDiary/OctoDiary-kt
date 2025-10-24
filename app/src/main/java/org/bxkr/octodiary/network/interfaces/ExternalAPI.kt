package org.bxkr.octodiary.network.interfaces

import org.bxkr.octodiary.models.ParagraphResponse
import org.bxkr.octodiary.network.NetworkService
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ExternalAPI {
    @POST("stats/enter")
    fun sendStat(
        @Query("systemId") system: Int,
        @Query("userHashId") userHashId: String,
        @Header("verify-token") verifyToken: String = NetworkService.ExternalIntegrationConfig.VERIFY_TOKEN
    ): Call<Unit>

    @POST("paragraphs/request")
    fun requestParagraph(
        @Query("topic") topic: String,
        @Query("language") language: String = "ru",
        @Query("maxLength") maxLength: Int? = null,
        @Header("verify-token") verifyToken: String = NetworkService.ExternalIntegrationConfig.VERIFY_TOKEN
    ): Call<ParagraphResponse>
}