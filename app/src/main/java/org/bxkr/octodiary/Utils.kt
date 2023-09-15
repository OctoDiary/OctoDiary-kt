package org.bxkr.octodiary

import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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