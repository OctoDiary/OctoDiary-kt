package org.bxkr.octodiary.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SwitchPreference(
    title: String,
    listenState: MutableState<Boolean>,
    onToggled: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable {
                onToggled(!listenState.value)
            }
    ) {
        Row(
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title)
            Switch(checked = listenState.value, onCheckedChange = onToggled)
        }
    }
}