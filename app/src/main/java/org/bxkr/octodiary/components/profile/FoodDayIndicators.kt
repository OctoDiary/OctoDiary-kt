package org.bxkr.octodiary.components.profile

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.ui.graphics.vector.ImageVector
import org.bxkr.octodiary.models.daysbalanceinfo.Day

enum class FoodDayIndicators(
    val icon: ImageVector,
    @StringRes val descriptionRes: Int,
    val condition: (Day) -> Boolean,
) {
    DINING(
        icon = Icons.Outlined.LocalDining,
        descriptionRes = org.bxkr.octodiary.R.string.dining,
        condition = { it.transactions.any { it.type == "DINING"} }
    ),
    BUFFET(
        icon = Icons.Outlined.BakeryDining,
        descriptionRes = org.bxkr.octodiary.R.string.buffet,
        condition = { it.transactions.any { it.type == "BUFFET"} }
    )
}