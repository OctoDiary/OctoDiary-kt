package org.bxkr.octodiary.components.debugutils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.MainActivity

@Composable
fun ExtendedDebugMenu(context: MainActivity, clearFn: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Расширенное меню отладки", modifier = Modifier.padding(bottom = 16.dp))

        Button(onClick = { /* TODO: Implement a debug action */ }) {
            Text("Тестовое действие 1")
        }

        Button(onClick = { /* TODO: Implement another debug action */ }) {
            Text("Тестовое действие 2")
        }

        // Кнопка для закрытия меню
        Button(
            onClick = clearFn,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Закрыть")
        }
    }
}
