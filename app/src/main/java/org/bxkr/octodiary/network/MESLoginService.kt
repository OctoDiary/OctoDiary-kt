package org.bxkr.octodiary.network

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.Diary
import org.bxkr.octodiary.authPrefs
import org.bxkr.octodiary.baseEnqueue
import org.bxkr.octodiary.encodeToBase64
import org.bxkr.octodiary.get
import org.bxkr.octodiary.getRandomString
import org.bxkr.octodiary.hash
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.models.auth.RegisterBody
import org.bxkr.octodiary.models.auth.SchoolAuthBody
import org.bxkr.octodiary.models.auth.UserAuthenticationForMobileRequest
import org.bxkr.octodiary.network.NetworkService.MESAPIConfig
import org.bxkr.octodiary.network.interfaces.SchoolSessionAPI
import org.bxkr.octodiary.save
import org.bxkr.octodiary.widget.StatusWidget.Companion.setUpdateFor
import java.util.Date

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
        issueCall.baseEnqueue(
            errorFunction = { errorBody, httpCode, className ->
                Log.e(className, "$httpCode: ${errorBody.string()}")
            }
        ) { body ->
            val codeVerifier = getRandomString(80)
            val codeChallenge = encodeToBase64(hash(codeVerifier))
            context.authPrefs.save(
                "code_verifier" to codeVerifier,
                "client_id" to body.clientId,
                "client_secret" to body.clientSecret
            )
            val openUri = Uri.parse(NetworkService.BaseUrl.MOS_AUTH + "sps/oauth/ae")
                .buildUpon()
                .appendQueryParameter("scope", MESAPIConfig.SCOPE)
                .appendQueryParameter("access_type", MESAPIConfig.ACCESS_TYPE)
                .appendQueryParameter("response_type", MESAPIConfig.RESPONSE_TYPE)
                .appendQueryParameter("client_id", body.clientId)
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

    @Composable
    fun MosExchangeToken(code: String, hasToken: MutableState<Boolean>) {
        val context = LocalContext.current
        context.getSharedPreferences("auth", Context.MODE_PRIVATE).apply {
            val codeVerifier = getString("code_verifier", "")!!
            val clientId = getString("client_id", "")
            val clientSecret = getString("client_secret", "")

            val authorization = encodeToBase64("$clientId:$clientSecret".toByteArray())
            val authHeader = "Basic $authorization"

            val exchangeCall = NetworkService.mosAuthApi().tokenExchange(
                grantType = MESAPIConfig.GRANT_TYPE_CODE,
                redirectUri = MESAPIConfig.REDIRECT_URI,
                code,
                codeVerifier,
                authHeader
            )
            exchangeCall.baseEnqueue { body ->
                context.authPrefs.save("mos_refresh_token" to body.refreshToken)
                mosToMesToken(context, mosToken = body.accessToken, hasToken)
            }
        }
    }

    private fun mosToMesToken(
        context: Context,
        mosToken: String,
        hasToken: MutableState<Boolean> = mutableStateOf(false),
        onSet: () -> Unit = {},
    ) {
        val schoolAuthCall =
            NetworkService.schoolSessionApi(SchoolSessionAPI.getBaseUrl(Diary.MES)).mosTokenToMes(
                SchoolAuthBody(
                    UserAuthenticationForMobileRequest(
                        mosAccessToken = mosToken
                    )
                )
            )
        schoolAuthCall.baseEnqueue { body ->
            body.userAuthenticationForMobileResponse.meshAccessToken.also { token ->
                context.authPrefs.save(
                    "auth" to true,
                    "subsystem" to Diary.MES.ordinal,
                    "access_token" to token
                )
                context.mainPrefs.save(
                    "first_launch" to true
                )
                hasToken.value = true
                onSet()
                context.setUpdateFor(Date())
            }
        }
    }

    fun Context.refreshToken(onUpdated: () -> Unit) {
        getSharedPreferences("auth", Context.MODE_PRIVATE).apply {
            val clientId = getString("client_id", "")
            val clientSecret = getString("client_secret", "")

            val authorization = encodeToBase64("$clientId:$clientSecret".toByteArray())
            val authHeader = "Basic $authorization"
            val exchangeCall = NetworkService.mosAuthApi().tokenExchange(
                grantType = MESAPIConfig.GRANT_TYPE_REFRESH,
                refreshToken = authPrefs.get<String>("mos_refresh_token")!!,
                authHeader = authHeader
            )
            exchangeCall.baseEnqueue { body ->
                authPrefs.save("mos_refresh_token" to body.refreshToken)
                mosToMesToken(this@refreshToken, body.accessToken, onSet = {
                    DataService.token = authPrefs.get<String>("access_token")!!
                    DataService.updateUserId { onUpdated() }
                })
            }
        }
    }
}