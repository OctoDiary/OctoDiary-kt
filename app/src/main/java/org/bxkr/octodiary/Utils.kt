package org.bxkr.octodiary

import android.content.Context
import android.icu.text.MessageFormat
import androidx.annotation.StringRes
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bxkr.octodiary.ui.activities.MainActivity
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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

    inline fun <reified T> getJsonRaw(rawResource: InputStream): T {
        val byteArray = ByteArray(rawResource.available())
        rawResource.read(byteArray)
        return Gson().fromJson(String(byteArray), object : TypeToken<T>() {}.type)
    }

    fun isDemo(context: Context): Boolean {
        val prefs =
            context.getSharedPreferences(
                context.getString(R.string.auth_file_key),
                Context.MODE_PRIVATE
            )

        return (prefs.getString(
            context.getString(R.string.token),
            null
        ) == context.getString(R.string.demo_token) &&
                prefs.getString(
                    context.getString(R.string.user_id),
                    null
                ) == context.getString(R.string.demo_user_id))
    }

    inline fun <reified T> agedData(context: Context, @StringRes dataKey: Int): T? {
        val dataAge = context.getSharedPreferences(
            context.getString(R.string.saved_data_key),
            Context.MODE_PRIVATE
        ).getLong(context.getString(R.string.data_age_key), (-1).toLong())

        val dataAgeCalendar = Calendar.getInstance()
        dataAgeCalendar.timeInMillis = dataAge
        if (dataAge == (-1).toLong() || ((System.currentTimeMillis() - dataAge) >= 3600000) || ((System.currentTimeMillis() - dataAge) < 0) ||
            Calendar.getInstance()
                .get(Calendar.DAY_OF_YEAR) != dataAgeCalendar.get(Calendar.DAY_OF_YEAR)
        ) {
            return null
        }

        val jsonEncoded = context.getSharedPreferences(
            context.getString(R.string.saved_data_key),
            Context.MODE_PRIVATE
        ).getString(context.getString(dataKey), null)
        if (jsonEncoded != null) {
            return Gson().fromJson<T>(
                jsonEncoded,
                object : TypeToken<T>() {}.type
            )
        }
        return null
    }

    fun <T> agedData(context: Context, @StringRes dataKey: Int, value: T) {
        val jsonEncoded = Gson().toJson(value)
        context.getSharedPreferences(
            context.getString(R.string.saved_data_key),
            Context.MODE_PRIVATE
        ).edit {
            putString(context.getString(dataKey), jsonEncoded)
            putLong(context.getString(R.string.data_age_key), System.currentTimeMillis())
        }
    }

    fun toPatternedDate(pattern: String, date: Date, locale: Locale): String =
        SimpleDateFormat(pattern, locale).format(date)
}