package org.bxkr.octodiary.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.R
import org.bxkr.octodiary.ui.theme.OctoDiaryTheme

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier
) {
    var loginText by rememberSaveable { mutableStateOf("") }
    var passwordText by rememberSaveable { mutableStateOf("") }
    Column(
        modifier.fillMaxSize(),
        Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .width(TextFieldDefaults.MinWidth)
                .height(TextFieldDefaults.MinHeight)
                .align(Alignment.CenterHorizontally)
                .border(
                    TextFieldDefaults.FocusedIndicatorThickness,
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.shapes.medium
                )
                .clip(MaterialTheme.shapes.medium)
                .clickable { LoginService.logInWithMosRu() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = stringResource(id = R.string.log_in_on_mosru),
                modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                stringResource(id = R.string.log_in_on_mosru),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        ) {
            Divider(
                modifier = Modifier
                    .width(96.dp)
                    .padding(horizontal = 16.dp)
                    .align(Alignment.CenterVertically),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                stringResource(id = R.string.or),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            Divider(
                modifier = Modifier
                    .width(96.dp)
                    .padding(horizontal = 16.dp)
                    .align(Alignment.CenterVertically),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        }
        TextField(
            value = loginText,
            onValueChange = { loginText = it },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 1.dp),
            label = { Text(stringResource(id = R.string.username)) },
            shape = RoundedCornerShape(
                topStart = MaterialTheme.shapes.medium.topStart,
                topEnd = MaterialTheme.shapes.medium.topEnd,
                bottomStart = MaterialTheme.shapes.extraSmall.bottomStart,
                bottomEnd = MaterialTheme.shapes.extraSmall.bottomEnd
            ),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Unspecified,
                unfocusedIndicatorColor = Color.Unspecified
            )
        )
        TextField(
            value = passwordText,
            onValueChange = { passwordText = it },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 1.dp),
            label = { Text(stringResource(id = R.string.password)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = MaterialTheme.shapes.medium.copy(
                topStart = MaterialTheme.shapes.extraSmall.topStart,
                topEnd = MaterialTheme.shapes.extraSmall.topEnd
            ),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Unspecified,
                unfocusedIndicatorColor = Color.Unspecified
            )
        )
        Button(
            onClick = { /*TODO*/ },
            modifier = Modifier
                .padding(top = 32.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(id = R.string.log_in))
        }
    }
}

@Preview
@Composable
fun LoginPreview() {
    OctoDiaryTheme {
        Surface {
            LoginScreen()
        }
    }
}