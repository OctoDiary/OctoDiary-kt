package org.bxkr.octodiary

import org.bxkr.octodiary.models.RegisterBody
import org.bxkr.octodiary.models.RegisterResponse
import org.bxkr.octodiary.models.SchoolAuthBody
import org.bxkr.octodiary.models.SchoolAuthResponse
import org.bxkr.octodiary.models.TokenExchange
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

object NetworkService {
    object APIConfig {
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
    }

    object BaseUrl {
        const val AUTH = "https://login.mos.ru/"
        const val SCHOOL = "https://school.mos.ru/"
    }

    interface MosAuthAPI {
        @POST("/sps/oauth/register")
        fun register(
            @Body body: RegisterBody,
            @Header("Authorization") authHeader: String
        ): Call<RegisterResponse>

        @POST("/sps/oauth/te")
        fun tokenExchange(
            @Query("grant_type") grantType: String,
            @Query("redirect_uri") redirectUri: String,
            @Query("code") code: String,
            @Query("code_verifier") codeVerifier: String,
            @Header("Authorization") authHeader: String
        ): Call<TokenExchange>
    }

    interface MESAuthAPI {
        @POST("/v3/auth/sudir/auth")
        fun mosTokenToMes(
            @Body body: SchoolAuthBody
        ): Call<SchoolAuthResponse>
    }

    fun mosAuthApi(): MosAuthAPI {
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrl.AUTH)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(MosAuthAPI::class.java)
    }

    fun mesAuthApi(): MESAuthAPI {
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrl.SCHOOL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(MESAuthAPI::class.java)
    }
}