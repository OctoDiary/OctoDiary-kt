package org.bxkr.octodiary.screens.navsections.dashboard

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.settings.CommonPrefs
import org.bxkr.octodiary.demoScheduleDate
import org.bxkr.octodiary.formatToDay
import org.bxkr.octodiary.formatToHumanDay
import org.bxkr.octodiary.get
import org.bxkr.octodiary.isDemo
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.modalBottomSheetContentLive
import org.bxkr.octodiary.modalBottomSheetStateLive
import org.bxkr.octodiary.parseFromDay
import org.bxkr.octodiary.parseLongDate
import org.bxkr.octodiary.screens.navsections.daybook.DayItem
import java.util.Date

@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val showNumbers =
        context.mainPrefs.get(CommonPrefs.showLessonNumbers.prefKey) ?: true
    
    // Проверяем, включён ли AI для показа карточки
    val aiEnabled = context.getSharedPreferences("main_prefs", android.content.Context.MODE_PRIVATE)
        .getBoolean("ai_enabled", true)
    val state = rememberLazyListState()
    LazyColumn(
        state = state,
        reverseLayout = true,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxHeight()
    ) {
        item {
            Spacer(Modifier.height(16.dp))
        }
        // AI Quick Access Card
        if (aiEnabled) {
            item {
                org.bxkr.octodiary.components.ai.AiQuickAccessCard()
            }
        }
        dashboardRatingVisits()
        item {
            ChangelogCard(context)
        }
        dashboardSchedule(context, showNumbers)
    }
}

fun LazyListScope.dashboardSchedule(context: Context, showNumbers: Boolean) {
    val date = if (context.isDemo) {
        demoScheduleDate
    } else Date()
    item {
        Spacer(Modifier.size(8.dp))
    }
    DayItem(
        day = DataService.eventCalendar.filter { it.startAt.parseLongDate().time > date.time }
            .minByOrNull {
                it.startAt.parseLongDate().time - date.time
            }?.startAt?.parseLongDate()?.formatToDay()?.let { day ->
                DataService.eventCalendar.filter {
                    it.startAt.parseLongDate().formatToDay() == day
                }
            } ?: listOf(), showNumbers, showBreaks = false, reversed = true)
    item {
        val currentDay = remember { date.formatToDay() }
        Column(
            verticalArrangement = Arrangement.Bottom
        ) {
            val todayCalendar = DataService.eventCalendar.filter {
                it.startAt.parseLongDate().formatToDay() == currentDay
            }
            val nearestEvent =
                DataService.eventCalendar.filter { it.startAt.parseLongDate().time > date.time }
                    .minByOrNull {
                        it.startAt.parseLongDate().time - date.time
                    }
            if (todayCalendar.isNotEmpty() && date < todayCalendar.maxBy { it.finishAt.parseLongDate() }.finishAt.parseLongDate()) {
                Text(
                    stringResource(id = R.string.schedule_today),
                    style = MaterialTheme.typography.labelLarge
                )
            } else if (nearestEvent != null) {
                Text(
                    stringResource(
                        id = R.string.schedule_for,
                        nearestEvent.startAt.parseLongDate().formatToHumanDay()
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

fun LazyListScope.dashboardRatingVisits() {
    item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)) {
            Column {
                if (LocalContext.current.mainPrefs.get<Boolean>(CommonPrefs.mainRating.prefKey) != false) {
                    Text(
                        stringResource(id = R.string.rating),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                modalBottomSheetContentLive.value = { RankingList() }
                                modalBottomSheetStateLive.postValue(true)
                            }
                    ) {
                        Column(
                            Modifier
                                .padding(16.dp)
                        ) {
                            Text(
                                stringResource(
                                    id = R.string.rating_place,
                                    DataService
                                        .run { ranking.firstOrNull { it.personId == profile.children[currentProfile].contingentGuid } }
                                        ?.rank?.rankPlace ?: "?"
                                )
                            )
                        }
                    }
                }
            }
            Column {
                if (DataService.hasVisits && DataService.visits.payload.isNotEmpty()) {
                    val lastVisit =
                        DataService.visits.payload.filter { day -> !day.visits.any { it.inX == "-" && it.out == "-" } }
                            .maxByOrNull {
                                it.date.parseFromDay().toInstant().toEpochMilli()
                            }
                    if (lastVisit != null) {
                        Text(
                            text = stringResource(
                                R.string.visits_t,
                                lastVisit.date.parseFromDay().formatToHumanDay()
                            ),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    modalBottomSheetContentLive.value = { VisitsList() }
                                    modalBottomSheetStateLive.postValue(true)
                                }
                        ) {
                            Row(
                                Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(lastVisit.visits[0].inX)
                                Icon(
                                    Icons.AutoMirrored.Rounded.ArrowForward,
                                    stringResource(id = R.string.to)
                                )
                                Text(lastVisit.visits[0].out)
                            }
                        }
                    }
                }
            }
        }
    }
}
