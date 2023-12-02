package org.bxkr.octodiary.screens.navsections

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastJoinToString
import org.bxkr.octodiary.CloverShape
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.Mark
import org.bxkr.octodiary.components.RankingMemberCard
import org.bxkr.octodiary.components.defaultMarkClick
import org.bxkr.octodiary.contentDependentActionIconLive
import org.bxkr.octodiary.contentDependentActionLive
import org.bxkr.octodiary.formatToDay
import org.bxkr.octodiary.formatToLongHumanDay
import org.bxkr.octodiary.formatToWeekday
import org.bxkr.octodiary.modalBottomSheetContentLive
import org.bxkr.octodiary.modalBottomSheetStateLive
import org.bxkr.octodiary.models.marklistdate.Mark
import org.bxkr.octodiary.models.marklistsubject.MarkListSubjectItem
import org.bxkr.octodiary.models.rankingforsubject.RankingForSubject
import org.bxkr.octodiary.parseFromDay
import org.bxkr.octodiary.parseSimpleLongDate
import org.bxkr.octodiary.showFilterLive

enum class MarksScreenTab(
    @StringRes val title: Int, val icon: ImageVector
) {
    ByDate(
        R.string.by_date, Icons.Rounded.DateRange
    ),
    BySubject(
        R.string.by_subject, Icons.Rounded.Book
    )
}

enum class DateMarkFilterType(
    @StringRes val title: Int
) {
    ByUpdated(R.string.mark_filter_by_updated),
    ByLessonDate(R.string.mark_filter_by_lesson_date)
}

