package org.bxkr.octodiary

import android.icu.text.MessageFormat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bxkr.octodiary.ui.activities.MainActivity
import java.io.InputStream
import java.util.Locale

object Utils {
    fun isSchoolDataOutOfDate(mainActivity: MainActivity): Boolean {
        return !(mainActivity.userData != null &&
                mainActivity.diaryData != null &&
                mainActivity.ratingData != null)
    }

    fun toOrdinal(place: Int): String {
        val formatter = MessageFormat("{0,ordinal}", Locale.getDefault())
        return formatter.format(arrayOf(place)).replace(".", "")
    }

    fun <T> getJsonRaw(rawResource: InputStream): T {
        val byteArray = ByteArray(rawResource.available())
        rawResource.read(byteArray)
        return Gson().fromJson(
            String(byteArray),
            object : TypeToken<T>() {}.type
        )
    }
}