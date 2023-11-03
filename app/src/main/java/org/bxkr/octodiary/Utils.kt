package org.bxkr.octodiary

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.content.edit
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

abstract class Prefs(
    val prefPath: String,
    val ctx: Context
)

class AuthPrefs(ctx: Context) : Prefs("auth", ctx)
class MainPrefs(ctx: Context) : Prefs("main", ctx)

val Context.authPrefs: AuthPrefs
    get() {
        return AuthPrefs(this)
    }

val Context.mainPrefs: MainPrefs
    get() {
        return MainPrefs(this)
    }

fun getRandomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9') + '_' + '-'
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun hash(string: String): ByteArray {
    val bytes = string.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    return md.digest(bytes)
}

@OptIn(ExperimentalEncodingApi::class)
fun encodeToBase64(byteArray: ByteArray): String {
    return Base64.UrlSafe.encode(byteArray).replace("=", "")
}

fun Prefs.save(vararg addPrefs: Pair<String, Any?>) {
    ctx.getSharedPreferences(prefPath, Context.MODE_PRIVATE).edit(commit = true) {
        addPrefs.map {
            when (it.second) {
                is String -> putString(it.first, it.second as String)
                is Boolean -> putBoolean(it.first, it.second as Boolean)
                is Int -> putInt(it.first, it.second as Int)
                is Long -> putLong(it.first, it.second as Long)
                is Float -> putFloat(it.first, it.second as Float)
                null -> remove(it.first)
                else -> {}
            }
        }
    }
}

inline fun <reified T> Prefs.get(prefId: String): T? {
    ctx.getSharedPreferences(prefPath, Context.MODE_PRIVATE).run {
        return when (T::class) {
            String::class -> getString(prefId, "") as T
            Boolean::class -> getBoolean(prefId, false) as T
            Int::class -> getInt(prefId, -1) as T
            Long::class -> getLong(prefId, -1L) as T
            Float::class -> getFloat(prefId, -1F) as T
            else -> null
        }
    }
}

inline fun <reified T> Call<T>.baseEnqueue(
    noinline errorFunction: ((errorBody: ResponseBody, httpCode: Int, className: String?) -> Unit) = { _, _, _ -> },
    noinline noConnectionFunction: ((t: Throwable, className: String?) -> Unit) = { _, _ -> },
    noinline function: (body: T) -> Unit,
) = enqueue(object : Callback<T> {
    override fun onResponse(
        call: Call<T>,
        response: Response<T>
    ) {
        val body = response.body()
        if (response.isSuccessful && body != null) {
            function(body)
        } else {
            response.errorBody()
                ?.let { it1 -> errorFunction(it1, response.code(), T::class.simpleName) }
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        noConnectionFunction(t, T::class.simpleName)
    }
})

inline fun <reified T> Call<T>.extendedEnqueue(
    noinline errorFunction: ((errorBody: ResponseBody, httpCode: Int, className: String?) -> Unit) = { _, _, _ -> },
    noinline noConnectionFunction: ((t: Throwable) -> Unit) = {},
    noinline function: (response: Response<T>) -> Unit,
) = enqueue(object : Callback<T> {
    override fun onResponse(
        call: Call<T>,
        response: Response<T>
    ) {
        val body = response.body()
        if (response.isSuccessful && body != null) {
            function(response)
        } else {
            response.errorBody()
                ?.let { it1 -> errorFunction(it1, response.code(), T::class.simpleName) }
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        noConnectionFunction(t)
    }
})

fun DataService.baseErrorFunction(errorBody: ResponseBody, httpCode: Int, className: String?) {
    if (httpCode == 401) {
        tokenExpirationHandler?.invoke()
    } else println("Error in $className: ${errorBody.string()}")
}

fun DataService.baseInternalExceptionFunction(t: Throwable, className: String?) {
    println("Error in $className:\n    ${t.message}\nTrying to reload everything...")
    loadingStarted = false
    updateAll()
}

/** Formats [Date] to yyyy-MM-dd format [String] **/
fun Date.formatToDay(): String = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(this)

/** Parses yyyy-MM-dd format [String] to [Date] **/
fun String.parseFromDay(): Date = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(this)!!

/** Formats [Date] to dd LLL format [String] **/
@ReadOnlyComposable
@Composable
fun Date.formatToHumanDay(): String =
    SimpleDateFormat("dd LLL", LocalConfiguration.current.locales[0]).format(this)

/** Formats [Date] to dd MMMM format [String] **/
@ReadOnlyComposable
@Composable
fun Date.formatToLongHumanDay(): String =
    SimpleDateFormat("dd MMMM", LocalConfiguration.current.locales[0]).format(this)

/** Formats [Date] to dd.MM.yyyy format [String] **/
@ReadOnlyComposable
@Composable
fun Date.formatToHumanDate(): String =
    SimpleDateFormat("dd.MM.yyyy", LocalConfiguration.current.locales[0]).format(this)

/** Formats [Date] to EEEE format [String] **/
@ReadOnlyComposable
@Composable
fun Date.formatToWeekday(): String =
    SimpleDateFormat("EEEE", LocalConfiguration.current.locales[0]).format(this)

/** Parses [String] of [OffsetDateTime] (very long with TZ) to [Date] **/
fun String.parseLongDate(): Date =
    OffsetDateTime.parse(this).toInstant().toEpochMilli().let { Date(it) }

/** Parses [String] of long date without TZ to [Date] **/
fun String.parseSimpleLongDate(): Date =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT).parse(this)!!

/** Formats [Date] to human time [String] **/
fun Date.formatToTime(): String = SimpleDateFormat("HH:mm", Locale.ROOT).format(this)

/** Parses [String] of long date without TZ and then formats it to human date [String] **/
@ReadOnlyComposable
@Composable
fun parseSimpleLongAndFormatToLong(toFormat: String, joiner: String): String =
    SimpleDateFormat("d LLL yyyy '$joiner' H:mm", LocalConfiguration.current.locales[0]).format(
        toFormat.parseSimpleLongDate()
    )

val Date.weekOfYear: Int
    get() =
        Calendar.getInstance().run {
            time = this@weekOfYear
            get(Calendar.WEEK_OF_YEAR)
        }
