package org.bxkr.octodiary.network

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import org.bxkr.octodiary.Diary
import org.bxkr.octodiary.Screen
import org.bxkr.octodiary.authPrefs
import org.bxkr.octodiary.baseEnqueue
import org.bxkr.octodiary.extendedEnqueue
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.models.auth.RegionalCredentialsResponse
import org.bxkr.octodiary.network.interfaces.SecondaryAPI
import org.bxkr.octodiary.save
import org.bxkr.octodiary.screenLive
import java.util.UUID

object MySchoolLoginService {
    lateinit var diary: Diary

    fun logInWithEsia(context: Context, diary: Diary) {
        this.diary = diary
        val tabIntent = CustomTabsIntent.Builder().build()
        tabIntent.launchUrl(context, Uri.parse(NetworkService.MySchoolAPIConfig.run {
            val guid = UUID.randomUUID().toString()
            context.authPrefs.save("state" to guid)
            ESIA_AUTH_URL_TEMPLATE.format(
                SecondaryAPI.getBaseUrl(diary), REDIRECT_URI, guid
            )
        }))
    }

    @Composable
    fun EsiaExchangeToken(code: String, token: MutableState<String?>) {
        val context = LocalContext.current
        NetworkService.secondaryApi(SecondaryAPI.getBaseUrl(diary)).esiaExchangeToken(
            code, context.authPrefs.get<String>("state")!!
        ).baseEnqueue {
            context.authPrefs.save(
                "auth" to true,
                "subsystem" to diary.ordinal,
                "access_token" to it.token
            )
            context.mainPrefs.save(
                "first_launch" to true
            )

            token.value = it.token
        }
    }

    fun Context.logInWithPassword(login: String, password: String) {
        val api = NetworkService.regionalAuthApi()
        api.enterCredentials(
            RegionalCredentialsResponse.Body(login, password)
        ).baseEnqueue { session ->
            api.exchangeToken(session.authenticationToken).extendedEnqueue {
                val token =
                    it.headers().values("Set-Cookie").first { it1 -> it1.contains("aupd_token") }
                        .split(";").map { it1 -> it1.split("=") }
                        .first { it1 -> it1.contains("aupd_token") }[1]
                authPrefs.save(
                    "auth" to true, "subsystem" to Diary.MySchool.ordinal, "access_token" to token
                )
                mainPrefs.save(
                    "first_launch" to true
                )
                screenLive.postValue(Screen.MainNav)
            }
        }
    }
}