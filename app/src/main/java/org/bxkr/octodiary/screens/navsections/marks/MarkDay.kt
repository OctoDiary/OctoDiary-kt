package org.bxkr.octodiary.screens.navsections.marks

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.formatToLongHumanDay
import org.bxkr.octodiary.formatToWeekday
import org.bxkr.octodiary.models.marklistdate.Mark
import org.bxkr.octodiary.parseFromDay
import org.bxkr.octodiary.parseSimpleLongDate

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