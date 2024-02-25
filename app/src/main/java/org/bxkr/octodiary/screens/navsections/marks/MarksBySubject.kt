package org.bxkr.octodiary.screens.navsections.marks

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.contentDependentActionLive
import org.bxkr.octodiary.parseFromDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksBySubject(scrollToSubjectId: Long? = null) {
    val filterState = remember { mutableStateOf(SubjectMarkFilterType.ByAverage) }
    contentDependentActionLive.postValue { SubjectMarkFilter(state = filterState) }
    val periods = remember {
        DataService.marksSubject.mapNotNull { it.period }.distinct()
    }
    var currentPeriod by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Bottom) {
        Crossfade(
            targetState = currentPeriod, modifier = Modifier.weight(1f), label = "subject_anim"
        ) { periodState ->
            AnimatedContent(targetState = filterState.value, label = "filter_anim") { filter ->
                Column {
                    val subjects =
                        DataService.marksSubject.filter { it.period == periods[periodState] }
                            .run {
                                when (filter) {
                                    SubjectMarkFilterType.Alphabetical -> sortedBy { it.subjectName }
                                    SubjectMarkFilterType.ByAverage -> sortedByDescending { it.average?.toDoubleOrNull() }
                                    SubjectMarkFilterType.ByRanking -> sortedBy { subject ->
                                        DataService.subjectRanking.firstOrNull { it.subjectId == subject.id }?.rank?.rankPlace
                                    }

                                    SubjectMarkFilterType.ByUpdated -> sortedByDescending {
                                        it.marks?.maxBy { it1 ->
                                            it1.date.parseFromDay().toInstant().toEpochMilli()
                                        }?.date?.parseFromDay()?.toInstant()?.toEpochMilli() ?: 0
                                    }
                                }
                            }
                    val lazyColumnState = rememberLazyListState()
                    LazyColumn(
                        Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 16.dp)
                            .weight(1f),
                        lazyColumnState
                    ) {
                        items(subjects) {
                            SubjectCard(subject = it)
                        }
                    }
                    if (scrollToSubjectId != null) {
                        LaunchedEffect(Unit) {
                            coroutineScope {
                                lazyColumnState.animateScrollToItem(subjects.indexOfFirst { it.id == scrollToSubjectId })
                                scrollToSubjectIdLive.postValue(null)
                            }
                        }
                    }
                    SecondaryScrollableTabRow(
                        selectedTabIndex = currentPeriod,
                        divider = {},
                        edgePadding = 0.dp
                    ) {
                        periods.forEachIndexed { index: Int, period: String ->
                            Tab(
                                selected = currentPeriod == index,
                                text = { Text(period) },
                                onClick = {
                                    currentPeriod = index
                                })
                        }
                    }
                }
            }
        }
    }
}