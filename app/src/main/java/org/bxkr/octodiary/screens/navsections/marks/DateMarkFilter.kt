package org.bxkr.octodiary.screens.navsections.marks

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun DateMarkFilter(state: MutableState<DateMarkFilterType>) {
    DateMarkFilterType.values().forEach {
        DropdownMenuItem(text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(state.value == it,
                    { state.value = it })
                Text(
                    stringResource(it.title),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }, onClick = { state.value = it })
    }
}
