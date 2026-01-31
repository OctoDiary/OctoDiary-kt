package org.bxkr.octodiary.components


import androidx.compose.material.icons.Icons
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.bxkr.octodiary.MainActivity
import android.util.Log
import org.bxkr.octodiary.authPrefs
import org.bxkr.octodiary.components.debugutils.PrefEditor
import org.bxkr.octodiary.components.debugutils.RemoteEditor
import org.bxkr.octodiary.components.debugutils.WidgetConfig
import org.bxkr.octodiary.components.debugutils.ExtendedDebugMenu
import org.bxkr.octodiary.get

@Composable
fun DebugMenu(
    context: MainActivity,
) {
    var isHide by remember { mutableStateOf(false) }
    var isShownMenu by remember { mutableStateOf(false) }
    var currentFunction: @Composable () -> Unit by remember { mutableStateOf({}) }
    val interactionSource = remember { MutableInteractionSource() }
    val viewConfiguration = LocalViewConfiguration.current
    LaunchedEffect(interactionSource) {
        var isLongClick = false

        interactionSource.interactions.collectLatest { interaction ->
            Log.d("DebugMenu", "Interaction received: $interaction")
            when (interaction) {
                is PressInteraction.Press -> {
                    Log.d("DebugMenu", "PressInteraction.Press detected")
                    isLongClick = false
                    delay(viewConfiguration.longPressTimeoutMillis)
                    Log.d("DebugMenu", "Long press timeout reached: ${viewConfiguration.longPressTimeoutMillis}ms")
                    isLongClick = true
                    isHide = true
                    Log.d("DebugMenu", "Long click triggered, hiding debug menu")
                }

                is PressInteraction.Release -> {
                    Log.d("DebugMenu", "PressInteraction.Release detected, isLongClick=$isLongClick")
                    if (isLongClick.not()) {
                        Log.d("DebugMenu", "Short click detected, toggling menu")
                        isShownMenu = !isShownMenu
                    } else {
                        Log.d("DebugMenu", "Long click completed")
                    }
                }
            }
        }
    }
    LaunchedEffect(interactionSource) {
        var isLongClick = false

        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isLongClick = false
                    delay(viewConfiguration.longPressTimeoutMillis)
                    isLongClick = true
                    isHide = true
                }

                is PressInteraction.Release -> {
                    if (isLongClick.not()) {
                        isShownMenu = !isShownMenu
                    }
                }
            }
        }
    }
    AnimatedVisibility(visible = !isHide, exit = slideOut { IntOffset(0, -it.height * 2) }) {
        OutlinedButton(
            onClick = {},
            Modifier.padding(end = 8.dp),
            interactionSource = interactionSource
        ) {
            Text("Debug")
        }
    }
    DropdownMenu(isShownMenu, onDismissRequest = { isShownMenu = false }) {
        DebugMenuItems.values().forEach {
            if (it.showCondition()) {
                DropdownMenuItem(
                    { Text(it.title) },
                    onClick = {
                        currentFunction = { it.function(context) { currentFunction = {} } }
                    })
            }
        }
    }
    currentFunction()
}

enum class DebugMenuItems(
    val title: String,
    val function: @Composable MainActivity.(clearFn: () -> Unit) -> Unit,
    val showCondition: @Composable () -> Boolean = { true },
) {
    PreferenceEditor("Preference editor", { clearFn -> PrefEditor { clearFn() } }),
    RemoteSettingsEditor(
        "Remote settings editor",
        { clearFn -> RemoteEditor { clearFn() } },
        { LocalContext.current.authPrefs.get<String>("access_token") != null }),
    ConfigureWidget(
        "Widget config",
        { clearFn -> WidgetConfig { clearFn() } }
    ),
    ExtendedDebug("Расширенное отладочное меню", { clearFn -> ExtendedDebugMenu(this, clearFn) })
}


