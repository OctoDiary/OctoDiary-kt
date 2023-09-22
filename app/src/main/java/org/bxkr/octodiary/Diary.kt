package org.bxkr.octodiary

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.ui.graphics.vector.ImageVector

enum class Diary(
    @StringRes val title: Int,
    val icon: ImageVector,
    @ColorRes val gradientColors: List<Int>,
    @StringRes val logInLabel: Int
) {
    MES(
        R.string.mes,
        Icons.Rounded.LocationCity,
        listOf(R.color.orange, R.color.purple),
        R.string.log_in_on_mosru
    ),
    MySchool(
        R.string.myschool,
        Icons.Rounded.Landscape,
        listOf(R.color.light_purple, R.color.purple),
        R.string.log_in_on_gosuslugi
    )
}