package org.bxkr.octodiary.components.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.bxkr.octodiary.LocalActivity
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.SwitchPreference
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.save
import org.bxkr.octodiary.screens.SetPinDialog

@Composable
fun Security() {
    val activity = LocalActivity.current
    var setPin by remember { mutableStateOf(false) }
    val pinEnabled =
        remember { mutableStateOf(activity.mainPrefs.get<Boolean>("has_pin")!!) }
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