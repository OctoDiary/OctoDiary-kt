package org.bxkr.octodiary.network.interfaces

import android.net.Uri
import org.bxkr.octodiary.Diary
import org.bxkr.octodiary.models.auth.EsiaExchange
import org.bxkr.octodiary.models.classranking.RankingMember
import org.bxkr.octodiary.models.events.EventsResponse
import org.bxkr.octodiary.models.rankingforsubject.RankingForSubject
import org.bxkr.octodiary.models.subjectranking.SubjectRanking
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.network.NetworkService.BaseUrl
import org.bxkr.octodiary.network.NetworkService.MESAPIConfig
import org.bxkr.octodiary.network.RegionalOnly
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * Secondary API.
 *
 * baseUrl - [BaseUrl.MOS_SCHOOL] or [BaseUrl.MOSREG_SECONDARY]
 */
interface SecondaryAPI {
    companion object : BaseUrls() {
        override fun getBaseUrl(diary: Diary): String {
            return when (diary) {
                Diary.MES -> BaseUrl.MOS_SCHOOL
                Diary.MySchool -> BaseUrl.MOSREG_SECONDARY
            }
        }

        /**
         * Creates avatar Uri from person ID.
         *
         * @param diary [Diary].
         * @param personId Person ID (guid-like).
         * @return A Uri of avatar on one of MES' servers.
         */
        fun getAvatarUrl(diary: Diary, personId: String): Uri {
            return Uri.parse(getBaseUrl(diary) + "avatars/" + personId)
        }
    }

    /**
     * Exchanges code to token.
     *
     * @param code Code caught from user's web gate auth session.
     * @param state State remembered on the auth initialization.
     * @return Token inside of [EsiaExchange]
     */
    @GET("v3/auth/token")
    fun esiaExchangeToken(
        @Query("code") code: String,
        @Query("state") state: String
    ): Call<EsiaExchange>

    /**
     * Refreshes a token.
     *
     * @param bearerToken Bearer auth header, e.g. 'Bearer AccessToken123'.
     * @return Access token.
     */
    @RegionalOnly
    @GET("v2/token/refresh")
    fun refreshToken(
        @Header("Authorization") bearerToken: String
    ): Call<String>

    /**
     * Gets events for student.
     *
     * @param authHeader Bearer-like string ("Bearer $accessToken").
     * @param personIds ID of students to parse events.
     * @param beginDate Start of event calendar in yyyy-MM-dd format.
     * @param endDate End of event calendar in yyyy-MM-dd format.
     * @param expandFields Fields of events to expand (e.g. 'homework,marks').
     * @param mesSubsystem MES subsystem (["familymp"][MESAPIConfig.FAMILYMP] by default).
     * @param mesRole MES role.
     * @param clientType Client type (["diary-mobile"][MESAPIConfig.DIARY_MOBILE]).
     * @return [EventsResponse]
     */
    @GET("api/eventcalendar/v1/api/events")
    fun events(
        @Header("Authorization") authHeader: String,
        @Query("person_ids") personIds: String,
        @Query("begin_date") beginDate: String,
        @Query("end_date") endDate: String,
        @Query("expand") expandFields: String? = null,
        @Header("X-Mes-Subsystem") mesSubsystem: String = MESAPIConfig.FAMILYMP,
        @Header("X-Mes-Role") mesRole: String = NetworkService.MESRole.STUDENT, // FUTURE: USES_STUDENT_ROLE
        @Header("Client-Type") clientType: String = MESAPIConfig.DIARY_MOBILE
    ): Call<EventsResponse>

    /**
     * Gets ranking in class.
     *
     * @param accessToken Access token.
     * @param personId Person ID.
     * @param date Ranking date in yyyy-MM-dd format.
     * @param mesSubsystem MES subsystem (["familymp"][MESAPIConfig.FAMILYMP] by default).
     * @return List of [RankingMember].
     */
    @GET("api/ej/rating/v1/rank/class")
    fun classRanking(
        @Header("auth-token") accessToken: String,
        @Query("personId") personId: String,
        @Query("date") date: String,
        @Header("X-Mes-Subsystem") mesSubsystem: String = MESAPIConfig.FAMILYMP
    ): Call<List<RankingMember>>

    /**
     * Gets ranking by subjects.
     *
     * @param accessToken Access token.
     * @param personId Person ID.
     * @param date Ranking date in yyyy-MM-dd format.
     * @param mesSubsystem MES subsystem (["familymp"][MESAPIConfig.FAMILYMP] by default).
     * @return List of [SubjectRanking].
     */
    @GET("api/ej/rating/v1/rank/subjects")
    fun subjectRanking(
        @Header("auth-token") accessToken: String,
        @Query("personId") personId: String,
        @Query("date") date: String,
        @Header("X-Mes-Subsystem") mesSubsystem: String = MESAPIConfig.FAMILYMP
    ): Call<List<SubjectRanking>>

    /**
     * Gets ranking for specific subject.
     *
     * @param accessToken Access token.
     * @param personId Person ID.
     * @param classUnitId Class unit ID.
     * @param date Ranking date in yyyy-MM-dd format.
     * @param subjectId Subject ID.
     * @param mesSubsystem MES subsystem (["familymp"][MESAPIConfig.FAMILYMP] by default).
     * @return List of [RankingForSubject].
     */
    @GET("api/ej/rating/v1/rank/class")
    fun rankingForSubject(
        @Header("auth-token") accessToken: String,
        @Query("personId") personId: String,
        @Query("classUnitId") classUnitId: Long,
        @Query("date") date: String,
        @Query("subjectId") subjectId: Long,
        @Header("X-Mes-Subsystem") mesSubsystem: String = MESAPIConfig.FAMILYMP
    ): Call<List<RankingForSubject>>
}