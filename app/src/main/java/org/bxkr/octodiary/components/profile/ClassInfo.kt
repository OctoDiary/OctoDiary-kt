package org.bxkr.octodiary.components.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.screens.navsections.dashboard.RankingList

@Composable
fun ClassInfo() {
    var showRanking by remember { mutableStateOf(false) }
    val enterTransition1 = remember {
        slideInHorizontally(
            tween(200)
        ) { it }
    }
    val exitTransition1 = remember {
        slideOutHorizontally(
            tween(200)
        ) { it }
    }
    val enterTransition2 = remember {
        slideInHorizontally(
            tween(200)
        ) { -it }
    }
    val exitTransition2 = remember {
        slideOutHorizontally(
            tween(200)
        ) { -it }
    }
    Box {
        AnimatedVisibility(
            visible = showRanking,
            enter = enterTransition1,
            exit = exitTransition1
        ) {
            Column {
                IconButton(onClick = { showRanking = false }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(id = R.string.back))
                }
                RankingList()
            }
        }
        AnimatedVisibility(
            visible = !showRanking,
            enter = enterTransition2,
            exit = exitTransition2
        ) {
            Column(Modifier.padding(16.dp)) {
                with(DataService) {
                    Text(
                        stringResource(
                            R.string.class_t,
                            profile.children[currentProfile].className
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        LocalContext.current.resources.getQuantityString(
                            R.plurals.student_count,
                            classMembers.size,
                            classMembers.size
                        )
                    )
                    Row(
                        Modifier
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(R.string.name_column),
                            Modifier.alpha(.8f),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            stringResource(R.string.rating_column),
                            Modifier.alpha(.8f),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    LazyColumn(
                        Modifier.clip(MaterialTheme.shapes.large),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(classMembers) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                ),
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            it.user.lastName,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(it.user.run { "$firstName ${middleName ?: ""}" })
                                    }
                                    FilledTonalIconButton(
                                        onClick = { showRanking = true },
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            ranking.firstOrNull { rankingMember -> it.personId == rankingMember.personId }
                                                ?.rank?.rankPlace?.toString()
                                                ?: "?",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}