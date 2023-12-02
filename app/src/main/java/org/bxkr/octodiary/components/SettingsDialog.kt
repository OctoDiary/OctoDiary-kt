package org.bxkr.octodiary.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.bxkr.octodiary.LocalActivity
import org.bxkr.octodiary.R
import org.bxkr.octodiary.darkThemeLive
import org.bxkr.octodiary.get
import org.bxkr.octodiary.logOut
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.save
import org.bxkr.octodiary.screens.SetPinDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(onDismissRequest: () -> Unit) {
    Dialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        onDismissRequest = { onDismissRequest() }
    ) {
        Scaffold(
            topBar = {
                MediumTopAppBar(
                    title = { Text(stringResource(R.string.settings)) },
                    navigationIcon = {
                        IconButton(onClick = { onDismissRequest() }) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                stringResource(R.string.back)
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Surface(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    val activity = LocalActivity.current
                    var setPin by remember { mutableStateOf(false) }
                    val darkTheme = darkThemeLive.observeAsState(isSystemInDarkTheme())
                    val pinEnabled =
                        remember { mutableStateOf(activity.mainPrefs.get<Boolean>("has_pin")!!) }

                    SwitchPreference(
                        title = stringResource(R.string.dark_theme),
                        listenState = darkTheme
                    ) {
                        darkThemeLive.value = it
                        activity.mainPrefs.save("is_dark_theme" to it)
                    }

                    SwitchPreference(
                        title = stringResource(id = R.string.use_pin),
                        listenState = pinEnabled
                    ) {
                        if (it) {
                            setPin = true
                        } else {
                            activity.mainPrefs.save(
                                "has_pin" to false,
                                "pin" to null
                            )
                            pinEnabled.value = false
                        }
                    }
                    OutlinedButton(onClick = {
                        onDismissRequest()
                        activity.logOut()
                    }, Modifier.padding(32.dp)) {
                        Text(stringResource(R.string.log_out))
                    }

                    AnimatedVisibility(setPin) {
                        val pinFinished = remember { mutableStateOf(false) }
                        val initialPin = remember { mutableStateOf(emptyList<Int>()) }
                        val secondPin = remember { mutableStateOf(emptyList<Int>()) }

                        SetPinDialog(
                            pinFinished = pinFinished,
                            initialPin = initialPin,
                            secondPin = secondPin,
                            closeButtonTitle = stringResource(id = R.string.cancel)
                        )

                        if (pinFinished.value) {
                            setPin = false
                        }

                        if (
                            initialPin.value.size == 4 &&
                            secondPin.value.size == 4
                        ) {
                            if (initialPin.value == secondPin.value) {
                                LocalContext.current.mainPrefs.save(
                                    "has_pin" to true,
                                    "pin" to secondPin.value.joinToString("")
                                )
                                pinFinished.value = true
                                setPin = false
                                pinEnabled.value = true
                            } else {
                                initialPin.value = emptyList()
                                secondPin.value = emptyList()
                                pinFinished.value = false
                            }
                        }
                    }
                }
            }
        }
    }
}