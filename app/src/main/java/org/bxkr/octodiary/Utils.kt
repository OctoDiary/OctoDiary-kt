package org.bxkr.octodiary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.Matrix
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import org.bxkr.octodiary.models.rankingforsubject.ErrorBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.nio.charset.Charset
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.system.exitProcess

abstract class Prefs(
    val prefPath: String,
    val ctx: Context,
)

class AuthPrefs(ctx: Context) : Prefs("auth", ctx)
class MainPrefs(ctx: Context) : Prefs("main", ctx)
class NotificationPrefs(ctx: Context) : Prefs("notification", ctx)
class CachePrefs(ctx: Context) : Prefs("cache", ctx)

val Context.authPrefs: AuthPrefs
    get() {
        return AuthPrefs(this)
    }

val Context.mainPrefs: MainPrefs
    get() {
        return MainPrefs(this)
    }

val Context.notificationPrefs: NotificationPrefs
    get() {
        return NotificationPrefs(this)
    }

val Context.cachePrefs: CachePrefs
    get() {
        return CachePrefs(this)
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

@OptIn(ExperimentalEncodingApi::class)
inline fun <reified T> decodeFromBase64Json(string: String, charset: Charset = Charsets.UTF_8): T {
    return Gson().fromJson(
        Base64.UrlSafe.decode(string).toString(charset),
        object : TypeToken<T>() {}.type
    )
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
        if (!contains(prefId)) return null
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

fun Prefs.clear() {
    ctx.getSharedPreferences(prefPath, Context.MODE_PRIVATE).edit(commit = true) {
        clear()
    }
}

inline fun <reified T> Call<T>.baseEnqueue(
    noinline errorFunction: ((errorBody: ResponseBody, httpCode: Int, className: String?) -> Unit) = { _, _, _ -> },
    noinline noConnectionFunction: ((t: Throwable, className: String?) -> Unit) = { _, _ -> },
    noinline function: (body: T) -> Unit,
) = enqueue(object : Callback<T> {
    override fun onResponse(
        call: Call<T>,
        response: Response<T>,
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
        response: Response<T>,
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
    if (httpCode in listOf(401, 403)) {
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

/** Formats [Date] to d LLL format [String] **/
@ReadOnlyComposable
@Composable
fun Date.formatToHumanDay(): String =
    SimpleDateFormat("d LLL", LocalConfiguration.current.locales[0]).format(this)

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

/** Formats [Date] to EEEE format [String] (takes context from composition) **/
@ReadOnlyComposable
@Composable
fun Date.formatToWeekday(): String =
    SimpleDateFormat("EEEE", LocalConfiguration.current.locales[0]).format(this)

/** Formats [Date] to EEEE format [String] (takes context as an argument) **/
fun Date.formatToWeekday(ctx: Context): String =
    SimpleDateFormat("EEEE", ctx.resources.configuration.locales[0]).format(this)

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

fun Activity.logOut(reason: String? = null) {
    if (reason != null) {
        Log.i("LogOuter", "Logged out for reason:\n$reason")
    } else {
        Log.i("LogOuter", "Logged out for an unknown reason")
    }
    authPrefs.save(
        "auth" to false,
        "access_token" to null,
        "client_id" to null,
        "client_secret" to null,
        "refresh_token" to null
    )
    mainPrefs.save(
        "first_launch" to true,
        "has_pin" to false,
        "pin" to null
    )
    cachePrefs.clear()
    screenLive.value = Screen.Login
    startActivity(Intent(this, MainActivity::class.java))
    exitProcess(0)
}

fun PackageManager.isPackageInstalled(packageName: String): Boolean {
    return try {
        getPackageInfo(packageName, 0)
        true
    } catch (e: NameNotFoundException) {
        false
    }
}

val CloverShape: Shape = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val baseWidth = 200f
        val baseHeight = 200f

        val path = Path().apply {
            moveTo(12f, 100f)
            cubicTo(12f, 76f, 0f, 77.6142f, 0f, 50f)
            cubicTo(0f, 22.3858f, 22.3858f, 0f, 50f, 0f)
            cubicTo(77.6142f, 0f, 76f, 12f, 100f, 12f)
            cubicTo(124f, 12f, 122.3858f, 0f, 150f, 0f)
            cubicTo(177.6142f, 0f, 200f, 22.3858f, 200f, 50f)
            cubicTo(200f, 77.6142f, 188f, 76f, 188f, 100f)
            cubicTo(188f, 124f, 200f, 122.3858f, 200f, 150f)
            cubicTo(200f, 177.6142f, 177.6142f, 200f, 150f, 200f)
            cubicTo(122.3858f, 200f, 124f, 188f, 100f, 188f)
            cubicTo(76f, 188f, 77.6142f, 200f, 50f, 200f)
            cubicTo(22.3858f, 200f, 0f, 177.6142f, 0f, 150f)
            cubicTo(0f, 122.3858f, 12f, 124f, 12f, 100f)
            close()
        }

        return Outline.Generic(
            path
                .asAndroidPath()
                .apply {
                    transform(Matrix().apply {
                        setScale(size.width / baseWidth, size.height / baseHeight)
                    })
                }
                .asComposePath()
        )
    }
}

fun Calendar.getRussianWeekdayOnFormat(): String =
    when (get(Calendar.DAY_OF_WEEK)) {
        Calendar.TUESDAY -> "во вторник"
        Calendar.WEDNESDAY -> "в среду"
        Calendar.FRIDAY -> "в пятницу"
        Calendar.SATURDAY -> "в субботу"
        else -> "в ${getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale("ru"))}"
    }

fun DataService.errorListenerForMessage(errorListener: (String) -> Unit): (errorBody: ResponseBody, httpCode: Int, className: String?) -> Unit {
    return { errorBody: ResponseBody, httpCode: Int, className: String? ->
        val contents = errorBody.string()
        try {
            val body = Gson().fromJson(contents, ErrorBody::class.java)
            errorListener(body.message)
        } catch (exception: Throwable) {
            errorListener(contents)
        }
    }
}

fun String.isJwtExpired() =
    split(".")[1].let { decodeFromBase64Json<Map<String, String>>(it) }.get("exp")
        ?.toIntOrNull()?.let { Date().time > it }
