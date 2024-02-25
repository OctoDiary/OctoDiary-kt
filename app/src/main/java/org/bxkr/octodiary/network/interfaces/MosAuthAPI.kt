package org.bxkr.octodiary.network.interfaces

import org.bxkr.octodiary.Diary
import org.bxkr.octodiary.models.auth.RegisterBody
import org.bxkr.octodiary.models.auth.RegisterResponse
import org.bxkr.octodiary.models.auth.TokenExchange
import org.bxkr.octodiary.network.MESLoginService
import org.bxkr.octodiary.network.MESOnly
import org.bxkr.octodiary.network.NetworkService.BaseUrl
import org.bxkr.octodiary.network.NetworkService.MESAPIConfig
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Authorization gate of mos.ru.
 *
 * baseUrl - [BaseUrl.MOS_AUTH]
 */
interface MosAuthAPI {
    companion object : BaseUrls() {
        override fun getBaseUrl(diary: Diary): String {
            return when (diary) {
                Diary.MES -> BaseUrl.MOS_AUTH
                Diary.MySchool -> throw NoSuchFieldError()
            }
        }
    }

    /**
     * Issues a client registration config for
     * building the link for authorization.
     *
     * @param body Fully mocked required JSON body.
     * @param authHeader Server secret.
     * @return Authorization config - [RegisterResponse].
     * @see MESAPIConfig.AUTH_ISSUER_SECRET
     */
    @MESOnly
    @POST("sps/oauth/register")
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
     * @see MESLoginService.MosExchangeToken
     */
    @MESOnly
    @POST("sps/oauth/te")
    @FormUrlEncoded
    fun tokenExchange(
        @Field("grant_type") grantType: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("code") code: String,
        @Field("code_verifier") codeVerifier: String,
        @Header("Authorization") authHeader: String,
    ): Call<TokenExchange>

    @MESOnly
    @POST("sps/oauth/te")
    @FormUrlEncoded
    fun tokenExchange(
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String,
        @Header("Authorization") authHeader: String,
    ): Call<TokenExchange.Refresh>
}