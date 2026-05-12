package org.bxkr.octodiary

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.network.MESLoginService
import org.bxkr.octodiary.network.MySchoolLoginService
import org.bxkr.octodiary.network.MySchoolLoginService.logInWithPassword

enum class Diary(
    @StringRes val title: Int,
    val icon: ImageVector,
    @ColorRes val primaryLogGradientColors: List<Int>,
    @StringRes val primaryLogInLabel: Int,
    val primaryLogInFunction: (Context) -> Unit,
    val alternativeLogIn: @Composable (Modifier, (() -> Unit) -> Unit) -> Unit,
) {
    MES(
        R.string.mes,
        Icons.Rounded.LocationCity,
        listOf(R.color.mosru_primary, R.color.mosru_primary),
        R.string.log_in_on_mosru,
        { MESLoginService.logInWithMosRu(it) },
        @Composable { modifier, onClick ->
            Context.MODE_PRIVATE
            val context = androidx.compose.ui.platform.LocalContext.current
            Row(
                modifier = modifier
                    .width(TextFieldDefaults.MinWidth)
                    .height(TextFieldDefaults.MinHeight)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                colorResource(R.color.blue),
                                colorResource(R.color.red)
                            )
                        ), MaterialTheme.shapes.medium
                    )
                    .clip(MaterialTheme.shapes.medium)
                    .clickable {
                        onClick {
                            MySchoolLoginService.logInWithEsia(
                                context,
                                MES
                            )
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = stringResource(id = R.string.log_in),
                    modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                    tint = Color.White
                )
                Text(
                    stringResource(id = R.string.log_in_on_gosuslugi),
                    color = Color.White
                )
            }
        }
    ),
    MySchool(
        R.string.myschool,
        Icons.Rounded.Landscape,
        listOf(R.color.blue, R.color.red),
        R.string.log_in_on_gosuslugi,
        { MySchoolLoginService.logInWithEsia(it, MySchool) },
        @Composable { modifier, _ ->
            val context = androidx.compose.ui.platform.LocalContext.current

            Row(
                modifier = modifier
                    .width(TextFieldDefaults.MinWidth)
                    .height(TextFieldDefaults.MinHeight)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.tertiaryContainer,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        ), MaterialTheme.shapes.medium
                    )
                    .clip(MaterialTheme.shapes.medium)
                    .clickable {
                        logInWithPassword(context)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = stringResource(id = R.string.log_in),
                    modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                )
                Text(
                    stringResource(id = R.string.log_in_by_password)
                )
            }

            androidx.compose.material3.OutlinedButton(
                onClick = {
                    logInWithPassword(
                        context,
                        demo = true
                    )
                },
                modifier = modifier
                    .padding(top = 8.dp)
            ) {
                Text(stringResource(id = R.string.demo_mode))
            }
        }
    )
}