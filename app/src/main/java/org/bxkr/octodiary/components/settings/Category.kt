package org.bxkr.octodiary.components.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun Category(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp),
            fontSize = LocalTextStyle.current.fontSize.times(0.85f),
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        )
        content()
    }
}