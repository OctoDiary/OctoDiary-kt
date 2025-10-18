package org.bxkr.octodiary.components.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.NavSection
import org.bxkr.octodiary.R
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.save

object CommonPrefs {
    val breaks = SwitchPreferenceSpec(
        titleRes = R.string.show_breaks,
        prefKey = "breaks",
        defaultValue = true
    )
    val showLessonNumbers = SwitchPreferenceSpec(
        titleRes = R.string.show_lesson_numbers,
        prefKey = "show_lesson_numbers",
        defaultValue = true
    )
    val showOnlyPlan = SwitchPreferenceSpec(
        titleRes = R.string.show_only_plan,
        descriptionRes = R.string.show_only_plan_desc,
        prefKey = "show_only_plan",
        defaultValue = false
    )
    val mainRating = SwitchPreferenceSpec(
        titleRes = R.string.main_rating,
        prefKey = "main_rating",
        defaultValue = true
    )
    val subjectRating = SwitchPreferenceSpec(
        titleRes = R.string.subject_rating,
        prefKey = "subject_rating",
        defaultValue = true
    )
    val hideDefaultWeight = SwitchPreferenceSpec(
        titleRes = R.string.hide_default_weight,
        prefKey = "hide_default_weight",
        defaultValue = true
    )
    val markHighlighting = SwitchPreferenceSpec(
        titleRes = R.string.mark_highlighting,
        descriptionRes = R.string.mark_highlighting_desc,
        prefKey = "mark_highlighting",
        defaultValue = true
    )
}

@Composable
fun Common() {
    val context = LocalContext.current
    with(CommonPrefs) {
        Category(stringResource(R.string.diary)) {
            breaks.BasicSwitchPreference()
            showLessonNumbers.BasicSwitchPreference()
            showOnlyPlan.BasicSwitchPreference()
        }
        Category(stringResource(R.string.ratings)) {
            mainRating.BasicSwitchPreference()
            subjectRating.BasicSwitchPreference()
        }
        Category(stringResource(R.string.marks)) {
            hideDefaultWeight.BasicSwitchPreference()
            markHighlighting.BasicSwitchPreference()
        }
        // Startup screen selection
        Category(stringResource(R.string.screen_when_opening)) {
            val routes = remember { NavSection.values().toList() }
            val validRoutes = remember { routes.map { it.route }.toSet() }
            val initial = context.mainPrefs.get<String>("start_destination")
                ?.takeIf { it in validRoutes } ?: NavSection.Dashboard.route
            val selected = remember { mutableStateOf(initial) }

            Column(Modifier.fillMaxWidth()) {
                routes.forEach { section ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected.value = section.route
                                context.mainPrefs.save("start_destination" to section.route)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected.value == section.route,
                            onClick = {
                                selected.value = section.route
                                context.mainPrefs.save("start_destination" to section.route)
                            }
                        )
                        Text(
                            text = stringResource(id = section.title),
                            modifier = Modifier.padding(start = 12.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
        
        // Automation settings
        Category("Автоматизация (Droidrun)") {
            val providers = listOf(
                "disabled" to "Отключено",
                "openai" to "OpenAI (GPT-4o)",
                "anthropic" to "Anthropic (Claude 3.5)",
                "google" to "Google (Gemini 2.5)"
            )
            val currentProvider = remember {
                mutableStateOf(context.mainPrefs.get<String>("automation_provider") ?: "disabled")
            }
            
            DropdownPreference(
                title = "AI провайдер",
                currentValue = currentProvider.value,
                options = providers,
                onValueChange = {
                    currentProvider.value = it
                    context.mainPrefs.save("automation_provider" to it)
                }
            )
            
            if (currentProvider.value != "disabled") {
                // API Key
                val apiKeyState = remember {
                    mutableStateOf(context.mainPrefs.get<String>("automation_api_key") ?: "")
                }
                
                OutlinedTextField(
                    value = apiKeyState.value,
                    onValueChange = {
                        apiKeyState.value = it
                        context.mainPrefs.save("automation_api_key" to it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text("API ключ") },
                    singleLine = true
                )
                
                // Model selection
                val models = when (currentProvider.value) {
                    "openai" -> listOf(
                        "gpt-4o" to "GPT-4o (лучший баланс)",
                        "gpt-4o-mini" to "GPT-4o mini (дешевле)",
                        "gpt-4-turbo" to "GPT-4 Turbo (надежнее)"
                    )
                    "anthropic" -> listOf(
                        "claude-3-5-sonnet-20241022" to "Claude 3.5 Sonnet (точный)",
                        "claude-3-opus-20240229" to "Claude 3 Opus (высший класс)"
                    )
                    "google" -> listOf(
                        "gemini-2.0-flash-exp" to "Gemini 2.0 Flash (быстро)",
                        "gemini-exp-1206" to "Gemini Exp (экспериментальная)"
                    )
                    else -> emptyList()
                }
                
                val currentModel = remember {
                    mutableStateOf(
                        context.mainPrefs.get<String>("automation_model") 
                            ?: models.firstOrNull()?.first.orEmpty()
                    )
                }
                
                if (models.isNotEmpty()) {
                    Text(
                        text = "Модель",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    models.forEach { (id, name) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentModel.value = id
                                    context.mainPrefs.save("automation_model" to id)
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentModel.value == id,
                                onClick = {
                                    currentModel.value = id
                                    context.mainPrefs.save("automation_model" to id)
                                }
                            )
                            Text(
                                text = name,
                                modifier = Modifier.padding(start = 12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}