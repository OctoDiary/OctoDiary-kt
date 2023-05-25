package org.bxkr.octodiary.models.shared

import androidx.annotation.StringRes
import org.bxkr.octodiary.R

@Suppress("unused")
enum class PeriodType(@StringRes val stringRes: Int) {
    HalfYear(R.string.half_year),
    Quarter(R.string.quarter),
    Semester(R.string.semester),
    Trimester(R.string.trimester),
    Module(R.string.module)
}