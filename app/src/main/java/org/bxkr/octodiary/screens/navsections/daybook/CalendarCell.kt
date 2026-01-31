package org.bxkr.octodiary.screens.navsections.daybook


import androidx.compose.material.icons.Icons
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import java.util.Calendar

@Composable
fun CalendarCell(date: Calendar, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val number = date.get(Calendar.DAY_OF_MONTH).toString()
    val shortName =
        date.getDisplayName(
            Calendar.DAY_OF_WEEK, Calendar.SHORT, configuration.locales[0]
        )!!
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


