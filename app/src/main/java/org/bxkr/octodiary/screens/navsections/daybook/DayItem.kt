package org.bxkr.octodiary.screens.navsections.daybook

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.models.events.Event

fun LazyListScope.DayItem(
    day: List<Event>,
    showLessonNumbers: Boolean = true,
    addBelow: @Composable () -> Unit = {},
) {
    items(day) {
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
    }
    item {
        addBelow()
    }
}