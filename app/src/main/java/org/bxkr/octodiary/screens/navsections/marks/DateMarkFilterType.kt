package org.bxkr.octodiary.screens.navsections.marks

import androidx.annotation.StringRes
import org.bxkr.octodiary.R

enum class DateMarkFilterType(
    @StringRes val title: Int
) {
    ByUpdated(R.string.mark_filter_by_updated),
    ByLessonDate(R.string.mark_filter_by_lesson_date)
}