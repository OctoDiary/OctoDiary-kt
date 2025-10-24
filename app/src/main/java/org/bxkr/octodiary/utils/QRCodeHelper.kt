package org.bxkr.octodiary.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream
import android.util.Base64

object QRCodeHelper {
    
    data class NoteData(
        val title: String,
        val content: String,
        val subject: String,
        val date: String,
        val format: String = "octodiary_v1" // Свой формат
    )
    
    fun encodeNoteToQR(note: NoteData): String {
        val json = Gson().toJson(note)
        val compressed = compress(json)
        return Base64.encodeToString(compressed, Base64.NO_WRAP)
    }
    
    fun decodeNoteFromQR(qrData: String): NoteData? {
        return try {
            val compressed = Base64.decode(qrData, Base64.NO_WRAP)
            val json = decompress(compressed)
            Gson().fromJson(json, NoteData::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun compress(input: String): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).use { it.write(input.toByteArray()) }
        return bos.toByteArray()
    }
    
    private fun decompress(input: ByteArray): String {
        return String(java.util.zip.GZIPInputStream(input.inputStream()).readBytes())
    }
    
    // Заглушка для генерации QR-кода (требует библиотеку ZXing)
    fun generateQRCode(data: String, size: Int = 512): Bitmap {
        // TODO: Реализовать через ZXing
        return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
        }
    }
}
