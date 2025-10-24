package org.bxkr.octodiary.components.ai

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.models.ai.StudyPlan
import org.bxkr.octodiary.models.ai.StudyTask

/**
 * Карточка персонального плана учёбы
 */
@Composable
fun StudyPlanCard(plan: StudyPlan, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Персональный план",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Schedule,
                        null,
                        Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${plan.totalEstimatedMinutes} мин",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Text(
                "Отход ко сну: ${plan.bedTime}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            
            HorizontalDivider()
            
            plan.tasks.forEach { task ->
                StudyTaskItem(task)
            }
        }
    }
}

@Composable
fun StudyTaskItem(task: StudyTask) {
    var isCompleted by remember { mutableStateOf(task.isCompleted) }
    
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { isCompleted = it }
            )
            Column {
                Text(
                    task.subjectName,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "${task.startTime} • ${task.estimatedMinutes} мин",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Priority badge
        Surface(
            shape = MaterialTheme.shapes.small,
            color = when (task.priority) {
                5 -> MaterialTheme.colorScheme.error
                4 -> MaterialTheme.colorScheme.errorContainer
                3 -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ) {
            Text(
                "P${task.priority}",
                Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
