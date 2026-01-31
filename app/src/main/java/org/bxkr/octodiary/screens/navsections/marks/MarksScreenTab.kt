package org.bxkr.octodiary.screens.navsections.marks


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DateRange
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import org.bxkr.octodiary.R

enum class MarksScreenTab(
    @StringRes val title: Int, val icon: ImageVector
) {
    ByDate(
        R.string.by_date, Icons.Default.DateRange
    ),
    BySubject(
        R.string.by_subject, Icons.Default.Book
    )
}


