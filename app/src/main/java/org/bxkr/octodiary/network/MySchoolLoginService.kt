package org.bxkr.octodiary.network

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import org.bxkr.octodiary.Diary
import org.bxkr.octodiary.Screen
import org.bxkr.octodiary.authPrefs
import org.bxkr.octodiary.baseEnqueue
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.network.NetworkService.MySchoolAPIConfig.LOGIN_PASSWORD_TEMPLATE
import org.bxkr.octodiary.network.interfaces.SecondaryAPI
import org.bxkr.octodiary.save
import org.bxkr.octodiary.screenLive
import org.bxkr.octodiary.widget.StatusWidget.Companion.setUpdateFor
import java.util.Date
import java.util.UUID

object MySchoolLoginService {
    lateinit var diary: Diary

    fun logInWithEsia(context: Context, diary: Diary) {
        this.diary = diary
        val tabIntent = CustomTabsIntent.Builder().build()
        tabIntent.launchUrl(context, NetworkService.MySchoolAPIConfig.run {
            val guid = UUID.randomUUID().toString()
            context.authPrefs.save("state" to guid)
            ESIA_AUTH_URL_TEMPLATE.format(
                SecondaryAPI.getBaseUrl(diary), REDIRECT_URI, guid
            )
        }.toUri())
    }

    @Composable
    fun EsiaExchangeToken(code: String, hasToken: MutableState<Boolean>) {
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
            context.setUpdateFor(Date())
            hasToken.value = true
        }
    }

    fun logInWithPassword(
        context: Context,
        demo: Boolean = false
    ) {
        this@MySchoolLoginService.diary = Diary.MySchool
        if (demo) {
            return context.logInDemo()
        }
        val guid = UUID.randomUUID().toString()
        context.authPrefs.save("state" to guid)
        val tabIntent = CustomTabsIntent.Builder().build()
        tabIntent.launchUrl(context, LOGIN_PASSWORD_TEMPLATE.format(guid).toUri())
    }

    private fun Context.logInDemo() {
        authPrefs.save(
            "auth" to true, "subsystem" to Diary.MES.ordinal, "access_token" to "gitgud"
        )
        mainPrefs.save(
            "first_launch" to true,
            "demo" to true
        )
        screenLive.postValue(Screen.MainNav)
    }
}