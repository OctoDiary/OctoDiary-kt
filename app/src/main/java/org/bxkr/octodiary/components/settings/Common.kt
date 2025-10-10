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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.ai.OpenAiLikeProvider
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

        // AI autosolve provider and API key
            val providers = listOf(
                "disabled" to stringResource(R.string.provider_disabled),
                "openai" to stringResource(R.string.provider_openai),
                "gemini" to stringResource(R.string.provider_gemini),
                "openrouter" to stringResource(R.string.provider_openrouter),
                "custom" to stringResource(R.string.provider_custom),
            )
            val currentProvider = remember {
                mutableStateOf(context.mainPrefs.get<String>("ai_provider") ?: "disabled")
            }
            Column(Modifier.fillMaxWidth()) {
                DropdownPreference(
                    title = stringResource(R.string.ai_provider),
                    currentValue = currentProvider.value,
                    options = providers,
                    onValueChange = {
                        currentProvider.value = it
                        context.mainPrefs.save("ai_provider" to it)
                        if (it != "disabled") {
                            val firstModel = OpenAiLikeProvider().defaultModels().firstOrNull()?.first
                            if (firstModel != null) context.mainPrefs.save("ai_model" to firstModel)
                        }
                    }
                )

                if (currentProvider.value != "disabled") {
                    val keyState = remember {
                        mutableStateOf(context.mainPrefs.get<String>("ai_api_key") ?: "")
                    }
                    val testResult = remember { mutableStateOf<String?>(null) }
                    val isTesting = remember { mutableStateOf(false) }
                    val scope = rememberCoroutineScope()
                    
                    OutlinedTextField(
                        value = keyState.value,
                        onValueChange = {
                            keyState.value = it
                            context.mainPrefs.save("ai_api_key" to it)
                            testResult.value = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        label = { Text(stringResource(R.string.ai_api_key)) },
                        singleLine = true
                    )
                    
                    androidx.compose.material3.Button(
                        onClick = {
                            isTesting.value = true
                            testResult.value = null
                            scope.launch {
                                val (success, message) = org.bxkr.octodiary.ai.OpenAiClient.testApiKey(context)
                                testResult.value = if (success) "✓ $message" else "✗ $message"
                                isTesting.value = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        enabled = !isTesting.value && keyState.value.isNotBlank()
                    ) {
                        Text(if (isTesting.value) "Проверка..." else "Проверить API-ключ")
                    }
                    
                    if (testResult.value != null) {
                        Text(
                            text = testResult.value!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (testResult.value!!.startsWith("✓")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    // Model selection
                    val models = OpenAiLikeProvider().defaultModels() + listOf("custom" to "Custom model")
                    val currentModel = remember {
                        mutableStateOf(context.mainPrefs.get<String>("ai_model") ?: models.firstOrNull()?.first.orEmpty())
                    }
                    Text(
                        text = stringResource(R.string.ai_model),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    models.forEach { (id, desc) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentModel.value = id
                                    context.mainPrefs.save("ai_model" to id)
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentModel.value == id,
                                onClick = {
                                    currentModel.value = id
                                    context.mainPrefs.save("ai_model" to id)
                                }
                            )
                            Column(Modifier.padding(start = 12.dp)) {
                                Text(id, style = MaterialTheme.typography.bodyLarge)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                    
                    // Custom model input
                    if (currentModel.value == "custom") {
                        val customModel = remember { mutableStateOf(context.mainPrefs.get<String>("ai_custom_model") ?: "") }
                        OutlinedTextField(
                            value = customModel.value,
                            onValueChange = {
                                customModel.value = it
                                context.mainPrefs.save("ai_custom_model" to it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            label = { Text(stringResource(R.string.ai_custom_model)) },
                            placeholder = { Text(stringResource(R.string.ai_custom_model_hint)) },
                            singleLine = true
                        )
                    }

                    // Custom base URL (only for "custom" provider)
                    if (currentProvider.value == "custom") {
                        val baseUrl = remember { mutableStateOf(context.mainPrefs.get<String>("ai_base_url") ?: "") }
                        OutlinedTextField(
                            value = baseUrl.value,
                            onValueChange = {
                                baseUrl.value = it
                                context.mainPrefs.save("ai_base_url" to it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            label = { Text(stringResource(R.string.ai_base_url)) },
                            singleLine = true,
                            placeholder = { Text("https://api.openai.com/v1") }
                        )
                    }
                }
            }
    }
}