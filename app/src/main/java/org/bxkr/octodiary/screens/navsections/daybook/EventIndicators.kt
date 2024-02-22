package org.bxkr.octodiary.screens.navsections.daybook

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.models.events.Event

@Composable
fun EventIndicators(event: Event, modifier: Modifier = Modifier) {
    Row(modifier) {
        Indicators.values().forEach {
            if (it.condition(event)) {
                Icon(
                    it.icon,
                    stringResource(it.descriptionRes),
                    Modifier.size(16.dp),
                    MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}