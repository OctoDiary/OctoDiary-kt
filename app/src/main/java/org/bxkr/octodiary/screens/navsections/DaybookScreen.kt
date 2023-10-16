package org.bxkr.octodiary.screens.navsections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.screens.EventCalendar

@Composable
fun DaybookScreen() {
    val loadingFinished = remember { mutableStateOf(false) }
    dataLoading(loadingFinished)

    if (loadingFinished.value) {
        EventCalendar(modifier = Modifier.fillMaxSize(), DataService.eventCalendar)
    }
    AnimatedVisibility(visible = !loadingFinished.value) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            LinearProgressIndicator(
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            )
        }
    }
}

private fun dataLoading(loadingFinished: MutableState<Boolean>) {
    DataService.also {
        if (!it.hasEventCalendar) {
            if (it.hasUserId) {
                if (it.hasSessionUser) {
                    it.updateEventCalendar {
                        loadingFinished.value = true
                    }
                } else {
                    it.updateSessionUser {
                        it.updateEventCalendar {
                            loadingFinished.value = true
                        }
                    }
                }
            } else {
                it.updateUserId {
                    it.updateSessionUser {
                        it.updateEventCalendar {
                            loadingFinished.value = true
                        }
                    }
                }
            }
        } else loadingFinished.value = true
    }
}