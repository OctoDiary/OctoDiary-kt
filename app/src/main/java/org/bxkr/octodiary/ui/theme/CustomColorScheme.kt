package org.bxkr.octodiary.ui.theme

import androidx.compose.material3.ColorScheme

enum class CustomColorScheme(
    val lightColorScheme: ColorScheme,
    val darkColorScheme: ColorScheme
) {
    Blue(
        org.bxkr.octodiary.ui.theme.blue.LightColorScheme,
        org.bxkr.octodiary.ui.theme.blue.DarkColorScheme
    ),
    Brown(
        org.bxkr.octodiary.ui.theme.brown.LightColorScheme,
        org.bxkr.octodiary.ui.theme.brown.DarkColorScheme
    ),
    Green(
        org.bxkr.octodiary.ui.theme.green.LightColorScheme,
        org.bxkr.octodiary.ui.theme.green.DarkColorScheme
    ),
    Pink(
        org.bxkr.octodiary.ui.theme.pink.LightColorScheme,
        org.bxkr.octodiary.ui.theme.pink.DarkColorScheme
    ),
    Purple(
        org.bxkr.octodiary.ui.theme.purple.LightColorScheme,
        org.bxkr.octodiary.ui.theme.purple.DarkColorScheme
    ),
    Teal(
        org.bxkr.octodiary.ui.theme.teal.LightColorScheme,
        org.bxkr.octodiary.ui.theme.teal.DarkColorScheme
    ),
    Yellow(
        org.bxkr.octodiary.ui.theme.yellow.LightColorScheme,
        org.bxkr.octodiary.ui.theme.yellow.DarkColorScheme
    )
}