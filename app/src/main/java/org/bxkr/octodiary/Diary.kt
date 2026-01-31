package org.bxkr.octodiary


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
        Icons.Default.LocationCity,
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
                    Icons.AutoMirrored.Default.OpenInNew,
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
        Icons.Default.Landscape,
        listOf(R.color.blue, R.color.red),
        R.string.log_in_on_gosuslugi,
        { MySchoolLoginService.logInWithEsia(it, MySchool) },
        @Composable { modifier, _ ->

            var loginText by rememberSaveable { mutableStateOf("") }
            var passwordText by rememberSaveable { mutableStateOf("") }
            val context = androidx.compose.ui.platform.LocalContext.current
            val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
            var isButtonEnabled by rememberSaveable { mutableStateOf(true) }


            TextField(
                value = loginText,
                onValueChange = { loginText = it },
                modifier = modifier
                    .padding(vertical = 1.dp),
                label = { Text(stringResource(id = R.string.username)) },
                shape = RoundedCornerShape(
                    topStart = MaterialTheme.shapes.medium.topStart,
                    topEnd = MaterialTheme.shapes.medium.topEnd,
                    bottomStart = MaterialTheme.shapes.extraSmall.bottomStart,
                    bottomEnd = MaterialTheme.shapes.extraSmall.bottomEnd
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true
            )
            TextField(
                value = passwordText,
                onValueChange = { passwordText = it },
                modifier = modifier
                    .padding(vertical = 1.dp),
                label = { Text(stringResource(id = R.string.password)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                shape = MaterialTheme.shapes.medium.copy(
                    topStart = MaterialTheme.shapes.extraSmall.topStart,
                    topEnd = MaterialTheme.shapes.extraSmall.topEnd
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                keyboardActions = KeyboardActions(onDone = {
                    this.defaultKeyboardAction(ImeAction.Done)
                    context.logInWithPassword(loginText, passwordText, coroutineScope) {
                        isButtonEnabled = true
                    }
                    isButtonEnabled = false
                })
            )
            Button(
                onClick = {
                    context.logInWithPassword(loginText, passwordText, coroutineScope) {
                        isButtonEnabled = true
                    }
                    isButtonEnabled = false
                },
                modifier = modifier
                    .padding(top = 32.dp),
                enabled = isButtonEnabled
            ) {
                Text(stringResource(id = R.string.log_in))
            }
            androidx.compose.material3.OutlinedButton(
                onClick = {
                    context.logInWithPassword(
                        "demousername",
                        "demopassword",
                        coroutineScope,
                    ) {}
                },
                modifier = modifier
                    .padding(top = 8.dp)
            ) {
                Text(stringResource(id = R.string.demo_mode))
            }
        }
    )
}


