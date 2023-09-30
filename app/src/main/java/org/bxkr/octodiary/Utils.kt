package org.bxkr.octodiary

import android.content.Context
import androidx.core.content.edit
import java.security.MessageDigest
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

fun getRandomString(length: Int) : String {
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