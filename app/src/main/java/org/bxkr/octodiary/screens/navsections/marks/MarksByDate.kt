package org.bxkr.octodiary.screens.navsections.marks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.contentDependentActionLive
import org.bxkr.octodiary.formatToDay
import org.bxkr.octodiary.models.marklistdate.Mark
import org.bxkr.octodiary.parseFromDay
import org.bxkr.octodiary.parseSimpleLongDate


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