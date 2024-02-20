package org.bxkr.octodiary.screens.navsections.homeworks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.contentDependentActionIconLive
import org.bxkr.octodiary.contentDependentActionLive
import org.bxkr.octodiary.models.homeworks.Homework
import org.bxkr.octodiary.parseFromDay

val enabledSubjectsLive = MutableLiveData<List<Long>>(emptyList())

@Composable
fun HomeworksScreen() {
    LaunchedEffect(Unit) {
        val enable = { enabled: Boolean, id: Long ->
            if (enabledSubjectsLive.value != null) {
                if (enabled && enabledSubjectsLive.value?.contains(id) == false) {
                    enabledSubjectsLive.postValue(enabledSubjectsLive.value!! + listOf(id))
                } else if (!enabled && enabledSubjectsLive.value?.contains(id) == true) {
                    enabledSubjectsLive.postValue(enabledSubjectsLive.value!!.filter {
                        it != id
                    })
                }
            }
        }
        enabledSubjectsLive.postValue(DataService.homeworks.map { it.subjectId })
        contentDependentActionIconLive.postValue(Icons.Rounded.FilterAlt)
        contentDependentActionLive.postValue {
            DataService.homeworks.map { it.subjectId to it.subjectName }.toSet().forEach {
                var checked by rememberSaveable(key = it.first.toString()) {
                    mutableStateOf(
                        enabledSubjectsLive.value!!.any { it1 -> it1 == it.first })
                }
                DropdownMenuItem(text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked,
                            { isChecked -> checked = isChecked; enable(isChecked, it.first) })
                        Text(
                            it.second,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }, onClick = {
                    checked = !checked
                    enable(checked, it.first)
                })
            }
        }
    }

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
                    stringResource(R.string.next_week_homeworks_are_shown),
                    Modifier.alpha(.8f),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
