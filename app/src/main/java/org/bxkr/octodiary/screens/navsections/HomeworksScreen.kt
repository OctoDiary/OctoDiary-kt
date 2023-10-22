package org.bxkr.octodiary.screens.navsections

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.formatToLongHumanDay
import org.bxkr.octodiary.formatToWeekday
import org.bxkr.octodiary.models.homeworks.Homework
import org.bxkr.octodiary.parseFromDay

@Composable
fun HomeworksScreen() {
    val daySplitMarks = remember {
        DataService.homeworks.sortedBy {
            it.date.parseFromDay().toInstant().toEpochMilli()
        }.fold(mutableListOf<MutableList<Homework>>()) { sum, it ->
            if (sum.isEmpty() || sum.last().first().date != it.date) {
                sum.add(mutableListOf(it))
            } else {
                sum.last().add(it)
            }
            sum
        }
    }
    LazyColumn(Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
        items(daySplitMarks) {
            HomeworkDay(homeworks = it)
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "показаны домашние задания на неделю вперёд",
                    Modifier.alpha(.8f),
                    style = MaterialTheme.typography.labelMedium
                ) // FUTURE: UNTRANSLATED
            }
        }
    }
}

@Composable
fun HomeworkDay(homeworks: List<Homework>) {
    Column(Modifier.padding(bottom = 16.dp)) {
        val date = homeworks[0].date.parseFromDay()
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
        val subjectSplitHomeworks =
            homeworks.fold(mutableListOf<MutableList<Homework>>()) { sum, it ->
                if (sum.isEmpty() || sum.last().first().subjectId != it.subjectId) {
                    sum.add(mutableListOf(it))
                } else {
                    sum.last().add(it)
                }
                sum
            }
        subjectSplitHomeworks.forEach {
            val cardShape =
                if (homeworks.size == 1) MaterialTheme.shapes.large else if (homeworks.indexOf(it.first()) == 0) MaterialTheme.shapes.extraSmall.copy(
                    topStart = MaterialTheme.shapes.large.topStart,
                    topEnd = MaterialTheme.shapes.large.topEnd
                ) else if (homeworks.indexOf(it.last()) == homeworks.lastIndex) MaterialTheme.shapes.extraSmall.copy(
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
                HomeworkSubject(homeworks = it)
            }
        }
    }
}

@Composable
fun HomeworkSubject(homeworks: List<Homework>) {
    Column {
        Text(
            homeworks.first().subjectName,
            Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
            style = MaterialTheme.typography.titleMedium
        )
        homeworks.forEach {
            var isDone by rememberSaveable { mutableStateOf(it.isDone) }
            var expanded by remember { mutableStateOf(false) }
            Column(
                Modifier
                    .clickable { expanded = !expanded } // FUTURE: SEND_HOMEWORK_DONE_STATE
                    .padding(
                        start = 8.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
            ) {
                Column(Modifier.padding(start = 8.dp)) {
                    if (it.materialsCount.isNotEmpty()) {
                        if (it.materialsCount.any { it.selectedMode == "learn" }) {
                            Text(
                                "${it.materialsCount.first { it.selectedMode == "learn" }.amount} выучить", // FUTURE: UNTRANSLATED
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        if (it.materialsCount.any { it.selectedMode == "execute" }) {
                            Text(
                                "${it.materialsCount.first { it.selectedMode == "execute" }.amount} выполнить", // FUTURE: UNTRANSLATED
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        isDone,
                        { isDone = it }
                    ) // FUTURE: SEND_HOMEWORK_DONE_STATE
                    Text(
                        it.description,
                        Modifier.animateContentSize(),
                        maxLines = if (!expanded) 3 else Int.MAX_VALUE,
                        overflow = TextOverflow.Ellipsis
                    ) // FUTURE: COPY_ON_LONG_PRESS
                }
            }
        }
    }
}