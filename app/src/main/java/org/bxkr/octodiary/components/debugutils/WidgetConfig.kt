package org.bxkr.octodiary.components.debugutils


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.gson.Gson
import org.bxkr.octodiary.MainActivity
import org.bxkr.octodiary.R
import org.bxkr.octodiary.cachePrefs
import org.bxkr.octodiary.getDemoProperty
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.models.events.Event
import org.bxkr.octodiary.save
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivity.WidgetConfig(clear: () -> Unit) {
    Dialog(
        onDismissRequest = { clear() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface {
                        Text(
                            "Widget config",
                            Modifier.padding(16.dp),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    Surface(onClick = { clear() }) {
                        Icon(Icons.Default.Close, "close", Modifier.padding(16.dp))
                    }
                }
                var textFieldValue by remember { mutableStateOf("") }
                var showPicker by remember { mutableStateOf(false) }
                var date by remember { mutableStateOf(Date()) }
                val setNewDate = { date1: Date ->
                    mainPrefs.save("widget_date" to date1.time)
                }
                TextField(date.toString(),
                    onValueChange = {
                        textFieldValue = it
                    },
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { showPicker = true }) {
                            Icon(Icons.Default.CalendarMonth, "go")
                        }
                    },
                    supportingText = { Text("Click on widget to update it with new date") },
                    label = { Text(text = "Date using") }
                )
                Row(
                    Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    var buttonEnabled by remember { mutableStateOf(true) }
                    Button({ setDemo { buttonEnabled = false } }, enabled = buttonEnabled) {
                        Text(if (buttonEnabled) "Clone demo events to cache" else "Cloned!")
                    }
                }
                val datePickerState = rememberDatePickerState()
                val timePickerState = rememberTimePickerState()
                AnimatedVisibility(showPicker) {
                    DatePickerDialog(onDismissRequest = { showPicker = false }, confirmButton = {
                        TextButton(onClick = {
                            val selectedDate = datePickerState.selectedDateMillis
                            if (selectedDate != null) {
                                date = Calendar.getInstance().apply {
                                    time = Date().apply { time = selectedDate }
                                    set(Calendar.HOUR, timePickerState.hour)
                                    set(Calendar.MINUTE, timePickerState.minute)
                                }.time
                                setNewDate(date)
                                showPicker = false
                            }
                        }) {
                            Text(text = stringResource(id = R.string.select))
                        }
                    }) {
                        var isClockMode by remember { mutableStateOf(false) }
                        val timePicker = @Composable { timePickerState1: TimePickerState ->
                            Column(
                                Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Select time",
                                    Modifier
                                        .padding(16.dp)
                                        .padding(start = 8.dp)
                                        .fillMaxWidth(),
                                    style = MaterialTheme.typography.labelLarge
                                )
                                TimePicker(timePickerState1, Modifier.padding(top = 24.dp))
                            }
                        }
                        Box(contentAlignment = Alignment.Center) {
                            IconButton(
                                { isClockMode = !isClockMode },
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    if (isClockMode) Icons.Default.CalendarMonth else Icons.Default.AccessTime,
                                    "Go to other mode"
                                )
                            }
                            AnimatedContent(
                                isClockMode,
                                label = "date_clock_anim"
                            ) { showClock ->
                                if (!showClock) DatePicker(datePickerState) else timePicker(
                                    timePickerState
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun MainActivity.setDemo(onSet: () -> Unit) {
    val events = getDemoProperty<List<Event>>(R.raw.demo_event_calendar)
    cachePrefs.save("eventCalendar" to Gson().toJson(events))
    onSet()
}


