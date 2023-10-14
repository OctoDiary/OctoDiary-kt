package org.bxkr.octodiary.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.bxkr.octodiary.R
import org.bxkr.octodiary.formatToDay
import org.bxkr.octodiary.models.events.Event
import org.bxkr.octodiary.parseLongDate
import org.bxkr.octodiary.ui.theme.OctoDiaryTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.roundToInt


@Composable
@OptIn(ExperimentalFoundationApi::class)
fun EventCalendar(modifier: Modifier, eventCalendar: List<Event>) {
    if (eventCalendar.isNotEmpty()) {
        val firstDayOfWeek = eventCalendar[0].startAt.parseLongDate().let {
            Calendar.getInstance().run {
                time = it
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                time
            }
        }
        val weekDays = (0..6).toList().map {
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
        val today = Date().formatToDay()
            .let { weekDays.indexOfFirst { it1 -> it1.formatToDay() == it } }

        val daySplitCalendar = eventCalendar.fold(mutableListOf<MutableList<Event>>()) { sum, it ->
            if (sum.isEmpty() ||
                sum.last().first().startAt.parseLongDate().formatToDay() !=
                it.startAt.parseLongDate().formatToDay()
            ) {
                sum.add(mutableListOf(it))
            } else {
                sum.last().add(it)
            }
            sum
        }
        val dayPosition = rememberPagerState(today, pageCount = { 7 })
        val weekPosition = remember { mutableFloatStateOf(1f) }
        Column(modifier) {
            DateSeeker(weekPosition, dayPosition)
            HorizontalPager(state = dayPosition) { page ->
                Column(Modifier.padding(16.dp)) {
                    Text(
                        weekDays[page]
                            .let {
                                SimpleDateFormat(
                                    "dd MMMM, EEEE",
                                    LocalConfiguration.current.locales[0]
                                ).format(it)
                            },
                        Modifier.padding(bottom = 8.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (daySplitCalendar.size > page) {
                        DayItem(Modifier.fillMaxSize(), day = daySplitCalendar[page])
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DateSeeker(weekPosition: MutableState<Float>, dayPosition: PagerState) {
    val coroutineScope = rememberCoroutineScope()
    ElevatedCard(
        shape = MaterialTheme.shapes.medium.copy(
            topStart = CornerSize(0.dp),
            topEnd = CornerSize(0.dp)
        )
    ) {
        Column(
            Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(id = R.string.weeks),
                style = MaterialTheme.typography.labelLarge
            )
            Row {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text(stringResource(id = R.string.add_week_before))
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = { /*later*/ }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            stringResource(id = R.string.add_week_before),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Slider(
                    modifier = Modifier.weight(1f, true),
                    value = weekPosition.value,
                    onValueChange = { weekPosition.value = it },
                    valueRange = 0f..3f,
                    steps = 1
                )
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text(stringResource(id = R.string.add_week_after))
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = { /*later*/ }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            stringResource(id = R.string.add_week_after),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Slider(
                value = dayPosition.currentPage.toFloat(),
                onValueChange = {
                    coroutineScope.launch {
                        dayPosition.animateScrollToPage(it.roundToInt())
                    }
                },
                valueRange = 0f..6f,
                steps = 5
            )
            Text(
                stringResource(id = R.string.days),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun DayItem(modifier: Modifier, day: List<Event>) {
    Column(modifier) {
        LazyColumn {
            items(day) {
                val cardShape = if (day.size == 1) MaterialTheme.shapes.large else
                    if (day.indexOf(it) == 0) MaterialTheme.shapes.extraSmall.copy(
                        topStart = MaterialTheme.shapes.large.topStart,
                        topEnd = MaterialTheme.shapes.large.topEnd
                    ) else if (day.indexOf(it) == day.lastIndex) MaterialTheme.shapes.extraSmall.copy(
                        bottomStart = MaterialTheme.shapes.large.bottomStart,
                        bottomEnd = MaterialTheme.shapes.large.bottomEnd
                    ) else MaterialTheme.shapes.extraSmall
                Card(
                    Modifier
                        .padding(bottom = 2.dp)
                        .fillMaxWidth(),
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    LessonItem(event = it)
                }
            }
        }
    }
}

@Composable
fun LessonItem(event: Event) {
    Column {
        Text(
            "${event.roomNumber}::${event.subjectName}",
            Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
fun EventCalendarPreview() {
    OctoDiaryTheme {
        Surface {
            EventCalendar(modifier = Modifier.fillMaxSize(), listOf())
        }
    }
}