enum class SubjectMarkFilterType(
    @StringRes val title: Int
) {
    ByAverage(R.string.mark_filter_by_average),
    ByRanking(R.string.mark_filter_by_ranking),
    ByUpdated(R.string.mark_filter_by_last_update),
    Alphabetical(R.string.mark_filter_alphabetical)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksScreen() {
    showFilterLive.postValue(true)
    contentDependentActionIconLive.postValue(Icons.AutoMirrored.Rounded.Sort)
    var currentTab by remember { mutableStateOf(MarksScreenTab.ByDate) }
    Column {
        if (DataService.marksSubject.isNotEmpty()) {
            PrimaryTabRow(selectedTabIndex = currentTab.ordinal, divider = {}) {
                MarksScreenTab.values().forEach {
                    Tab(
                        selected = currentTab == it,
                        onClick = {
                            currentTab = it
                        },
                        text = { Text(stringResource(id = it.title)) },
                        icon = { Icon(it.icon, stringResource(it.title)) },
                        modifier = Modifier.clip(MaterialTheme.shapes.large)
                    )
                }
            }
        }
        Crossfade(targetState = currentTab, label = "marks_tab_anim") {
            when (it) {
                MarksScreenTab.ByDate -> MarksByDate()
                MarksScreenTab.BySubject -> MarksBySubject()
            }
        }
    }
}

@Composable
fun MarksByDate() {
    val filterState = remember { mutableStateOf(DateMarkFilterType.ByUpdated) }
    contentDependentActionLive.postValue { DateMarkFilter(state = filterState) }
    val daySplitMarks = DataService.marksDate.payload.sortedByDescending {
        when (filterState.value) {
            DateMarkFilterType.ByUpdated -> it.updatedAt.parseSimpleLongDate()
            DateMarkFilterType.ByLessonDate -> it.lessonDate.parseFromDay()
        }
    }.fold(mutableListOf<MutableList<Mark>>()) { sum, it ->
        val condition: () -> Boolean = when (filterState.value) {
            DateMarkFilterType.ByUpdated -> {
                {
                    sum.last().first().updatedAt.parseSimpleLongDate()
                        .formatToDay() != it.updatedAt.parseSimpleLongDate().formatToDay()
                }
            }

            DateMarkFilterType.ByLessonDate -> {
                { sum.last().first().lessonDate != it.lessonDate }
            }
        }
        if (sum.isEmpty() || condition()) {
            sum.add(mutableListOf(it))
        } else {
            sum.last().add(it)
        }
        sum
    }
    LazyColumn(Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)) {
        items(daySplitMarks) {
            MarkDay(marks = it, filterState)
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.last_week_marks_are_shown),
                    Modifier.alpha(.8f),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun MarkDay(marks: List<Mark>, filterType: MutableState<DateMarkFilterType>) {
    AnimatedContent(targetState = filterType.value, label = "filter_anim") {
        Column(Modifier.padding(bottom = 16.dp)) {
            val date = when (it) {
                DateMarkFilterType.ByUpdated -> marks[0].updatedAt.parseSimpleLongDate()
                DateMarkFilterType.ByLessonDate -> marks[0].lessonDate.parseFromDay()
            }
            Row {
                Text(
                    date.formatToLongHumanDay(),
                    Modifier.padding(end = 3.dp),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    date.formatToWeekday(),
                    Modifier.alpha(.8f),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            marks.forEach {
                val cardShape =
                    if (marks.size == 1) MaterialTheme.shapes.large else if (marks.indexOf(it) == 0) MaterialTheme.shapes.extraSmall.copy(
                        topStart = MaterialTheme.shapes.large.topStart,
                        topEnd = MaterialTheme.shapes.large.topEnd
                    ) else if (marks.indexOf(it) == marks.lastIndex) MaterialTheme.shapes.extraSmall.copy(
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
                    ExtendedMark(mark = it)
                }
            }
        }
    }
}

@Composable
fun ExtendedMark(mark: Mark) {
    val eventMark = org.bxkr.octodiary.models.events.Mark.fromMarkListDate(mark)
    Column(Modifier.clickable { defaultMarkClick(eventMark) }) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(mark.subjectName, style = MaterialTheme.typography.titleMedium)
                Text(mark.controlFormName)
            }
            Mark(eventMark)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksBySubject() {
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
                                        DataService.subjectRanking.first { it.subjectId == subject.id }.rank.rankPlace
                                    }
                                    SubjectMarkFilterType.ByUpdated -> sortedByDescending {
                                        it.marks?.maxBy { it1 ->
                                            it1.date.parseFromDay().toInstant().toEpochMilli()
                                        }?.date?.parseFromDay()?.toInstant()?.toEpochMilli() ?: 0
                                    }
                                }
                            }
                    LazyColumn(
                        Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 16.dp)
                            .weight(1f)
                    ) {
                        items(subjects) {
                            SubjectCard(subject = it)
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

@Composable
fun SubjectCard(subject: MarkListSubjectItem) {
    Card(
        Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    subject.subjectName,
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis
                )
                Row {
                    if (subject.average != null) {
                        FilterChip(
                            onClick = {},
                            label = {
                                Text(
                                    subject.average, color = when (subject.dynamic) {
                                        "UP" -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onTertiaryContainer
                                    }
                                )
                            },
                            selected = true,
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = if (subject.dynamic == "UP") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer),
                            leadingIcon = {
                                if (subject.dynamic == "UP") {
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowDropUp,
                                        contentDescription = subject.dynamic,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowDropDown,
                                        contentDescription = subject.dynamic,
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            },
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    if (subject.fixedValue != null) {
                        FilterChip(
                            selected = true, onClick = {}, label = { Text(subject.fixedValue) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Done,
                                    contentDescription = stringResource(id = R.string.final_mark),
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            },
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
            if (subject.marks != null) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    LazyRow {
                        items(subject.marks) {
                            Mark(org.bxkr.octodiary.models.events.Mark.fromMarkListSubject(it))
                        }
                    }
                    FilledIconButton(onClick = {
                        modalBottomSheetStateLive.postValue(true)
                        modalBottomSheetContentLive.postValue {
                            SubjectRatingBottomSheet(
                                subject.id,
                                subject.subjectName
                            )
                        }
                    }, shape = CloverShape) {
                        Text(
                            DataService.subjectRanking.first { it.subjectId == subject.id }.rank.rankPlace.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DateMarkFilter(state: MutableState<DateMarkFilterType>) {
    DateMarkFilterType.values().forEach {
        DropdownMenuItem(text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(state.value == it,
                    { state.value = it })
                Text(
                    stringResource(it.title),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }, onClick = { state.value = it })
    }
}

@Composable
fun SubjectMarkFilter(state: MutableState<SubjectMarkFilterType>) {
    SubjectMarkFilterType.values().forEach {
        DropdownMenuItem(text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(state.value == it,
                    { state.value = it })
                Text(
                    stringResource(it.title),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }, onClick = { state.value = it })
    }
}

@Composable
fun SubjectRatingBottomSheet(subjectId: Long, subjectName: String) {
    var ranking by remember { mutableStateOf<List<RankingForSubject>?>(null) }
    LaunchedEffect(Unit) {
        DataService.getRankingForSubject(subjectId) { ranking = it }
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
        } else {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}