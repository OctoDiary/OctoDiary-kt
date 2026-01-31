package org.bxkr.octodiary.components.settings


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.LocalActivity
import org.bxkr.octodiary.components.SwitchPreference
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.save

@Composable
fun AccessibilitySettings() {
    val activity = LocalActivity.current
    
    // Режим дислексии
    val dyslexiaMode = remember { 
        mutableStateOf(activity.mainPrefs.get<Boolean>("dyslexia_mode") ?: false) 
    }
    
    // Масштаб текста
    val textScale = remember { 
        mutableFloatStateOf(activity.mainPrefs.get<Float>("text_scale") ?: 1.0f) 
    }
    
    // Жирность текста
    val boldText = remember { 
        mutableStateOf(activity.mainPrefs.get<Boolean>("bold_text") ?: false) 
    }
    
    // Высокая контрастность
    val highContrast = remember { 
        mutableStateOf(activity.mainPrefs.get<Boolean>("high_contrast") ?: false) 
    }
    
    // Упрощенная навигация
    val simplifiedNav = remember { 
        mutableStateOf(activity.mainPrefs.get<Boolean>("simplified_nav") ?: false) 
    }
    
    // Увеличенные кнопки
    val largeButtons = remember { 
        mutableStateOf(activity.mainPrefs.get<Boolean>("large_buttons") ?: false) 
    }
    
    Column(Modifier.padding(vertical = 8.dp)) {
        
        // Заголовок секции
        Text(
            "Специальные возможности",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Режим дислексии
        SwitchPreference(
            title = "Режим для дислексии",
            description = "Увеличенный интервал между буквами, жирный текст",
            listenState = dyslexiaMode
        ) {
            dyslexiaMode.value = it
            activity.mainPrefs.save("dyslexia_mode" to it)
            
            // Автоматически включаем жирный текст и увеличиваем масштаб
            if (it) {
                boldText.value = true
                activity.mainPrefs.save("bold_text" to true)
                if (textScale.floatValue < 1.1f) {
                    textScale.floatValue = 1.15f
                    activity.mainPrefs.save("text_scale" to 1.15f)
                    val config = activity.resources.configuration
                    config.fontScale = 1.15f
                    activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
                }
            }
        }
        
        Divider(Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        
        // Масштаб текста
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Масштаб текста",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "${(textScale.floatValue * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Slider(
                value = textScale.floatValue,
                onValueChange = { textScale.floatValue = it },
                onValueChangeFinished = {
                    activity.mainPrefs.save("text_scale" to textScale.floatValue)
                    
                    // Применяем масштаб к configuration
                    val config = activity.resources.configuration
                    config.fontScale = textScale.floatValue
                    activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
                },
                valueRange = 0.8f..1.5f,
                steps = 13,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Text(
                "От 80% до 150%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Divider(Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        
        // Жирный текст
        SwitchPreference(
            title = "Жирный текст",
            description = "Увеличенная толщина шрифта для лучшей читаемости",
            listenState = boldText
        ) {
            boldText.value = it
            activity.mainPrefs.save("bold_text" to it)
        }
        
        // Высокая контрастность
        SwitchPreference(
            title = "Высокая контрастность",
            description = "Усиленный контраст цветов",
            listenState = highContrast
        ) {
            highContrast.value = it
            activity.mainPrefs.save("high_contrast" to it)
        }
        
        // Упрощенная навигация
        SwitchPreference(
            title = "Упрощенная навигация",
            description = "Крупные элементы управления, меньше деталей",
            listenState = simplifiedNav
        ) {
            simplifiedNav.value = it
            activity.mainPrefs.save("simplified_nav" to it)
        }
        
        // Увеличенные кнопки
        SwitchPreference(
            title = "Увеличенные кнопки",
            description = "Большие области нажатия для удобства",
            listenState = largeButtons
        ) {
            largeButtons.value = it
            activity.mainPrefs.save("large_buttons" to it)
        }
        
        Divider(Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        
        // Информация
        Card(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        "О специальных возможностях",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Эти настройки помогают сделать приложение удобнее для людей с особенностями зрения, дислексией и другими потребностями. Все изменения применяются сразу после перезапуска приложения.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}



