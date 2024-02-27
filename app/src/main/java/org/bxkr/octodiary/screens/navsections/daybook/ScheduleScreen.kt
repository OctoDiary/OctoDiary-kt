package org.bxkr.octodiary.screens.navsections.daybook

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.formatToDay
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.parseLongDate
import java.text.SimpleDateFormat
import java.util.Date


@Composable
fun ScheduleScreen() {
    val eventCalendar = DataService.eventCalendar.let {
        if (LocalContext.current.mainPrefs.get("show_only_plan") ?: false) {
            it.filter { it.source == "PLAN" }
        } else it
    }
    Column {
        CalendarBar()
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            Text(
                Date().let {
                    SimpleDateFormat(
                        "d MMMM, EEEE", LocalConfiguration.current.locales[0]
                    ).format(it)
                },
                Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.titleLarge
            )
            LazyColumn(Modifier.fillMaxSize()) {
                DayItem(eventCalendar.filter {
                    it.startAt.parseLongDate().formatToDay() == Date().formatToDay()
                })
            }
        }
    }
}