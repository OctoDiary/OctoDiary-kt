package org.bxkr.octodiary.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.Diary
import org.bxkr.octodiary.R
import org.bxkr.octodiary.Screen
import org.bxkr.octodiary.authPrefs
import org.bxkr.octodiary.formatToLongHumanDate
import org.bxkr.octodiary.jwtPayload
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.modalDialogStateLive
import org.bxkr.octodiary.save
import org.bxkr.octodiary.screenLive
import org.bxkr.octodiary.widget.StatusWidget.Companion.setUpdateFor
import java.util.Date

@Composable
fun TokenLogin() {
    Column(Modifier.padding(16.dp)) {
        Text(
            stringResource(id = R.string.log_in_by_token),
            Modifier.padding(bottom = 4.dp),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            stringResource(R.string.token_auth_warning), Modifier.alpha(.8f),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(16.dp))
        var tokenValue by remember { mutableStateOf("") }
        val clipboardManager = LocalClipboardManager.current
        OutlinedTextField(
            tokenValue,
            { tokenValue = it },
            Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.token)) },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = {
                    tokenValue = clipboardManager.getText()?.text ?: tokenValue
                }) {
                    Icon(
                        Icons.Default.ContentPaste,
                        stringResource(R.string.paste)
                    )
                }
            }
        )
        if (tokenValue.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            val payload = tokenValue.jwtPayload
            if (payload != null && payload.containsKey("iss") && payload.containsKey("exp")) {
                val issuer = when (payload.get("iss")!!) {
                    "https://school.mos.ru" -> Diary.MES
                    "https://authedu.mosreg.ru" -> Diary.MySchool
                    else -> null
                }
                val expirationDate =
                    payload.get("exp")!!.toLong().let { Date(it * 1000) }.formatToLongHumanDate()
                Column {
                    Text(
                        stringResource(
                            R.string.token_description,
                            issuer?.let { stringResource(it.title) } ?: payload.get("iss")!!,
                            expirationDate
                        )
                    )
                    if (issuer != null) {
                        val context = LocalContext.current
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(onClick = {
                                context.logInByToken(tokenValue.trim(), issuer)
                                modalDialogStateLive.postValue(false)
                            }) {
                                Text(stringResource(R.string.log_in))
                            }
                        }
                    }
                }
            } else {
                Text(stringResource(R.string.invalid_token))
            }
        }
    }
}

fun Context.logInByToken(token: String, subsystem: Diary) {
    authPrefs.save(
        "auth" to true, "subsystem" to subsystem.ordinal, "access_token" to token
    )
    mainPrefs.save(
        "first_launch" to true
    )
    screenLive.postValue(Screen.MainNav)
    setUpdateFor(Date())
}



