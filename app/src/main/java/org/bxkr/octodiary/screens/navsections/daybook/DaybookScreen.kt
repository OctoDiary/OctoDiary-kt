package org.bxkr.octodiary.screens.navsections.daybook

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.formatToDay
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.models.events.Event
import org.bxkr.octodiary.parseLongDate
import org.bxkr.octodiary.weekOfYear
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.roundToInt


@Composable
@OptIn(ExperimentalFoundationApi::class)
fun DaybookScreen() {
    val eventCalendar = DataService.eventCalendar.let {
        if (LocalContext.current.mainPrefs.get("show_only_plan") ?: false) {
            it.filter { it.source == "PLAN" }
        } else it
    }
    val recompositionTrigger = remember { mutableStateOf(false) }
    key(recompositionTrigger.value) {
        val showNumbers = LocalContext.current.mainPrefs.get("show_lesson_numbers") ?: true
        val weekSplitCalendar = key(eventCalendar) {
            eventCalendar.fold(mutableListOf<MutableList<Event>>()) { sum, it ->
                if (sum.isEmpty() || sum.last().first().startAt.parseLongDate()
                        .formatToDay() != it.startAt.parseLongDate().formatToDay()
                ) {
                    sum.add(mutableListOf(it))
                } else {
                    sum.last().add(it)
                }
                sum
            }.fold(mutableListOf<MutableList<MutableList<Event>>>()) { sum, it ->
                if (sum.isEmpty() ||
                    Calendar.getInstance().run {
                        time = sum.last().first().first().startAt.parseLongDate()
                        get(Calendar.WEEK_OF_YEAR)
                    } != Calendar.getInstance().run {
                        time = it.first().startAt.parseLongDate()
                        get(Calendar.WEEK_OF_YEAR)
                    }
                ) {
                    sum.add(mutableListOf(it))
                } else {
                    sum.last().add(it)
                }
                sum
            }
        }
        val currentWeekIndex = weekSplitCalendar.indexOfFirst {
            Date().weekOfYear == it.first().first().startAt.parseLongDate().weekOfYear
        }
        val currentWeeksAfter = weekSplitCalendar.lastIndex - currentWeekIndex
        val addWeekBefore = { onFinish: () -> Unit ->
            DataService.updateEventCalendar(currentWeekIndex + 1, currentWeeksAfter) {
                recompositionTrigger.value = !recompositionTrigger.value
                onFinish()
            }
        }
        val addWeekAfter = { onFinish: () -> Unit ->
            DataService.updateEventCalendar(currentWeekIndex, currentWeeksAfter + 1) {
                recompositionTrigger.value = !recompositionTrigger.value
                onFinish()
            }
        }
        val weekPosition = remember {
            mutableFloatStateOf(currentWeekIndex.toFloat())
        }
        if (eventCalendar.isNotEmpty()) {
            val firstDayOfWeekInitial = remember {
                weekSplitCalendar[weekPosition.floatValue.roundToInt()][0][0].startAt.parseLongDate()
                    .let {
                        Calendar.getInstance().run {
                            time = it
                            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                            time
                        }
                    }
            }
            val weekDaysInitial = remember {
                (0..6).toList().map {
                    Calendar.getInstance().run {
                        time = firstDayOfWeekInitial
                        set(
                            Calendar.DAY_OF_WEEK, listOf(
                                Calendar.MONDAY,
                                Calendar.TUESDAY,
                                Calendar.WEDNESDAY,
                                Calendar.THURSDAY,
                                Calendar.FRIDAY,
                                Calendar.SATURDAY,
                                Calendar.SUNDAY
                            )[it]
                        )
                        time
                    }
                }
            }
            val todayInitial =
                remember {
                    Date().formatToDay()
                        .let { weekDaysInitial.indexOfFirst { it1 -> it1.formatToDay() == it } }
                        .takeIf {
                            it != -1
                        } ?: 0
                }
            val dayPosition = rememberPagerState(todayInitial, pageCount = { 7 })
            Column(Modifier.fillMaxSize()) {
                DateSeeker(
                    weekPosition,
                    dayPosition,
                    weekSplitCalendar.lastIndex,
                    addWeekBefore,
                    addWeekAfter
                )
                AnimatedContent(
                    targetState = weekPosition.floatValue,
                    label = "week_anim"
                ) { mWeekPosition ->
                    val daySplitCalendar = weekSplitCalendar[mWeekPosition.roundToInt()]
                    val firstDayOfWeek =
                        weekSplitCalendar[mWeekPosition.roundToInt()][0][0].startAt.parseLongDate()
                            .let {
                                Calendar.getInstance().run {
                                    time = it
                                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                                    time
                                }
                            }
                    val weekDays =
                        (0..6).toList().map {
                            Calendar.getInstance().run {
                                time = firstDayOfWeek
                                set(
                                    Calendar.DAY_OF_WEEK, listOf(
                                        Calendar.MONDAY,
                                        Calendar.TUESDAY,
                                        Calendar.WEDNESDAY,
                                        Calendar.THURSDAY,
                                        Calendar.FRIDAY,
                                        Calendar.SATURDAY,
                                        Calendar.SUNDAY
                                    )[it]
                                )
                                time
                            }
                        }
                    HorizontalPager(state = dayPosition, beyondBoundsPageCount = 6) { page ->
                        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
                            Text(
                                weekDays[page].let {
                                    SimpleDateFormat(
                                        "d MMMM, EEEE", LocalConfiguration.current.locales[0]
                                    ).format(it)
                                },
                                Modifier.padding(bottom = 8.dp),
                                style = MaterialTheme.typography.titleLarge
                            )
                            if (daySplitCalendar.size > page) {
                                LazyColumn(Modifier.fillMaxSize()) {
                                    DayItem(day = daySplitCalendar[page], showNumbers)
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
        }
    }
}