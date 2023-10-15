package org.bxkr.octodiary.network

import org.bxkr.octodiary.models.auth.RegisterBody
import org.bxkr.octodiary.models.auth.RegisterResponse
import org.bxkr.octodiary.models.auth.SchoolAuthBody
import org.bxkr.octodiary.models.auth.SchoolAuthResponse
import org.bxkr.octodiary.models.auth.TokenExchange
import org.bxkr.octodiary.models.events.EventsResponse
import org.bxkr.octodiary.models.mark.MarkInfo
import org.bxkr.octodiary.models.profilesid.ProfileId
import org.bxkr.octodiary.models.profilesid.ProfilesId
import org.bxkr.octodiary.models.sessionuser.SessionUser
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
    object MESAPIConfig {
        const val SCOPE = "birthday contacts openid profile snils blitz_change_password blitz_user_rights blitz_qr_auth"
        const val RESPONSE_TYPE = "code"
        const val PROMPT = "login"
        const val BIP_ACTION_HINT = "used_sms"
        const val REDIRECT_URI = "dnevnik-mes://oauth2redirect"
        const val ACCESS_TYPE = "offline"
        const val CODE_CHALLENGE_METHOD = "S256"
        const val AUTH_ISSUER_SECRET = "Bearer FqzGn1dTJ9BQCHgV0rmMjtYFIgaFf9TrGVEzgtju-zbtIbeJSkIyDcl0e2QMirTNpEqovTT8NvOLZI0XklVEIw"
        const val MOCK_SOFTWARE_STATEMENT = "eyJ0eXAiOiJKV1QiLCJibGl0ejpraW5kIjoiU09GVF9TVE0iLCJhbGciOiJSUzI1NiJ9.eyJncmFudF90eXBlcyI6WyJhdXRob3JpemF0aW9uX2NvZGUiLCJwYXNzd29yZCIsImNsaWVudF9jcmVkZW50aWFscyIsInJlZnJlc2hfdG9rZW4iXSwic2NvcGUiOiJiaXJ0aGRheSBibGl0el9jaGFuZ2VfcGFzc3dvcmQgYmxpdHpfYXBpX3VzZWNfY2hnIGJsaXR6X3VzZXJfcmlnaHRzIGNvbnRhY3RzIG9wZW5pZCBwcm9maWxlIGJsaXR6X3JtX3JpZ2h0cyBibGl0el9hcGlfc3lzX3VzZXJfY2hnIGJsaXR6X2FwaV9zeXNfdXNlcnMgYmxpdHpfYXBpX3N5c191c2Vyc19jaGcgc25pbHMgYmxpdHpfYXBpX3N5c191c2VjX2NoZyBibGl0el9xcl9hdXRoIiwianRpIjoiYTVlM2NiMGQtYTBmYi00ZjI1LTk3ODctZTllYzRjOTFjM2ZkIiwic29mdHdhcmVfaWQiOiJkbmV2bmlrLm1vcy5ydSIsInNvZnR3YXJlX3ZlcnNpb24iOiIxIiwicmVzcG9uc2VfdHlwZXMiOlsiY29kZSIsInRva2VuIl0sImlhdCI6MTYzNjcyMzQzOSwiaXNzIjoiaHR0cHM6Ly9sb2dpbi5tb3MucnUiLCJyZWRpcmVjdF91cmlzIjpbImh0dHA6Ly9sb2NhbGhvc3QiLCJzaGVsbDovL2F1dGhwb3J0YWwiLCJkbmV2bmlrLW1lczovL29hdXRoMnJlZGlyZWN0IiwiaHR0cHM6Ly9zY2hvb2wubW9zLnJ1L2F1dGgvbWFpbi9jYWxsYmFjayIsImh0dHBzOi8vc2Nob29sLm1vcy5ydS92MS9vYXV0aC9jYWxsYmFjayIsImh0dHBzOi8vZG5ldm5pay5tb3MucnUvc3VkaXIiLCJodHRwczovL3NjaG9vbC5tb3MucnUvYXV0aC9jYWxsYmFjayIsImh0dHA6Ly9kbmV2bmlrLm1vcy5ydS9zdWRpciJdLCJhdWQiOlsiZG5ldm5pay5tb3MucnUiXX0.EERWGw5RGhLQ1vBiGrdG_eJrCyJEyan-H4UWT1gr4B9ZfP58pyJoVw5wTt8YFqzwbvHNQBnvrYfMCzOkHpsU7TxlETJpbWcWbnV5JI-inzXGyKCic2fAVauVCjos3v6AFiP6Uw6ZXIC6b9kQ5WgRVM66B9UwAB2MKTThTohJP7_MNZJ0RiOd8RLlvF4C7yfuqoGU2-KWLwr78ATniTvYFWszl8jAi_SiD9Ai1GWW4mO9-JQ01f4N9umC5Cy2tYiZhxbaz2rOsAQBBjY6rbCCJbCpb1lyGfs2qhhAB-ODGTq7W7r1WBlAm5EXlPpuW_9pi8uxdxiqjkG3d6xy7h7gtQ"
        const val SOFTWARE_ID = "dnevnik.mos.ru"
        const val DEVICE_TYPE = "android_phone"
        const val GRANT_TYPE = "authorization_code"
        const val FAMILYMP = "familymp"
        const val DIARY_MOBILE = "diary-mobile"
    }

    object MESRole {
        const val STUDENT = "student"
    }

    object MySchoolAPIConfig {
        const val AUTH_URL_TEMPLATE =
            "https://authedu.mosreg.ru/v3/auth/esia/login?redirect_url=%s&state=%s"
        const val REDIRECT_URI = "dnevnik-mes://authRegionRedirect"
    }

    object BaseUrl {
        const val AUTH = "https://login.mos.ru/"
        const val DNEVNIK = "https://dnevnik.mos.ru/"
        const val SCHOOL = "https://school.mos.ru/"
    }

    /**
     * Authorization gate of mos.ru.
     *
     * baseUrl - [BaseUrl.AUTH]
     */
    interface MosAuthAPI {
        /**
         * Issues a client registration config for
         * building the link for authorization.
         *
         * @param body Fully mocked required JSON body.
         * @param authHeader Server secret.
         * @return Authorization config - [RegisterResponse].
         * @see MESAPIConfig.AUTH_ISSUER_SECRET
         */
        @POST("/sps/oauth/register")
        fun register(
            @Body body: RegisterBody,
            @Header("Authorization") authHeader: String
        ): Call<RegisterResponse>

        /**
         * Exchanges code to token.
         *
         * @param grantType Constant grant type.
         * @param redirectUri Constant redirect URI (apparently unnecessary).
         * @param code Code caught from user's web gate auth session.
         * @param codeVerifier Code verifier remembered by the app since the [auth link generation][MESLoginService.logInWithMosRu].
         * @param authHeader "clientId:clientSecret" ([received in auth config][MESLoginService.logInWithMosRu]) pair encoded in base64.
         * @return [TokenExchange] containing mos.ru access token.
         * @see MESLoginService.ExchangeToken
         */
        @POST("/sps/oauth/te")
        fun tokenExchange(
            @Query("grant_type") grantType: String,
            @Query("redirect_uri") redirectUri: String,
            @Query("code") code: String,
            @Query("code_verifier") codeVerifier: String,
            @Header("Authorization") authHeader: String
        ): Call<TokenExchange>
    }

    /**
     * Old MES API which however is used.
     *
     * baseUrl - [BaseUrl.DNEVNIK]
     */
    interface DSchoolAPI {
        /**
         * This is a final **REQUIRED** stage of authorization.
         * Token that haven't been used in this request is not eligible for other requests.
         * Basically, this method just returns list containing all user's profiles id.
         * @param authHeader Token to be activated.
         * @return List of [ProfileId]s.
         **/
        @GET("/acl/api/users/profile_info")
        fun profilesId(
            @Header("auth-token") authHeader: String
        ): Call<ProfilesId>
    }

    /**
     * Main MES API.
     *
     * baseUrl - [BaseUrl.SCHOOL]
     */
    interface SchoolAPI {
        /**
         * Issues a MES access token by a mos.ru access token.
         *
         * @param body JSON body containing a mos.ru access token.
         * @return [SchoolAuthResponse] containing MES token.
         */
        @POST("/v3/auth/sudir/auth")
        fun mosTokenToMes(
            @Body body: SchoolAuthBody
        ): Call<SchoolAuthResponse>

        /**
         * Gets info about current session user.
         *
         * @param body Access token inside the JSON body.
         * @return [SessionUser]
         */
        @POST("/lms/api/sessions")
        fun sessionUser(
            @Body body: SessionUser.Body
        ): Call<SessionUser>

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
        @GET("/api/eventcalendar/v1/api/events")
        fun events(
            @Header("Authorization") authHeader: String,
            @Query("person_ids") personIds: String,
            @Query("begin_date") beginDate: String,
            @Query("end_date") endDate: String,
            @Query("expand") expandFields: String? = null,
            @Header("X-Mes-Subsystem") mesSubsystem: String = MESAPIConfig.FAMILYMP,
            @Header("X-Mes-Role") mesRole: String = MESRole.STUDENT, // FUTURE: USES_STUDENT_ROLE
            @Header("Client-Type") clientType: String = MESAPIConfig.DIARY_MOBILE
        ): Call<EventsResponse>

        /**
         * Gets info about mark.
         *
         * @param accessToken Access token.
         * @param markId Mark ID.
         * @param studentId Student ID.
         * @param mesSubsystem MES subsystem (["familymp"][MESAPIConfig.FAMILYMP] by default).
         * @return [MarkInfo]
         */
        @GET("/api/family/mobile/v1/marks/{mark_id}")
        fun markInfo(
            @Header("auth-token") accessToken: String,
            @Path("mark_id") markId: Int,
            @Query("student_id") studentId: Int,
            @Header("X-Mes-Subsystem") mesSubsystem: String = MESAPIConfig.FAMILYMP
        ): Call<MarkInfo>
    }

    fun mosAuthApi(): MosAuthAPI {
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrl.AUTH)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(MosAuthAPI::class.java)
    }

    fun dnevnikApi(): DSchoolAPI {
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrl.DNEVNIK)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(DSchoolAPI::class.java)
    }

    fun mesApi(): SchoolAPI {
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrl.SCHOOL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(SchoolAPI::class.java)
    }
}