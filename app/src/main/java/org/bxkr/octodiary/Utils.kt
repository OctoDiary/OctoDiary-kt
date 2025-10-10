package org.bxkr.octodiary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.Matrix
import android.graphics.Typeface
import android.net.Uri
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.Layout
import android.util.Log
import android.webkit.CookieManager
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.common.component.fixed
import com.patrykandpatrick.vico.compose.common.component.rememberLayeredComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.compose.common.shape.markerCornered
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.Insets
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.copyColor
import com.patrykandpatrick.vico.core.common.shape.Corner
import okhttp3.ResponseBody
import org.bxkr.octodiary.components.MarkConfig
import org.bxkr.octodiary.components.settings.CommonPrefs
import org.bxkr.octodiary.models.marklistsubject.Mark
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

fun Context.isOnline(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
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
inline fun <reified T> decodeFromBase64JsonOrNull(
    string: String,
    charset: Charset = Charsets.UTF_8,
): T? {
    try {
        return Gson().fromJson(
            Base64.UrlSafe.decode(string).toString(charset),
            object : TypeToken<T>() {}.type
        )
    } catch (e: RuntimeException) {
        return null
    }
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

val Prefs.raw: SharedPreferences
    get() = ctx.getSharedPreferences(prefPath, Context.MODE_PRIVATE)

//inline fun <reified T> String.asGeneric(): T = when (T::class) {
//    String::class -> this as T
//    Boolean::class -> this.toBoolean() as T
//    Int::class -> this.toInt() as T
//    Long::class -> this.toLong() as T
//    Float::class -> this.toFloat() as T
//    else -> throw IllegalStateException("Unknown Generic Type")
//}

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

inline fun <reified T> Call<T>.baseEnqueueOrNull(
    noinline errorFunction: ((errorBody: ResponseBody, httpCode: Int, className: String?) -> Unit) = { _, _, _ -> },
    noinline noConnectionFunction: ((t: Throwable, className: String?) -> Unit) = { _, _ -> },
    noinline function: (body: T?) -> Unit,
) = enqueue(object : Callback<T> {
    override fun onResponse(
        call: Call<T>,
        response: Response<T>,
    ) {
        val body = response.body()
        if (response.isSuccessful) {
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
    println("Error in $className:\n    ${t.message}\nSwitching to cache fallback if available")
    loadingStarted = false
}

/** Formats [Date] to yyyy-MM-dd format [String] **/
fun Date.formatToDay(): String = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(this)

/** Parses yyyy-MM-dd format [String] to [Date] **/
fun String.parseFromDay(): Date = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(this)!!

/** Formats [Date] to d MMMM format [String] **/
@ReadOnlyComposable
@Composable
fun Date.formatToHumanDay(): String =
    SimpleDateFormat("d MMMM", LocalConfiguration.current.locales[0]).format(this)

/** Formats [Date] to dd MMMM format [String] **/
@ReadOnlyComposable
@Composable
fun Date.formatToLongHumanDay(includeYear: Boolean = true): String =
    SimpleDateFormat("dd MMMM" + if (includeYear) " yyyy" else "", LocalConfiguration.current.locales[0]).format(this)

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

/** Formats [Date] to human date [String] **/
@ReadOnlyComposable
@Composable
fun Date.formatToLongHumanDate(): String =
    SimpleDateFormat("d MMMM yyyy H:mm", LocalConfiguration.current.locales[0]).format(this)

/** Formats [Date] to human date [String] without time **/
@ReadOnlyComposable
@Composable
fun Date.formatToLongHumanDateNoTime(): String =
    SimpleDateFormat("d MMMM yyyy", LocalConfiguration.current.locales[0]).format(this)

/** Formats [Date] to human date [String] with joiner **/
@ReadOnlyComposable
@Composable
fun Date.formatToLongHumanDate(joiner: String): String =
    SimpleDateFormat("d MMMM yyyy '$joiner' H:mm", LocalConfiguration.current.locales[0]).format(
        this
    )

/** Parses [String] of long date without TZ and then formats it to human date [String] **/
@ReadOnlyComposable
@Composable
fun parseSimpleLongAndFormatToLong(toFormat: String, joiner: String): String =
    SimpleDateFormat("d MMMM yyyy '$joiner' H:mm", LocalConfiguration.current.locales[0]).format(
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
        "pin" to null,
        "demo" to null
    )
    CookieManager.getInstance().removeAllCookies(null)
    cachePrefs.clear()
    notificationPrefs.clear()
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

fun errorListenerForMessage(errorListener: (String) -> Unit): (errorBody: ResponseBody, httpCode: Int, className: String?) -> Unit {
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

val String.jwtPayload
    get() = split(".").getOrNull(1)?.let { decodeFromBase64JsonOrNull<Map<String, String>>(it) }

fun String.isJwtExpired() =
    jwtPayload?.get("exp")?.toLongOrNull()?.let { Date().time > it * 1000 }


fun <T> sumLists(list: List<List<T>?>): List<T> {
    val result = mutableListOf<T>()
    list.forEach { if (it != null) result.addAll(it) }
    return result
}

fun getWeekday(date: Date): Int = Calendar.getInstance().run {
    time = date
    get(Calendar.DAY_OF_WEEK)
}

//fun Date.isDateBetween(start: Date, end: Date): Boolean = time > start.time && time < end.time
fun Date.isDateBetween(range: List<Long>): Boolean = time > range[0] && time < range[1]

@Composable
fun rememberMarker(
    labelPosition: DefaultCartesianMarker.LabelPosition = DefaultCartesianMarker.LabelPosition.AroundPoint,
    showIndicator: Boolean = true,
): CartesianMarker {
    val labelBackgroundShape =
        com.patrykandpatrick.vico.core.common.shape.Shape.markerCornered(Corner.FullyRounded)
    val labelBackground =
        rememberShapeComponent(labelBackgroundShape, MaterialTheme.colorScheme.surfaceVariant)
            .setShadow(
                radius = 4f,
                dy = 2f,
                applyElevationOverlay = true,
            )
    val label =
        rememberTextComponent(
            color = MaterialTheme.colorScheme.onSurface,
            background = labelBackground,
            padding = Dimensions.of(8.dp, 4.dp),
            typeface = Typeface.MONOSPACE,
            textAlignment = Layout.Alignment.ALIGN_CENTER,
            minWidth = TextComponent.MinWidth.fixed(40.dp),
        )
    val indicatorFrontComponent = rememberShapeComponent(
        com.patrykandpatrick.vico.core.common.shape.Shape.Pill,
        MaterialTheme.colorScheme.surfaceVariant
    )
    val indicatorCenterComponent =
        rememberShapeComponent(com.patrykandpatrick.vico.core.common.shape.Shape.Pill)
    val indicatorRearComponent =
        rememberShapeComponent(com.patrykandpatrick.vico.core.common.shape.Shape.Pill)
    val indicator =
        rememberLayeredComponent(
            rear = indicatorRearComponent,
            front =
            rememberLayeredComponent(
                rear = indicatorCenterComponent,
                front = indicatorFrontComponent,
                padding = Dimensions.of(5.dp),
            ),
            padding = Dimensions.of(10.dp),
        )
    val guideline = rememberAxisGuidelineComponent()
    return remember(label, labelPosition, indicator, showIndicator, guideline) {
        @SuppressLint("RestrictedApi")
        object : DefaultCartesianMarker(
            label = label,
            labelPosition = labelPosition,
            indicator = if (showIndicator) indicator else null,
            indicatorSizeDp = 36f,
            setIndicatorColor =
            if (showIndicator) {
                { color ->
                    indicatorRearComponent.color = color.copyColor(alpha = .15f)
                    indicatorCenterComponent.color = color
                    indicatorCenterComponent.setShadow(radius = 12f, color = color)
                }
            } else {
                null
            },
            guideline = guideline,
        ) {
            override fun getInsets(
                context: CartesianMeasureContext,
                outInsets: Insets,
                horizontalDimensions: HorizontalDimensions,
            ) {
                with(context) {
                    outInsets.top = (1.4f * 4f - 2f).pixels
                    if (labelPosition == LabelPosition.AroundPoint) return
                    outInsets.top += label.getHeight(context) + labelBackgroundShape.tickSizeDp.pixels
                }
            }
        }
    }
}

var Context.isDemo
    get() = mainPrefs.get<Boolean>("demo") ?: false
    set(value) = mainPrefs.save("demo" to value)

inline fun <reified T> Context.getDemoProperty(@RawRes propertyRes: Int): T {
    val text =
        resources.openRawResource(propertyRes).bufferedReader(Charsets.UTF_8).use { it.readText() }
    return Gson().fromJson(text, object : TypeToken<T>() {}.type)
}

val demoScheduleDate = Date(1710190800000)

fun Context.openUri(uri: String) {
    val browserIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(uri)
    )
    ContextCompat.startActivity(this, browserIntent, null)
}

inline fun <reified T> CachePrefs.getFromJson(key: String): T {
    return this.get<String>(key).run { Gson().fromJson(this, object : TypeToken<T>() {}.type) }
}

fun convertToRoman(number: Int): String {
    val numbers = linkedMapOf<Int, String>(
        1000 to "M",
        900 to "CM",
        500 to "D",
        400 to "CD",
        100 to "C",
        90 to "XC",
        50 to "L",
        40 to "XL",
        10 to "X",
        9 to "IX",
        5 to "V",
        4 to "IV",
        1 to "I"
    )
    for (i in numbers.keys) {
        if (number >= i) {
            return numbers[i] + convertToRoman(number - i)
        }
    }
    return ""
}

@Composable
fun getMarkConfig(): MarkConfig {
    val context = LocalContext.current
    return MarkConfig(
        hideDefaultWeight = context.mainPrefs.get(CommonPrefs.hideDefaultWeight.prefKey) ?: true,
        markHighlighting = context.mainPrefs.get(CommonPrefs.markHighlighting.prefKey) ?: true
    )
}

@Composable
fun areBreaksShown(): Boolean {
    val context = LocalContext.current
    return context.mainPrefs.get(CommonPrefs.breaks.prefKey) ?: true
}

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

inline fun <reified T> String.fromJson(): T? =
    Gson().fromJson(this, object : TypeToken<T>() {}.type)

operator fun <T> Iterable<T>.times(count: Int): List<T> = List(count) { this }.flatten()

fun simpleMark(value: Int, weight: Int = 1) = Mark(
    comment = null,
    commentExists = false,
    controlFormName = "CALC",
    createdAt = null,
    criteria = null,
    date = "01-01-1970",
    id = (-100000..0).random().toLong(),
    isExam = false,
    isPoint = false,
    originalGradeSystemType = "5",
    pointDate = null,
    updatedAt = null,
    value = value.toString(),
    values = null,
    weight = weight
)

@Composable
fun TextWithIcon(icon: ImageVector, text: @Composable () -> Unit) {
    Row(Modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = "", modifier = Modifier.padding(end = 8.dp))
        text.invoke()
    }
}

@Composable
fun TextWithIcon(icon: ImageVector, text: String) {
    TextWithIcon(icon) { Text(text) }
}

inline fun <reified Original> jsonSaver() = Saver<Original, String>(
    save = { Gson().toJson(it) },
    restore = { it.fromJson() }
)
