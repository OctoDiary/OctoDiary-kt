package org.bxkr.octodiary.network

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import org.bxkr.octodiary.Diary
import org.bxkr.octodiary.Screen
import org.bxkr.octodiary.authPrefs
import org.bxkr.octodiary.baseEnqueue
import org.bxkr.octodiary.extendedEnqueue
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.models.auth.RegionalCredentialsResponse
import org.bxkr.octodiary.save
import org.bxkr.octodiary.screenLive
import java.util.UUID

object MySchoolLoginService {
    fun logInWithEsia(context: Context) {
        val tabIntent = CustomTabsIntent.Builder().build()
        tabIntent.launchUrl(context, Uri.parse(NetworkService.MySchoolAPIConfig.run {
            AUTH_URL_TEMPLATE.format(
                REDIRECT_URI,
                UUID.randomUUID().toString()
            )
        }))
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
                    "auth" to true,
                    "subsystem" to Diary.MySchool.ordinal,
                    "access_token" to token
                )
                mainPrefs.save(
                    "first_launch" to true
                )
                screenLive.postValue(Screen.MainNav)
            }
        }
    }
}