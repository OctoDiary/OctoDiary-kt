package org.bxkr.octodiary.components.settings

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.LocalActivity
import org.bxkr.octodiary.R
import org.bxkr.octodiary.colorSchemeLive
import org.bxkr.octodiary.components.SwitchPreference
import org.bxkr.octodiary.components.ThemeCard
import org.bxkr.octodiary.darkThemeLive
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.save
import org.bxkr.octodiary.ui.theme.CustomColorScheme

@Composable
fun Appearance() {
    val activity = LocalActivity.current
    val darkTheme = darkThemeLive.observeAsState(isSystemInDarkTheme())
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
}