package org.bxkr.octodiary.screens.navsections.marks


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.contentDependentActionLive
import org.bxkr.octodiary.get
import org.bxkr.octodiary.getMarkConfig
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.parseFromDay
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksBySubject(scrollToSubjectId: Long? = null) {
    val filterState = remember { mutableStateOf(SubjectMarkFilterType.ByAverage) }
    contentDependentActionLive.postValue { SubjectMarkFilter(state = filterState) }
    val periods = remember {
        DataService.marksSubject.maxBy {
            it.periods?.size ?: 0
        }.periods?.map { it.title to (it.startIso to it.endIso) }
    }
    var currentPeriod by remember {
        mutableStateOf(
            periods?.firstOrNull { (it.second.first.parseFromDay().time < Date().time) and (it.second.second.parseFromDay().time > Date().time) }?.first
                ?: periods?.firstOrNull()?.first
        )
    }
    var finalSelected by remember { mutableStateOf(false) }
    val markConfig = getMarkConfig()
    if (periods?.size?.let { it > 0 } == true) {
        Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Bottom) {
            Crossfade(targetState = finalSelected, modifier = Modifier.weight(1f)) {
                if (!it) {
                    Crossfade(
                        targetState = currentPeriod, label = "subject_anim"
                    ) { periodState ->
                        AnimatedContent(
                            targetState = filterState.value,
                            label = "filter_anim"
                        ) { filter ->
                            Column {
                                val subjects =
                                    DataService.marksSubject.filter {
                                        it.periods != null && it.periods.map { it.title }
                                            .contains(periodState)
                                    }
                                        .run {
                                            when (filter) {
                                                SubjectMarkFilterType.Alphabetical -> sortedBy { it.subjectName }
                                                SubjectMarkFilterType.ByAverage -> sortedByDescending { it.periods?.first { it.title == periodState }?.value?.toDoubleOrNull() }
                                                SubjectMarkFilterType.ByRanking -> sortedBy { subject ->
                                                    DataService.subjectRanking.firstOrNull { it.subjectId == subject.subjectId }?.rank?.rankPlace
                                                }

                                                SubjectMarkFilterType.ByUpdated -> sortedByDescending {
                                                    it.periods?.first { it.title == periodState }?.marks?.maxBy { it1 ->
                                                        it1.date.parseFromDay().toInstant()
                                                            .toEpochMilli()
                                                    }?.date?.parseFromDay()?.toInstant()
                                                        ?.toEpochMilli()
                                                        ?: 0
                                                }
                                            }
                                        }
                                val lazyColumnState = rememberLazyListState()
                                val context = LocalContext.current
                                LazyColumn(
                                    Modifier
                                        .fillMaxHeight()
                                        .padding(horizontal = 16.dp)
                                        .weight(1f),
                                    lazyColumnState
                                ) {
                                    val helpingIndex = (0..3).random()
                                    val showHints =
                                        context.mainPrefs.get<Boolean>("show_calc_hint") ?: true
                                    itemsIndexed(subjects) { index, it ->
                                        if (it.periods != null && it.periods.any { it.title == periodState }) {
                                            val sentPeriod =
                                                it.periods.first { it.title == periodState }
                                            SubjectCard(
                                                period = sentPeriod,
                                                it.subjectId,
                                                it.subjectName,
                                                sentPeriod == it.currentPeriod,
                                                markConfig,
                                                showHintOnce = (index == helpingIndex) && showHints
                                            )
                                        }
                                    }
                                }
                                if (scrollToSubjectId != null) {
                                    LaunchedEffect(Unit) {
                                        coroutineScope {
                                            lazyColumnState.animateScrollToItem(subjects.indexOfFirst { it.subjectId == scrollToSubjectId })
                                            scrollToSubjectIdLive.postValue(null)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    FinalsScreen()
                }
            }
            SecondaryScrollableTabRow(
                selectedTabIndex = if (!finalSelected) periods?.map { it.first }
                    ?.indexOf(currentPeriod) ?: 0 else periods?.map { it.first }?.size
                    ?: 0,
                divider = {},
                edgePadding = 0.dp
            ) {
                periods?.forEachIndexed { index: Int, period: Pair<String, Pair<String, String>> ->
                    Tab(
                        selected = (periods.map { it.first }
                            .indexOf(currentPeriod) == index) && !finalSelected,
                        text = { Text(period.first) },
                        onClick = {
                            currentPeriod = periods[index].first
                            finalSelected = false
                        })
                }
                Tab(
                    selected = finalSelected,
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                stringResource(R.string.finals),
                                Modifier
                                    .padding(end = 4.dp)
                                    .size(16.dp),
                                MaterialTheme.colorScheme.secondary
                            )
                            Text(stringResource(R.string.finals))
                        }
                    },
                    onClick = { finalSelected = true }
                )
            }
        }
    } else {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.no_periods_yet),
                Modifier.alpha(.8f),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}


