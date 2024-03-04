package org.bxkr.octodiary.screens.navsections.daybook

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.formatToDay
import org.bxkr.octodiary.get
import org.bxkr.octodiary.getWeekday
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.models.events.Event
import org.bxkr.octodiary.parseLongDate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Collections
import java.util.Date

val customScheduleRefreshListenerLive = MutableLiveData<() -> Unit>(null)
val updatedScheduleLive = MutableLiveData(true)
val daySelectedLive = MutableLiveData<Date>(Date())

@Composable
fun ScheduleScreen() {
    key(updatedScheduleLive.observeAsState().value) {
        val eventCalendar = DataService.eventCalendar.let {
            if (LocalContext.current.mainPrefs.get("show_only_plan") ?: false) {
                it.filter { it.source == "PLAN" }
            } else it
        }
        Column {
            CalendarBar()
            WeekPager(eventCalendar)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeekPager(events: List<Event>) {
    val showNumbers = LocalContext.current.mainPrefs.get("show_lesson_numbers") ?: true
    val weekdays = remember { (1..7).toList().also { Collections.rotate(it, -1) } }
    val dayPosition =
        rememberPagerState(initialPage = weekdays.indexOf(getWeekday(Date())) + 1,
            pageCount = { 9 })
    val currentDay = daySelectedLive.observeAsState(Date())
    LaunchedEffect(dayPosition) {
        snapshotFlow { dayPosition.currentPage }.collect { page ->
            if (page in 1..7) {
                if (weekdays.indexOf(getWeekday(currentDay.value)) + 1 != page) {
                    daySelectedLive.postValue(currentDay.value.let {
                        Calendar.getInstance().apply {
                            time = it
                            set(Calendar.DAY_OF_WEEK, weekdays[page - 1])
                        }.time
                    })
                }
            } else {
                // TODO: Load a new week
            }
        }
    }
    LaunchedEffect(currentDay) {
        snapshotFlow { currentDay.value }.collect { date ->
            if (weekdays.indexOf(getWeekday(date)) + 1 != dayPosition.currentPage) {
                dayPosition.animateScrollToPage(weekdays.indexOf(getWeekday(currentDay.value)) + 1)
            }
        }
    }
    HorizontalPager(state = dayPosition, beyondBoundsPageCount = 8) { weekdayIndex ->
        if (weekdayIndex in 1..7) {
            val date = currentDay.value.let {
                Calendar.getInstance().apply {
                    time = it
                    set(Calendar.DAY_OF_WEEK, weekdays[weekdayIndex - 1])
                }.time
            }
            val dayEvents = events.filter {
                it.startAt.parseLongDate().formatToDay() == date.formatToDay()
            }
            Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
                Text(
                    date.let {
                        SimpleDateFormat(
                            "d MMMM, EEEE", LocalConfiguration.current.locales[0]
                        ).format(it)
                    },
                    Modifier.padding(bottom = 8.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                if (dayEvents.isNotEmpty()) {
                    LazyColumn(Modifier.fillMaxSize()) {
                        DayItem(dayEvents, showNumbers)
                    }
                } else {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = stringResource(id = R.string.free_day))
                    }
                }
            }
        }
    }
}