package org.bxkr.octodiary.screens


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeCreatorScreen() {
    var primaryColor by remember { mutableStateOf(Color(0xFF6200EE)) }
    var secondaryColor by remember { mutableStateOf(Color(0xFF03DAC5)) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создатель тем") },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Share, "Поделиться")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Save, "Сохранить")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Основной цвет", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(primaryColor, MaterialTheme.shapes.medium)
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text("Вторичный цвет", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(secondaryColor, MaterialTheme.shapes.medium)
            )
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Palette, null)
                Spacer(Modifier.width(8.dp))
                Text("Применить тему")
            }
        }
    }
}



