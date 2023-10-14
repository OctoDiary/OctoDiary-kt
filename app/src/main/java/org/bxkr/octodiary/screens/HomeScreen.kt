package org.bxkr.octodiary.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.bxkr.octodiary.DataService

@Composable
fun HomeScreen() {
    with(LocalContext.current) {
        val loadingFinished = remember { mutableStateOf(false) }

        DataService.updateUserId {
            DataService.updateSessionUser {
                DataService.updateEventCalendar {
                    loadingFinished.value = true
                }
            }
        }


        if (loadingFinished.value) {
            EventCalendar(modifier = Modifier.fillMaxSize(), DataService.eventCalendar)
        }
    }
}