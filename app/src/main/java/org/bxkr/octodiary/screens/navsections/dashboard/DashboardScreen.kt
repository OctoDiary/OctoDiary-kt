package org.bxkr.octodiary.screens.navsections.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.formatToDay
import org.bxkr.octodiary.formatToHumanDay
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.modalBottomSheetContentLive
import org.bxkr.octodiary.modalBottomSheetStateLive
import org.bxkr.octodiary.parseFromDay
import org.bxkr.octodiary.parseLongDate
import org.bxkr.octodiary.screens.navsections.daybook.DayItem
import java.util.Date

@Composable
fun DashboardScreen() {
    val showNumbers = LocalContext.current.mainPrefs.get("show_lesson_numbers") ?: true
    LazyColumn(
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxHeight()
    ) {
        item {
            val currentDay = remember { Date().formatToDay() }
            Column(
                verticalArrangement = Arrangement.Bottom
            ) {
                val todayCalendar = DataService.eventCalendar.filter {
                    it.startAt.parseLongDate().formatToDay() == currentDay
                }
                val nearestEvent =
                    DataService.eventCalendar.filter { it.startAt.parseLongDate().time > Date().time }
                        .minByOrNull {
                            it.startAt.parseLongDate().time - Date().time
                        }
                if (todayCalendar.isNotEmpty() && Date() < todayCalendar.maxBy { it.finishAt.parseLongDate() }.finishAt.parseLongDate()) {
                    Text(
                        stringResource(id = R.string.schedule_today),
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                } else if (nearestEvent != null) {
                    Text(
                        stringResource(
                            id = R.string.schedule_for,
                            nearestEvent.startAt.parseLongDate().formatToHumanDay()
                        ),
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
        DayItem(
            day = DataService.eventCalendar.filter { it.startAt.parseLongDate().time > Date().time }
                .minByOrNull {
                    it.startAt.parseLongDate().time - Date().time
                }?.startAt?.parseLongDate()?.formatToDay()?.let { day ->
                    DataService.eventCalendar.filter {
                        it.startAt.parseLongDate().formatToDay() == day
                    }
                } ?: listOf(), showNumbers)
        item {
            if (LocalContext.current.mainPrefs.get("main_rating") ?: true) {
                Text(
                    stringResource(id = R.string.rating),
                    modifier = Modifier.padding(top = 8.dp),
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
            if (DataService.hasVisits && DataService.visits.payload.isNotEmpty()) {
                val lastVisit = DataService.visits.payload.maxBy {
                    it.date.parseFromDay().toInstant().toEpochMilli()
                }
                Text(
                    text = stringResource(
                        R.string.visits_t,
                        lastVisit.date.parseFromDay().formatToHumanDay()
                    ),
                    modifier = Modifier.padding(top = 8.dp),
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
            Spacer(Modifier.height(16.dp))
        }
    }
}
