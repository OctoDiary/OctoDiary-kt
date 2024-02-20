package org.bxkr.octodiary.screens.navsections.homeworks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.formatToLongHumanDay
import org.bxkr.octodiary.formatToWeekday
import org.bxkr.octodiary.models.homeworks.Homework
import org.bxkr.octodiary.parseFromDay

@Composable
fun HomeworkDay(homeworks: List<Homework>) {
    val enabledSubjects = enabledSubjectsLive.observeAsState()
    AnimatedVisibility(visible = homeworks.any { it.subjectId in enabledSubjects.value!! }) {
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
                remember {
                    homeworks.fold(mutableListOf<MutableList<Homework>>()) { sum, it ->
                        if (sum.isEmpty() || sum.last().first().subjectId != it.subjectId) {
                            sum.add(mutableListOf(it))
                        } else {
                            sum.last().add(it)
                        }
                        sum
                    }
                }
            Column(
                Modifier.clip(MaterialTheme.shapes.large),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                subjectSplitHomeworks.forEach {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraSmall,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        AnimatedVisibility(visible = it.first().subjectId in enabledSubjects.value!!) {
                            HomeworkSubject(homeworks = it)
                        }
                    }
                }
            }
        }
    }
}