package org.bxkr.octodiary.screens

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import org.bxkr.octodiary.NetworkService
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
}