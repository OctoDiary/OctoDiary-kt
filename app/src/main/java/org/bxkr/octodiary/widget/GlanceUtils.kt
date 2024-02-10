package org.bxkr.octodiary.widget

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDefaults
import androidx.glance.text.TextStyle

@Composable
fun ThemedText(
    text: String,
    modifier: GlanceModifier = GlanceModifier,
    style: TextStyle = TextDefaults.defaultTextStyle,
    maxLines: Int = Int.MAX_VALUE,
    fontStyle: FontStyle = FontStyle.Normal
) {
    Text(text, modifier, style.copy(GlanceTheme.colors.onSurface, fontStyle = fontStyle), maxLines)
}

fun androidx.compose.ui.text.TextStyle.toGlanceStyle(): TextStyle = TextStyle(
    fontSize = fontSize,
    fontWeight = fontWeight?.run {
        when (weight) {
            500 -> FontWeight.Medium
            700 -> FontWeight.Bold
            else -> FontWeight.Normal
        }
    },
)