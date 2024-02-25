package org.bxkr.octodiary.network

import org.bxkr.octodiary.network.interfaces.DSchoolAPI
import org.bxkr.octodiary.network.interfaces.ExternalAPI
import org.bxkr.octodiary.network.interfaces.MainSchoolAPI
import org.bxkr.octodiary.network.interfaces.MosAuthAPI
import org.bxkr.octodiary.network.interfaces.RegionalAuthAPI
import org.bxkr.octodiary.network.interfaces.SchoolSessionAPI
import org.bxkr.octodiary.network.interfaces.SecondaryAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

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
        const val GRANT_TYPE_CODE = "authorization_code"
        const val GRANT_TYPE_REFRESH = "refresh_token"
        const val FAMILYMP = "familymp"
        const val DIARY_MOBILE = "diary-mobile"
        const val DATE_FIELD = "date"
        const val ASCENDING = "asc"
    }

    object MESRole {
        const val STUDENT = "student"
    }

    object MySchoolAPIConfig {
        const val ESIA_AUTH_URL_TEMPLATE =
            "%sv3/auth/esia/login?redirect_url=%s&state=%s"
        const val REDIRECT_URI = "dnevnik-mes://authRegionRedirect"
    }

    object ExternalIntegrationConfig {
        const val BOT_AUTH_URL =
            "https://octodiary.dsop.online/redir2bot?token=%s&system=%s&test=%s"
        const val TELEGRAM_CHANNEL_URL = "https://t.me/OctoDiary"
        const val TELEGRAM_REPORT_URL = "https://t.me/OctoDiaryBot?start=feedback"
        const val VERIFY_TOKEN = "930dd75f10cd6288f1bbd248cd2a79690e58fac6702a5fdcb77ea560269d2500"
    }

    object BaseUrl {
        const val MOS_AUTH = "https://login.mos.ru/"
        const val MOS_DNEVNIK = "https://dnevnik.mos.ru/"
        const val MOS_SCHOOL = "https://school.mos.ru/"
        const val MOS_SCHOOL_API = "https://school.mos.ru/api/"

        const val MOSREG_SECONDARY = "https://authedu.mosreg.ru/"
        const val MOSREG_SCHOOL_API = "https://api.myschool.mosreg.ru/"
        const val MOSREG_DNEVNIK = "https://myschool.mosreg.ru/"

        const val EXTERNAL_API = "https://octodiary.dsop.online/"
    }

    private inline fun <reified T> baseApiConstructor(baseUrl: String): T {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(T::class.java)
    }

    fun mosAuthApi() = baseApiConstructor<MosAuthAPI>(BaseUrl.MOS_AUTH)
    fun regionalAuthApi() = baseApiConstructor<RegionalAuthAPI>(BaseUrl.MOSREG_SECONDARY)
    fun externalApi() = baseApiConstructor<ExternalAPI>(BaseUrl.EXTERNAL_API)

    fun dSchoolApi(baseUrl: String) = baseApiConstructor<DSchoolAPI>(baseUrl)
    fun mainSchoolApi(baseUrl: String) = baseApiConstructor<MainSchoolAPI>(baseUrl)
    fun schoolSessionApi(baseUrl: String) = baseApiConstructor<SchoolSessionAPI>(baseUrl)
    fun secondaryApi(baseUrl: String) = baseApiConstructor<SecondaryAPI>(baseUrl)
}