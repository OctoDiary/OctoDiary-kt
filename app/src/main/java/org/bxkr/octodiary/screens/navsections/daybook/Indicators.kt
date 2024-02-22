package org.bxkr.octodiary.screens.navsections.daybook

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Star
import androidx.compose.ui.graphics.vector.ImageVector
import org.bxkr.octodiary.R
import org.bxkr.octodiary.models.events.Event

enum class Indicators(
    val icon: ImageVector,
    @StringRes val descriptionRes: Int,
    val condition: (Event) -> Boolean,
) {
    Replacement(
        Icons.Rounded.Repeat,
        R.string.replacement,
        { it.replaced == true }
    ),
    HasHomework(
        Icons.Rounded.Home,
        R.string.homeworks,
        { it.homework?.descriptions?.isNotEmpty() == true }
    ),
    HasMarks(
        Icons.Rounded.Star,
        R.string.marks,
        { it.marks?.isNotEmpty() == true }
    )
}