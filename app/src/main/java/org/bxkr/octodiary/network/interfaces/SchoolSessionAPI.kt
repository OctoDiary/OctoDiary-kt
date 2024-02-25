package org.bxkr.octodiary.network.interfaces

import org.bxkr.octodiary.Diary
import org.bxkr.octodiary.models.auth.SchoolAuthBody
import org.bxkr.octodiary.models.auth.SchoolAuthResponse
import org.bxkr.octodiary.models.sessionuser.SessionUser
import org.bxkr.octodiary.network.MESOnly
import org.bxkr.octodiary.network.NetworkService.BaseUrl
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API for getting user session.
 *
 * baseUrl - [BaseUrl.MOS_SCHOOL] or [BaseUrl.MOSREG_DNEVNIK]
 */
interface SchoolSessionAPI {
    companion object : BaseUrls() {
        override fun getBaseUrl(diary: Diary): String {
            return when (diary) {
                Diary.MES -> BaseUrl.MOS_SCHOOL
                Diary.MySchool -> BaseUrl.MOSREG_DNEVNIK
            }
        }
    }

    /**
     * Gets info about current session user.
     *
     * @param body Access token inside the JSON body.
     * @return [SessionUser]
     */
    @MESOnly
    @POST("lms/api/sessions")
    fun sessionUser(
        @Body body: SessionUser.Body
    ): Call<SessionUser>

    /**
     * Issues a MES access token by a mos.ru access token.
     *
     * @param body JSON body containing a mos.ru access token.
     * @return [SchoolAuthResponse] containing MES token.
     */
    @MESOnly
    @POST("v3/auth/sudir/auth")
    fun mosTokenToMes(
        @Body body: SchoolAuthBody
    ): Call<SchoolAuthResponse>
}