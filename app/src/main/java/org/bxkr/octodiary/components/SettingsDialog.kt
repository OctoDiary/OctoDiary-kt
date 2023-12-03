package org.bxkr.octodiary.components

import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.LocalActivity
import org.bxkr.octodiary.R
import org.bxkr.octodiary.colorSchemeLive
import org.bxkr.octodiary.darkThemeLive
import org.bxkr.octodiary.get
import org.bxkr.octodiary.launchUrlLive
import org.bxkr.octodiary.logOut
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.save
import org.bxkr.octodiary.screens.SetPinDialog
import org.bxkr.octodiary.ui.theme.CustomColorScheme

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
                    var selectedTheme by remember { mutableStateOf(colorSchemeLive.value) }

                    LazyRow {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            item {
                                MaterialTheme.colorScheme.run {
                                    val isDynamic = selectedTheme == -1
                                    ThemeCard(
                                        isDynamic,
                                        if (isDynamic) primary else MaterialTheme.colorScheme.surface,
                                        if (isDynamic) secondary else MaterialTheme.colorScheme.surface,
                                        if (isDynamic) surfaceVariant else MaterialTheme.colorScheme.surface,
                                        Modifier.padding(start = 8.dp),
                                        true
                                    ) {
                                        colorSchemeLive.postValue(-1)
                                        selectedTheme = -2
                                        activity.mainPrefs.save("theme" to -1)
                                    }
                                }
                            }
                        }
                        items(CustomColorScheme.values()) {
                            val scheme = remember {
                                when (darkTheme.value) {
                                    true -> it.darkColorScheme
                                    false -> it.lightColorScheme
                                }
                            }
                            scheme.run {
                                ThemeCard(
                                    selectedTheme == it.ordinal,
                                    primary,
                                    secondary,
                                    surfaceVariant
                                ) {
                                    colorSchemeLive.postValue(it.ordinal)
                                    selectedTheme = -2
                                    activity.mainPrefs.save("theme" to it.ordinal)
                                }
                            }
                        }
                    }

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
                    Button(
                        onClick = {
                            val link = Uri.parse(
                                NetworkService.ExternalIntegrationConfig.BOT_AUTH_URL.format(
                                    DataService.token,
                                    DataService.subsystem.ordinal
                                )
                            )
                            launchUrlLive.postValue(link)
                        },
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.OpenInNew,
                            stringResource(R.string.image),
                            Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.log_into_bot))
                    }
                    OutlinedButton(onClick = {
                        onDismissRequest()
                        activity.logOut()
                    }, Modifier.padding(bottom = 32.dp)) {
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

@Composable
fun ThemeCard(
    selected: Boolean,
    top: Color,
    middle: Color,
    bottom: Color,
    modifier: Modifier = Modifier,
    showIcon: Boolean = false,
    onClick: () -> Unit
) {
    Box(Modifier.clip(MaterialTheme.shapes.large)) {
        OutlinedCard(modifier.padding(8.dp), shape = MaterialTheme.shapes.extraLarge) {
            Box(Modifier.clickable(onClick = onClick)) {
                Column(
                    Modifier.border(
                        4.dp,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.shapes.extraLarge
                    )
                ) {
                    listOf(top, middle, bottom).forEach {
                        AnimatedContent(targetState = it, label = "color_anim") { color ->
                            Box(
                                Modifier
                                    .size(width = 72.dp, height = 36.dp)
                                    .background(color)
                            )
                        }
                    }
                }
                if (showIcon) {
                    Icon(
                        Icons.Rounded.Image,
                        stringResource(R.string.wallpaper_dynamic_color),
                        Modifier
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(8.dp),
                        MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
        AnimatedVisibility(selected) {
            Icon(
                Icons.Rounded.Done,
                stringResource(R.string.wallpaper_dynamic_color),
                Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
                    .size(18.dp)
                    .padding(2.dp),
                MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}