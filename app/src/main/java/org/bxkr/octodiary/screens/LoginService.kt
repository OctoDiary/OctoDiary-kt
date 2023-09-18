package org.bxkr.octodiary.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.core.content.edit
import org.bxkr.octodiary.BaseCallback
import org.bxkr.octodiary.NetworkService
import org.bxkr.octodiary.NetworkService.APIConfig
import org.bxkr.octodiary.NetworkService.mesAuthApi
import org.bxkr.octodiary.encodeToBase64
import org.bxkr.octodiary.getRandomString
import org.bxkr.octodiary.hash
import org.bxkr.octodiary.models.RegisterBody
import org.bxkr.octodiary.models.RegisterResponse
import org.bxkr.octodiary.models.SchoolAuthBody
import org.bxkr.octodiary.models.SchoolAuthResponse
import org.bxkr.octodiary.models.TokenExchange
import org.bxkr.octodiary.models.UserAuthenticationForMobileRequest

object LoginService {
    @Composable
    fun LogInWithMosRu(context: Context) {
        val issueCall = NetworkService.mosAuthApi().register(
            body = RegisterBody(
                softwareId = APIConfig.SOFTWARE_ID,
                deviceType = APIConfig.DEVICE_TYPE,
                softwareStatement = APIConfig.MOCK_SOFTWARE_STATEMENT
            ),
            authHeader = APIConfig.AUTH_ISSUER_SECRET
        )
        issueCall.enqueue(object : BaseCallback<RegisterResponse>({ result ->
            result.body().let {
                if (it != null) {
                    val codeVerifier = getRandomString(80)
                    val codeChallenge = encodeToBase64(hash(codeVerifier))
                    context.getSharedPreferences("auth", Context.MODE_PRIVATE).edit(commit = true) {
                        putString("code_verifier", codeVerifier)
                        putString("client_id", it.clientId)
                        putString("client_secret", it.clientSecret)
                    }
                    val openUri = Uri.parse(NetworkService.BaseUrl.AUTH + "sps/oauth/ae")
                        .buildUpon()
                        .appendQueryParameter("scope", APIConfig.SCOPE)
                        .appendQueryParameter("access_type", APIConfig.ACCESS_TYPE)
                        .appendQueryParameter("response_type", APIConfig.RESPONSE_TYPE)
                        .appendQueryParameter("client_id", it.clientId)
                        .appendQueryParameter("redirect_uri", APIConfig.REDIRECT_URI)
                        .appendQueryParameter("prompt", APIConfig.PROMPT)
                        .appendQueryParameter("code_challenge", codeChallenge)
                        .appendQueryParameter("code_challenge_method", APIConfig.CODE_CHALLENGE_METHOD)
                        .appendQueryParameter("bip_action_hint", APIConfig.BIP_ACTION_HINT)
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
                grantType = APIConfig.GRANT_TYPE,
                redirectUri = APIConfig.REDIRECT_URI,
                code,
                codeVerifier,
                authHeader
            )
            exchangeCall.enqueue(object : BaseCallback<TokenExchange>({ result ->
                result.body().let {
                    if (it != null) {
                        mosToMesToken(mosToken = it.accessToken, mesToken = token)
                    }
                }
            }) {})
        }
    }

    fun mosToMesToken(mosToken: String, mesToken: MutableState<String?>) {
        val schoolAuthCall = mesAuthApi().mosTokenToMes(SchoolAuthBody(
            UserAuthenticationForMobileRequest(
                mosAccessToken = mosToken
            )
        ))
        schoolAuthCall.enqueue(object : BaseCallback<SchoolAuthResponse>({
            mesToken.value = it.body()!!.userAuthenticationForMobileResponse.meshAccessToken
        }) {})
    }
}