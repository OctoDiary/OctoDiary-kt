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
                softwareId = APIConfig.softwareId,
                deviceType = APIConfig.deviceType,
                softwareStatement = APIConfig.mockSoftwareStatement
            ),
            authHeader = APIConfig.authIssuerSecret
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
                    val openUri = Uri.parse(NetworkService.BaseUrl.auth + "sps/oauth/ae")
                        .buildUpon()
                        .appendQueryParameter("scope", APIConfig.scope)
                        .appendQueryParameter("access_type", APIConfig.accessType)
                        .appendQueryParameter("response_type", APIConfig.responseType)
                        .appendQueryParameter("client_id", it.clientId)
                        .appendQueryParameter("redirect_uri", APIConfig.redirectUri)
                        .appendQueryParameter("prompt", APIConfig.prompt)
                        .appendQueryParameter("code_challenge", codeChallenge)
                        .appendQueryParameter("code_challenge_method", APIConfig.codeChallengeMethod)
                        .appendQueryParameter("bip_action_hint", APIConfig.bipActionHint)
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
                grantType = APIConfig.grantType,
                redirectUri = APIConfig.redirectUri,
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