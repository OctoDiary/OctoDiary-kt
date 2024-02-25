package org.bxkr.octodiary.components.settings

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.bxkr.octodiary.components.SwitchPreference
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.save

@Composable
fun BasicSwitchPreference(
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int? = null,
    prefKey: String,
    defaultValue: Boolean,
) {
    val context = LocalContext.current
    val state = remember { mutableStateOf(context.mainPrefs.get(prefKey) ?: defaultValue) }

    SwitchPreference(stringResource(titleRes), descriptionRes?.let { stringResource(it) }, state) {
        state.value = it
        context.mainPrefs.save(prefKey to it)
    }
}