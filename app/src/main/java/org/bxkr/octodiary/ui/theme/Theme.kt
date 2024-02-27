package org.bxkr.octodiary.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import org.bxkr.octodiary.ui.theme.yellow.DarkColorScheme
import org.bxkr.octodiary.ui.theme.yellow.LightColorScheme

val enterTransition = expandVertically(
    expandFrom = Alignment.Top, animationSpec = tween(200)
)

val exitTransition = shrinkVertically(
    shrinkTowards = Alignment.Top, animationSpec = tween(200)
)

val enterTransition1 = slideInHorizontally(
    tween(200)
) { it }

val exitTransition1 = slideOutHorizontally(
    tween(200)
) { it }

val enterTransition2 = slideInHorizontally(
    tween(200)
) { -it }

val exitTransition2 = slideOutHorizontally(
    tween(200)
) { -it }

@Composable
fun OctoDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    lightScheme: ColorScheme = LightColorScheme,
    darkScheme: ColorScheme = DarkColorScheme,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkScheme
        else -> lightScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.outlineVariant.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}