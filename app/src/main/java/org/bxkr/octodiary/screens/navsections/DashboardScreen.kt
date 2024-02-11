package org.bxkr.octodiary.screens.navsections

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastJoinToString
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.RankingMemberCard
import org.bxkr.octodiary.formatToDay
import org.bxkr.octodiary.formatToHumanDay
import org.bxkr.octodiary.modalBottomSheetContentLive
import org.bxkr.octodiary.modalBottomSheetStateLive
import org.bxkr.octodiary.parseFromDay
import org.bxkr.octodiary.parseLongDate
import java.util.Date

@Composable
fun DashboardScreen() {
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
                if (todayCalendar.isNotEmpty()) {
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
        dayItem(
            day = DataService.eventCalendar.filter { it.startAt.parseLongDate().time > Date().time }
                .minByOrNull {
                    it.startAt.parseLongDate().time - Date().time
                }?.startAt?.parseLongDate()?.formatToDay()?.let { day ->
                DataService.eventCalendar.filter {
                    it.startAt.parseLongDate().formatToDay() == day
                }
            } ?: listOf())
        item {
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

@Composable
fun RankingList() {
    LazyColumn(
        Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        items(DataService.ranking) { rankingMember ->
            val memberName = remember {
                DataService.classMembers.firstOrNull { classMember ->
                    rankingMember.personId == classMember.personId
                }?.user?.run { listOf(lastName, firstName, middleName ?: "").fastJoinToString(" ") }
                    ?: rankingMember.personId
            }
            RankingMemberCard(
                rankPlace = rankingMember.rank.rankPlace,
                average = rankingMember.rank.averageMarkFive,
                memberName = memberName,
                highlighted = DataService.run { rankingMember.personId == profile.children[currentProfile].contingentGuid }
            )
        }
    }
}

@Composable
fun VisitsList() {
    LazyColumn(
        Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        items(DataService.visits.payload) {
            // MES sets "-" for visit time if there was no visit on that day
            if (it.visits[0].run { inX != "-" && out != "-" }) {
                OutlinedCard(Modifier.padding(bottom = 8.dp)) {
                    Row {
                        it.visits.forEachIndexed { index, visit ->
                            Row(Modifier.padding(8.dp)) {
                                Text(
                                    it.date.parseFromDay().formatToHumanDay(),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .alpha(
                                            if (index == 0) 1f else 0f
                                        )
                                )
                                Text(
                                    visit.inX,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Icon(
                                    Icons.AutoMirrored.Rounded.ArrowForward,
                                    stringResource(id = R.string.to),
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(
                                    visit.out,
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}