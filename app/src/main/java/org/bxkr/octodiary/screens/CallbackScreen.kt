package org.bxkr.octodiary.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.R
import org.bxkr.octodiary.Screen
import org.bxkr.octodiary.network.MESLoginService.MosExchangeToken
import org.bxkr.octodiary.network.MySchoolLoginService.EsiaExchangeToken
import org.bxkr.octodiary.screenLive

@Composable
fun CallbackScreen(code: String, type: CallbackType) {
    val token: MutableState<String?> = remember { mutableStateOf(null) }

    if (token.value == null) {
        when (type) {
            CallbackType.MosRu -> MosExchangeToken(code, token)
            CallbackType.Esia -> EsiaExchangeToken(code, token)
        }
    }

    Surface(Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (token.value == null) {
                Text(text = stringResource(R.string.exchanging_code_to_token), modifier = Modifier.padding(16.dp))
                CircularProgressIndicator()

            } else {
                screenLive.value = Screen.MainNav
            }
        }
    }
}

enum class CallbackType(val host: String) {
    MosRu("oauth2redirect"),
    Esia("authRegionRedirect")
}