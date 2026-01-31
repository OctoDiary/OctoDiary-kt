package org.bxkr.octodiary.screens.navsections.daybook


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Star
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import org.bxkr.octodiary.R
import org.bxkr.octodiary.models.events.Event

enum class Indicators(
    val icon: ImageVector,
    @StringRes val descriptionRes: Int,
    val condition: (Event) -> Boolean,
) {
    Replacement(
        Icons.Default.Repeat,
        R.string.replacement,
        { it.replaced == true }
    ),
    HasHomework(
        Icons.Default.Home,
        R.string.homeworks,
        { it.homework?.descriptions?.isNotEmpty() == true }
    ),
    HasMarks(
        Icons.Default.Star,
        R.string.marks,
        { it.marks?.isNotEmpty() == true }
    )
}


