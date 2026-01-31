package org.bxkr.octodiary.screens.navsections.profile


import androidx.compose.material.icons.Icons
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@Composable
fun NameAndValueText(name: String, value: String, onClick: (() -> Unit)? = null) {
    Row {
        Text(
            name, modifier = Modifier
                .padding(end = 3.dp)
                .alpha(0.8f)
        )
        Text(value, modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    }
}


