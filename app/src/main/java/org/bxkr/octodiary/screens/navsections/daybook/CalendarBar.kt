package org.bxkr.octodiary.screens.navsections.daybook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.components.settings.CommonPrefs
import org.bxkr.octodiary.demoScheduleDate
import org.bxkr.octodiary.get
import org.bxkr.octodiary.isDemo
import org.bxkr.octodiary.mainPrefs
import java.util.Calendar
import java.util.Date

@Composable
fun CalendarBar() {
    val isDemo = LocalContext.current.isDemo
    val daySelected = daySelectedLive.observeAsState(if (!isDemo) Date() else demoScheduleDate)
    val weekStartsAlwaysMonday =
        LocalContext.current.mainPrefs.get(CommonPrefs.weekStartsAlwaysMonday.prefKey) ?: false

    var calendar by remember {
        mutableStateOf(
            Calendar.getInstance().apply {
                time = daySelected.value
            }
        )
    }
    LaunchedEffect(daySelected.value) {
        val day = daySelected.value
        calendar = Calendar.getInstance().apply { time = day }
        if (weekStartsAlwaysMonday)
            calendar.setFirstDayOfWeek(Calendar.MONDAY)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(
            MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
            MaterialTheme.shapes.medium.copy(
                topStart = CornerSize(0.dp), topEnd = CornerSize(0.dp)
            )
        )
    ) {
        CalendarRow(
            date = calendar,
            daySelected,
            { daySelectedLive.postValue(it) },
            Modifier
                .padding(bottom = 16.dp)
        )
    }
}