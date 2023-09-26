package org.bxkr.octodiary.network

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import org.bxkr.octodiary.Diary
import org.bxkr.octodiary.encodeToBase64
import org.bxkr.octodiary.getRandomString
import org.bxkr.octodiary.hash
import org.bxkr.octodiary.models.RegisterBody
import org.bxkr.octodiary.models.RegisterResponse
import org.bxkr.octodiary.models.SchoolAuthBody
import org.bxkr.octodiary.models.SchoolAuthResponse
import org.bxkr.octodiary.models.TokenExchange
import org.bxkr.octodiary.models.UserAuthenticationForMobileRequest
import org.bxkr.octodiary.network.NetworkService.MESAPIConfig
import org.bxkr.octodiary.network.NetworkService.mesAuthApi
import org.bxkr.octodiary.saveToAuthPrefs

object MESLoginService {
    fun logInWithMosRu(context: Context) {
        val issueCall = NetworkService.mosAuthApi().register(
            body = RegisterBody(
                softwareId = MESAPIConfig.SOFTWARE_ID,
                deviceType = MESAPIConfig.DEVICE_TYPE,
                softwareStatement = MESAPIConfig.MOCK_SOFTWARE_STATEMENT
            ),
            authHeader = MESAPIConfig.AUTH_ISSUER_SECRET
        )
        issueCall.enqueue(object : BaseCallback<RegisterResponse>({ result ->
            result.body().let {
                if (it != null) {
                    val codeVerifier = getRandomString(80)
                    val codeChallenge = encodeToBase64(hash(codeVerifier))
                    context.saveToAuthPrefs(
                        "code_verifier" to codeVerifier,
                        "client_id" to it.clientId,
                        "client_secret" to it.clientSecret
                    )
                    val openUri = Uri.parse(NetworkService.BaseUrl.AUTH + "sps/oauth/ae")
                        .buildUpon()
                        .appendQueryParameter("scope", MESAPIConfig.SCOPE)
                        .appendQueryParameter("access_type", MESAPIConfig.ACCESS_TYPE)
                        .appendQueryParameter("response_type", MESAPIConfig.RESPONSE_TYPE)
                        .appendQueryParameter("client_id", it.clientId)
                        .appendQueryParameter("redirect_uri", MESAPIConfig.REDIRECT_URI)
                        .appendQueryParameter("prompt", MESAPIConfig.PROMPT)
                        .appendQueryParameter("code_challenge", codeChallenge)
                        .appendQueryParameter(
                            "code_challenge_method",
                            MESAPIConfig.CODE_CHALLENGE_METHOD
                        )
                        .appendQueryParameter("bip_action_hint", MESAPIConfig.BIP_ACTION_HINT)
                        .build()
                    val tabIntent = CustomTabsIntent.Builder().build()
                    tabIntent.launchUrl(context, openUri)
                }
            }
        }, errorFunction = { errorBody, httpCode ->
            Log.e("LogInWithMosRu", "$httpCode: ${errorBody.string()}")
        }) {})
    }

    @Composable
    fun ExchangeToken(context: Context, code: String, token: MutableState<String?>) {
        context.getSharedPreferences("auth", Context.MODE_PRIVATE).apply {
            val codeVerifier = getString("code_verifier", "")!!
            val clientId = getString("client_id", "")
            val clientSecret = getString("client_secret", "")

            val authorization = encodeToBase64("$clientId:$clientSecret".toByteArray())
            val authHeader = "Basic $authorization"

            val exchangeCall = NetworkService.mosAuthApi().tokenExchange(
                grantType = MESAPIConfig.GRANT_TYPE,
                redirectUri = MESAPIConfig.REDIRECT_URI,
                code,
                codeVerifier,
                authHeader
            )
            exchangeCall.enqueue(object : BaseCallback<TokenExchange>({ result ->
                result.body().let {
                    if (it != null) {
                        mosToMesToken(context, mosToken = it.accessToken, mesToken = token)
                    }
                }
            }) {})
        }
    }

    fun mosToMesToken(context: Context, mosToken: String, mesToken: MutableState<String?>) {
        val schoolAuthCall = mesAuthApi().mosTokenToMes(SchoolAuthBody(
            UserAuthenticationForMobileRequest(
                mosAccessToken = mosToken
            )
        ))
        schoolAuthCall.enqueue(object : BaseCallback<SchoolAuthResponse>({
            it.body()!!.userAuthenticationForMobileResponse.meshAccessToken.also { token ->
                mesToken.value = token
                context.saveToAuthPrefs(
                    "auth" to true,
                    "subsystem" to Diary.MES.ordinal,
                    "access_token" to token
                )
            }
        }) {})
    }
}