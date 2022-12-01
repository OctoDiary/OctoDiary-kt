package org.bxkr.octodiary

import android.icu.text.MessageFormat
import org.bxkr.octodiary.ui.activities.MainActivity
import java.util.Locale

object Utils {
    fun isSchoolDataOutOfDate(mainActivity: MainActivity): Boolean {
        return !(mainActivity.userData != null &&
                mainActivity.diaryData != null &&
                mainActivity.ratingData != null)
    }

    fun toOrdinal(place: Int): String {
        val formatter = MessageFormat("{0,ordinal}", Locale.getDefault())
        return formatter.format(arrayOf(place))
    }
}