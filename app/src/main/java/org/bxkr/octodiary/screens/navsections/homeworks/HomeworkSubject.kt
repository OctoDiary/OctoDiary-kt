package org.bxkr.octodiary.screens.navsections.homeworks

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.models.homeworks.Homework


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
                    .clickable { expanded = !expanded }
                    .padding(
                        start = 8.dp, end = 16.dp, bottom = 16.dp
                    )) {
                Column(Modifier.padding(start = 8.dp)) {
                    if (it.materialsCount.isNotEmpty()) {
                        if (it.materialsCount.any { it.selectedMode == "learn" }) {
                            Text(
                                stringResource(
                                    R.string.learn_t,
                                    it.materialsCount.first { it.selectedMode == "learn" }.amount
                                ),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        if (it.materialsCount.any { it.selectedMode == "execute" }) {
                            Text(
                                stringResource(
                                    R.string.do_t,
                                    it.materialsCount.first { it.selectedMode == "execute" }.amount
                                ),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(isDone, { state ->
                        isDone = state
                        DataService.setHomeworkDoneState(it.homeworkEntryStudentId, state) {}
                    })
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