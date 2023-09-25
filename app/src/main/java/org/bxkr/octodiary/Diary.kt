package org.bxkr.octodiary

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.ui.graphics.vector.ImageVector
import org.bxkr.octodiary.screens.MESLoginService
import org.bxkr.octodiary.screens.MySchoolLoginService

enum class Diary(
    @StringRes val title: Int,
    val icon: ImageVector,
    @ColorRes val gradientColors: List<Int>,
    @StringRes val logInLabel: Int,
    val logInFunction: (Context) -> Unit
) {
    MES(
        R.string.mes,
        Icons.Rounded.LocationCity,
        listOf(R.color.mosru_primary, R.color.mosru_primary),
        R.string.log_in_on_mosru,
        { MESLoginService.logInWithMosRu(it) }
    ),
    MySchool(
        R.string.myschool,
        Icons.Rounded.Landscape,
        listOf(R.color.blue, R.color.red),
        R.string.log_in_on_gosuslugi,
        { MySchoolLoginService.logInWithEsia(it) }
    )
}