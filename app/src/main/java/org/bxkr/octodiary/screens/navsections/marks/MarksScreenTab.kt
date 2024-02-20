package org.bxkr.octodiary.screens.navsections.marks

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.ui.graphics.vector.ImageVector
import org.bxkr.octodiary.R

enum class MarksScreenTab(
    @StringRes val title: Int, val icon: ImageVector
) {
    ByDate(
        R.string.by_date, Icons.Rounded.DateRange
    ),
    BySubject(
        R.string.by_subject, Icons.Rounded.Book
    )
}