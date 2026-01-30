package org.bxkr.octodiary.screens.navsections.daybook

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import org.bxkr.octodiary.formatToDay
import java.util.Calendar
import java.util.Collections
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun CalendarRow(
    date: Calendar,
    daySelected: State<Date>,
    onDaySelect: (Date) -> Unit,
    modifier: Modifier = Modifier,
) {
    val weekdays = remember {
        listOf(
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
            Calendar.SUNDAY
        )
    }
    var selectedPositionX: Float by remember { mutableFloatStateOf(0f) }
    val selectedPosition = animateFloatAsState(selectedPositionX)
    var cellSize by remember { mutableStateOf<IntSize?>(null) }
    Box(modifier.padding(horizontal = 8.dp)) {
        val density = LocalDensity.current
        Box(
            Modifier
                .offset {
                    IntOffset(selectedPosition.value.roundToInt(), 0)
                }
                .size(with(density) {
                    (cellSize ?: IntSize(0, 0))
                        .toSize()
                        .toDpSize()
                })
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.shapes.medium,
                )
        )
        Row {
            weekdays.forEach { weekday ->
                val cellDate =
                    (date.clone() as Calendar).apply {
                        set(Calendar.DAY_OF_WEEK, weekday)
                    }

                val isSelected = daySelected.value.formatToDay() == cellDate.time.formatToDay()
                var cellPosition: Float by remember { mutableFloatStateOf(0f) }
                if (isSelected) {
                    selectedPositionX = cellPosition
                }
                CalendarCell(cellDate,
                    Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .clickable {
                            onDaySelect(cellDate.time)
                        }
                        .weight(1f)
                        .onPlaced { coordinates ->
                            cellPosition = coordinates.positionInParent().x
                            val size = coordinates.size
                            if (cellSize == null) cellSize = size
                            if (isSelected) selectedPositionX = cellPosition
                        })
            }
        }
    }
}