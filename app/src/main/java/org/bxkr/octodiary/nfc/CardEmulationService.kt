package org.bxkr.octodiary.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import org.bxkr.octodiary.cachePrefs
import org.bxkr.octodiary.get

class CardEmulationService : HostApduService() {
    
    companion object {
        private const val TAG = "CardEmulation"
        
        // SELECT AID команда
        private val SELECT_APDU_HEADER = byteArrayOf(
            0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte()
        )
        
        // Статусы ответа
        private val SUCCESS = byteArrayOf(0x90.toByte(), 0x00.toByte())
        private val UNKNOWN = byteArrayOf(0x6F.toByte(), 0x00.toByte())
    }
    
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) {
            return UNKNOWN
        }
        
        Log.d(TAG, "Received APDU: ${commandApdu.toHexString()}")
        
        // Проверяем, что это SELECT команда
        if (commandApdu.size >= 4 &&
            commandApdu[0] == SELECT_APDU_HEADER[0] &&
            commandApdu[1] == SELECT_APDU_HEADER[1] &&
            commandApdu[2] == SELECT_APDU_HEADER[2] &&
            commandApdu[3] == SELECT_APDU_HEADER[3]
        ) {
            Log.d(TAG, "SELECT command received")
            
            // Загружаем сохранённый UID карты
            val savedUid = cachePrefs.get<String>("nfc_card_uid")
            if (savedUid != null) {
                Log.d(TAG, "Emulating card with UID: $savedUid")
                // Возвращаем UID + SUCCESS
                return savedUid.hexToByteArray() + SUCCESS
            }
        }
        
        // Для всех остальных команд возвращаем сохранённые данные или SUCCESS
        val savedResponse = cachePrefs.get<String>("nfc_card_response")
        return if (savedResponse != null) {
            Log.d(TAG, "Returning saved response: $savedResponse")
            savedResponse.hexToByteArray()
        } else {
            SUCCESS
        }
    }
    
    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Deactivated: $reason")
    }
    
    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }
    
    private fun String.hexToByteArray(): ByteArray {
        return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
