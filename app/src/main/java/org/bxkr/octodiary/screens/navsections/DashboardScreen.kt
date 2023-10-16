package org.bxkr.octodiary.screens.navsections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastJoinToString
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.modalBottomSheetContentLive
import org.bxkr.octodiary.modalBottomSheetStateLive

@Composable
fun DashboardScreen() {
    Column(verticalArrangement = Arrangement.Bottom) {
        Card(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                            .run { ranking.firstOrNull { it.personId == sessionUser.personId } }
                            ?.rank?.rankPlace ?: "?"
                    )
                )
            }
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
                }?.user?.run { listOf(lastName, firstName, middleName).fastJoinToString(" ") }
                    ?: rankingMember.personId
            }
            OutlinedCard(Modifier.padding(bottom = 8.dp)) {
                Row {
                    Row(Modifier.padding(8.dp)) {
                        Text(
                            rankingMember.rank.rankPlace.toString(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            rankingMember.rank.averageMarkFive.toString(),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            memberName,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
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