package org.bxkr.octodiary.screens.navsections.daybook


import androidx.compose.material.icons.Icons
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.R
import org.bxkr.octodiary.models.events.Event
import org.bxkr.octodiary.parseLongDate

fun LazyListScope.DayItem(
    day: List<Event>,
    showLessonNumbers: Boolean,
    showBreaks: Boolean,
    reversed: Boolean = false,
    addBelow: @Composable () -> Unit = {},
) {
    itemsIndexed(day.let { if (reversed) it.reversed() else it }) { index, it ->
        val cardShape =
            if (day.size == 1) MaterialTheme.shapes.large else if (day.indexOf(it) == 0) MaterialTheme.shapes.extraSmall.copy(
                topStart = MaterialTheme.shapes.large.topStart,
                topEnd = MaterialTheme.shapes.large.topEnd
            ) else if (day.indexOf(it) == day.lastIndex) MaterialTheme.shapes.extraSmall.copy(
                bottomStart = MaterialTheme.shapes.large.bottomStart,
                bottomEnd = MaterialTheme.shapes.large.bottomEnd
            ) else MaterialTheme.shapes.extraSmall
        val cardColor = MaterialTheme.colorScheme.run {
            when (it.source) {
                "AE" -> secondaryContainer
                "EC" -> primaryContainer
                "EVENTS" -> tertiaryContainer
                else -> surfaceContainer
            }
        }
        Card(
            Modifier
                .padding(bottom = 2.dp)
                .fillMaxWidth(),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            EventItem(
                event = it,
                day.filter { it1 -> it1.source == "PLAN" }.indexOf(it),
                showLessonNumbers
            )
        }
        if ((index != day.lastIndex) && showBreaks) {
            val currentEndDate = it.finishAt.parseLongDate()
            val nextStartDate = day[index + 1].startAt.parseLongDate()
            val breakDuration = (nextStartDate.time - currentEndDate.time).div(60000).toInt()
            if (breakDuration > 0) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.surfaceContainer,
                            MaterialTheme.shapes.extraSmall
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        pluralStringResource(R.plurals.minute_break, breakDuration, breakDuration),
                        Modifier
                            .alpha(.6f)
                            .padding(start = 16.dp),
                        MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(Modifier.height(2.dp))
            }
        }
    }
    item {
        addBelow()
    }
}


