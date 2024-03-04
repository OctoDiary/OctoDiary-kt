package org.bxkr.octodiary.screens.navsections.daybook

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import org.bxkr.octodiary.formatToDay
import org.bxkr.octodiary.ui.theme.OctoDiaryTheme
import java.util.Calendar
import java.util.Collections
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun CalendarBar(onDaySelect: (Date) -> Unit = {}) {
    val date = remember { Date() }
    val calendar = remember {
        Calendar.getInstance().apply {
            time = date
        }
    }
    val daySelected = daySelectedLive.observeAsState(Date())
    LaunchedEffect(daySelected.value) {
        onDaySelect(daySelected.value)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(
            MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
            MaterialTheme.shapes.medium.copy(
                topStart = CornerSize(0.dp), topEnd = CornerSize(0.dp)
            )
        )
    ) {
        CalendarRow(date = calendar, daySelected, { daySelectedLive.postValue(it) })
        CalendarHandle(Modifier.padding(top = 24.dp))
    }
}

@Composable
fun CalendarHandle(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.secondary
    Box(modifier.padding(bottom = 16.dp)) {
        val anim = remember { androidx.compose.animation.core.Animatable(20f) }
        Surface(
            Modifier
                .size(24.dp, 4.dp)
                .offset(-10.dp)
                .rotate(anim.value), CircleShape, color
        ) {}
        Surface(
            Modifier
                .size(24.dp, 4.dp)
                .offset(10.dp)
                .rotate(-anim.value), CircleShape, color
        ) {}
    }
}

@Composable
fun CalendarRow(
    date: Calendar,
    daySelected: State<Date>,
    onDaySelect: (Date) -> Unit,
    modifier: Modifier = Modifier,
) {
    val weekdays = remember { (1..7).toList().also { Collections.rotate(it, -1) } }
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
                val cellDate = remember {
                    (date.clone() as Calendar).apply {
                        set(Calendar.DAY_OF_WEEK, weekday)
                    }
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

@Composable
fun CalendarCell(date: Calendar, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val number = remember { date.get(Calendar.DAY_OF_MONTH).toString() }
    val shortName = remember {
        date.getDisplayName(
            Calendar.DAY_OF_WEEK, Calendar.SHORT, configuration.locales[0]
        )!!
    }
    Column(modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(24.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number, style = MaterialTheme.typography.labelLarge)
        }
        Text(shortName, style = MaterialTheme.typography.labelMedium)
    }
}

@Preview
@Composable
fun CalendarBarPreview() {
    OctoDiaryTheme {
        Surface {
            CalendarBar()
        }
    }
}