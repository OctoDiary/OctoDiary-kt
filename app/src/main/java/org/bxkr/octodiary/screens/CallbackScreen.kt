package org.bxkr.octodiary.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.R
import org.bxkr.octodiary.screens.LoginService.ExchangeToken

@Composable
fun CallbackScreen(modifier: Modifier, code: String) {
    val token: MutableState<String?> = remember { mutableStateOf(null) }

    ExchangeToken(LocalContext.current, code, token)

    Surface(modifier) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (token.value == null) {
                Text(text = stringResource(R.string.exchanging_code_to_token), modifier = Modifier.padding(16.dp))
                CircularProgressIndicator()

            } else {
                Text(text = token.value!!, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}