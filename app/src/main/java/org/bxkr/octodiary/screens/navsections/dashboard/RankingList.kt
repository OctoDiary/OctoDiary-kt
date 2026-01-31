package org.bxkr.octodiary.screens.navsections.dashboard


import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastJoinToString
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.components.RankingMemberCard

@Composable
fun RankingList() {
    LazyColumn(
        Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        items(DataService.ranking) { rankingMember ->
            val memberName = remember {
                if (rankingMember.personId != DataService.profile.children[DataService.currentProfile].contingentGuid) {
                    DataService.classMembers.firstOrNull { classMember ->
                        rankingMember.personId == classMember.personId
                    }?.fio
                } else DataService.profile.children[DataService.currentProfile].run {
                    listOf(
                        lastName,
                        firstName,
                        middleName
                    ).fastJoinToString(" ")
                }
            }
            RankingMemberCard(
                rankPlace = rankingMember.rank.rankPlace,
                average = rankingMember.rank.averageMarkFive,
                memberName = memberName ?: rankingMember.personId,
                highlighted = DataService.run { rankingMember.personId == profile.children[currentProfile].contingentGuid },
                isAnonymized = memberName == null
            )
        }
    }
}


