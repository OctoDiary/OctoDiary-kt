package org.bxkr.octodiary.components.settings


import androidx.compose.material.icons.Icons
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
import org.bxkr.octodiary.NavSection
import org.bxkr.octodiary.R
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.save

object CommonPrefs {
    val autoUpdateEnabled = SwitchPreferenceSpec(
        titleRes = R.string.auto_update_enabled,
        prefKey = "auto_update_enabled",
        defaultValue = true
    )
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
        Category(stringResource(R.string.general)) {
            autoUpdateEnabled.BasicSwitchPreference()
        }
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
    }
}


