package org.bxkr.octodiary.components.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.bxkr.octodiary.R

@Composable
fun Common() {
    Category(stringResource(R.string.diary)) {
        BasicSwitchPreference(
            titleRes = R.string.show_lesson_numbers,
            prefKey = "show_lesson_numbers",
            defaultValue = true
        )
        BasicSwitchPreference(
            titleRes = R.string.show_only_plan,
            descriptionRes = R.string.show_only_plan_desc,
            prefKey = "show_only_plan",
            defaultValue = false
        )
    }
    Category(stringResource(R.string.ratings)) {
        BasicSwitchPreference(
            titleRes = R.string.main_rating,
            prefKey = "main_rating",
            defaultValue = true
        )
        BasicSwitchPreference(
            titleRes = R.string.subject_rating,
            prefKey = "subject_rating",
            defaultValue = true
        )
    }
}