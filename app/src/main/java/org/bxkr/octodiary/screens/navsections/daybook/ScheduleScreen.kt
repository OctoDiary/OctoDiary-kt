package org.bxkr.octodiary.screens.navsections.daybook

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.bxkr.octodiary.areBreaksShown
import org.bxkr.octodiary.components.settings.CommonPrefs
import org.bxkr.octodiary.demoScheduleDate
import org.bxkr.octodiary.formatToDay
import org.bxkr.octodiary.get
import org.bxkr.octodiary.getWeekday
import org.bxkr.octodiary.isDateBetween
import org.bxkr.octodiary.isDemo
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.models.events.Event
import org.bxkr.octodiary.parseLongDate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

val customScheduleRefreshListenerLive = MutableLiveData<() -> Unit>(null)
val updatedScheduleLive = MutableLiveData(true)
val daySelectedLive = MutableLiveData<Date>()

@Composable
fun ScheduleScreen() {
    key(updatedScheduleLive.observeAsState().value) {
        val eventCalendar = DataService.eventCalendar.let { calendar ->
            if (LocalContext.current.mainPrefs.get(CommonPrefs.showOnlyPlan.prefKey) ?: false) {
                calendar.filter { it.source == "PLAN" }
            } else calendar
        }
        Column {
            CalendarBar()
            WeekPager(eventCalendar)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeekPager(eventsLoaded: List<Event>) {
    val isDemo = LocalContext.current.isDemo
    var events by remember { mutableStateOf(eventsLoaded) }
    var isLoadingNewEvents by remember { mutableStateOf(false) }
    var currentDateRange by remember { mutableStateOf(DataService.eventsRange) }
    val showNumbers =
        LocalContext.current.mainPrefs.get(CommonPrefs.showLessonNumbers.prefKey) ?: true
    val showBreaks = areBreaksShown()
    val currentDay = daySelectedLive.observeAsState(if (!isDemo) Date() else demoScheduleDate)
    val weekdays = remember(currentDay.value) {
        val cal = Calendar.getInstance().apply { time = currentDay.value }
        val first = cal.firstDayOfWeek
        (0..6).map { offset -> ((first + offset - 1) % 7) + 1 }
    }

    val dayPosition = rememberPagerState(
        initialPage = weekdays.indexOf(getWeekday(currentDay.value)),
        pageCount = { 7 }
    )

    LaunchedEffect(dayPosition) {
        snapshotFlow { dayPosition.currentPage }.collect { page ->
            if (weekdays.indexOf(getWeekday(currentDay.value)) != page) {
                daySelectedLive.postValue(currentDay.value.let {
                    Calendar.getInstance().apply {
                        time = it
                        set(Calendar.DAY_OF_WEEK, weekdays[page])
                    }.time
                })
            }
        }
    }
    LaunchedEffect(currentDay) {
        snapshotFlow { currentDay.value }.collect { date ->
            if (weekdays.indexOf(getWeekday(date)) != dayPosition.currentPage) {
                dayPosition.animateScrollToPage(weekdays.indexOf(getWeekday(currentDay.value)))
            }
            if (date.isDateBetween(DataService.eventsRange) && currentDateRange != DataService.eventsRange) {
                events = eventsLoaded
                currentDateRange = DataService.eventsRange
            }
            if (!date.isDateBetween(currentDateRange) && !isDemo) {
                isLoadingNewEvents = true
                DataService.getEventWeek(date) { eventsResponse, range ->
                    currentDateRange = range
                    events = eventsResponse
                    isLoadingNewEvents = false
                }
            }
        }
    }
    HorizontalPager(state = dayPosition, beyondBoundsPageCount = 8) { weekdayIndex ->
        val date = currentDay.value.let {
            Calendar.getInstance().apply {
                time = it
                set(Calendar.DAY_OF_WEEK, weekdays[weekdayIndex])
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
            AnimatedVisibility(visible = isLoadingNewEvents) {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
            AnimatedVisibility(visible = !isLoadingNewEvents) {
                if (dayEvents.isNotEmpty()) {
                    LazyColumn(Modifier.fillMaxSize()) {
                        DayItem(dayEvents, showNumbers, showBreaks)
                    }
                } else {
                    Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
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