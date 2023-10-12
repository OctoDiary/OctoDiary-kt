package org.bxkr.octodiary.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.models.events.Event

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
            LazyColumn(Modifier.fillMaxSize()) {
                items(DataService.eventCalendar) {
                    EventItem(event = it)
                }
            }
        }
    }
}

@Composable
fun EventItem(event: Event) {
    Text(
        "${event.roomNumber}::${event.subjectName}"
    )
}