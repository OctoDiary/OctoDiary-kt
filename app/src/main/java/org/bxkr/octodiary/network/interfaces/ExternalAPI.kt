package org.bxkr.octodiary.network.interfaces

import org.bxkr.octodiary.network.NetworkService
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ExternalAPI {
    @POST("stats/enter")
    fun sendStat(
        @Query("userId") userId: Long,
        @Header("verify-token") verifyToken: String = NetworkService.ExternalIntegrationConfig.VERIFY_TOKEN
    ): Call<Unit>
}