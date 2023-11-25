package org.bxkr.octodiary.screens.navsections

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.Mark
import org.bxkr.octodiary.components.defaultMarkClick
import org.bxkr.octodiary.formatToLongHumanDay
import org.bxkr.octodiary.formatToWeekday
import org.bxkr.octodiary.models.marklistdate.Mark
import org.bxkr.octodiary.parseFromDay

enum class MarksScreenTab(
    @StringRes val title: Int,
    val icon: ImageVector
) {
    ByDate(
        R.string.by_date,
        Icons.Rounded.DateRange
    ),
    BySubject(
        R.string.by_subject,
        Icons.Rounded.Book
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksScreen() {
    var currentTab by remember { mutableStateOf(MarksScreenTab.ByDate) }
    Column {
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
    val daySplitMarks = remember {
        DataService.marks.payload.sortedByDescending {
            it.date.parseFromDay().toInstant().toEpochMilli()
        }.fold(mutableListOf<MutableList<Mark>>()) { sum, it ->
            if (sum.isEmpty() || sum.last().first().date != it.date) {
                sum.add(mutableListOf(it))
            } else {
                sum.last().add(it)
            }
            sum
        }
    }
    LazyColumn(Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)) {
        items(daySplitMarks) {
            MarkDay(marks = it)
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
fun MarkDay(marks: List<Mark>) {
    Column(Modifier.padding(bottom = 16.dp)) {
        val date = marks[0].date.parseFromDay()
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

@Composable
fun ExtendedMark(mark: Mark) {
    val eventMark = org.bxkr.octodiary.models.events.Mark.fromMarkList(mark)
    Column(
        Modifier
            .clickable { defaultMarkClick(eventMark) }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(mark.subjectName, style = MaterialTheme.typography.titleMedium)
                Text(mark.controlFormName)
            }
            Mark(eventMark)
        }
    }
}

@Composable
fun MarksBySubject() {

}