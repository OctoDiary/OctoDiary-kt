package org.bxkr.octodiary.screens.navsections.marks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastJoinToString
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.RankingMemberCard
import org.bxkr.octodiary.models.rankingforsubject.RankingForSubject
import org.bxkr.octodiary.network.NetworkService

@Composable
fun SubjectRatingBottomSheet(subjectId: Long, subjectName: String) {
    var ranking by remember { mutableStateOf<List<RankingForSubject>?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        DataService.getRankingForSubject(subjectId, { errorText = it }) { ranking = it }
    }

    Box(
        Modifier
            .heightIn(192.dp, Int.MAX_VALUE.dp)
            .fillMaxWidth()
    ) {
        if (ranking != null) {
            LazyColumn(Modifier.padding(8.dp)) {
                item {
                    Text(
                        subjectName,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                items(ranking!!) {
                    val memberName = remember {
                        DataService.classMembers.firstOrNull { classMember ->
                            it.personId == classMember.personId
                        }?.user?.run {
                            listOf(
                                lastName,
                                firstName,
                                middleName ?: ""
                            ).fastJoinToString(" ")
                        }
                            ?: it.personId
                    }

                    RankingMemberCard(
                        rankPlace = it.rank.rankPlace,
                        average = it.rank.averageMarkFive,
                        memberName = memberName,
                        highlighted = DataService.run { it.personId == profile.children[currentProfile].contingentGuid }
                    )
                }
            }
        } else if (errorText != null) {
            Column(
                Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Rounded.Warning,
                    stringResource(R.string.error_occurred),
                    Modifier.size(64.dp),
                    MaterialTheme.colorScheme.secondary
                )
                Text(
                    stringResource(R.string.error_occurred),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(errorText!!, textAlign = TextAlign.Center)
                val uriHandler = LocalUriHandler.current
                OutlinedButton(
                    onClick = { uriHandler.openUri(NetworkService.ExternalIntegrationConfig.TELEGRAM_REPORT_URL) },
                    modifier = Modifier.padding(16.dp),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                ) {
                    Icon(
                        Icons.Rounded.BugReport,
                        stringResource(id = R.string.report_issue),
                        Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.report_issue))
                }
            }
        } else {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}