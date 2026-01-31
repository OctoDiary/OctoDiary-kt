package org.bxkr.octodiary.nfc


import androidx.compose.material.icons.Icons
import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.NfcA
import android.util.Log
import org.bxkr.octodiary.cachePrefs
import org.bxkr.octodiary.get
import org.bxkr.octodiary.save

object NfcCardManager {
    private const val TAG = "NfcCardManager"
    
    fun isNfcAvailable(context: Context): Boolean {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        return nfcAdapter != null && nfcAdapter.isEnabled
    }
    
    fun enableReaderMode(activity: Activity, callback: (Tag) -> Unit) {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(activity) ?: return
        
        nfcAdapter.enableReaderMode(
            activity,
            { tag -> callback(tag) },
            NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V or
            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            null
        )
    }
    
    fun disableReaderMode(activity: Activity) {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(activity) ?: return
        nfcAdapter.disableReaderMode(activity)
    }
    
    fun readCard(context: Context, tag: Tag): CardData? {
        try {
            val uid = tag.id.toHexString()
            Log.d(TAG, "Card UID: $uid")
            
            val techList = tag.techList
            Log.d(TAG, "Available technologies: ${techList.joinToString()}")
            
            var cardType = "Unknown"
            var additionalData = ""
            var isProtected = false
            var protectionReason = ""
            
            if (techList.contains(NfcA::class.java.name)) {
                val nfcA = NfcA.get(tag)
                nfcA?.connect()
                val atqa = nfcA?.atqa?.toHexString() ?: ""
                val sak = nfcA?.sak?.toString(16) ?: ""
                nfcA?.close()
                cardType = "NFC-A"
                additionalData = "ATQA: $atqa, SAK: $sak"
                Log.d(TAG, "NfcA - $additionalData")
            }
            
            if (techList.contains(MifareClassic::class.java.name)) {
                val mifare = MifareClassic.get(tag)
                mifare?.connect()
                val type = when (mifare?.type) {
                    MifareClassic.TYPE_CLASSIC -> "Mifare Classic"
                    MifareClassic.TYPE_PLUS -> "Mifare Plus"
                    MifareClassic.TYPE_PRO -> "Mifare Pro"
                    else -> "Mifare Unknown"
                }
                cardType = type
                // Проверяем доступ к секторам стандартным ключом
                var canReadAny = false
                for (i in 0 until mifare!!.sectorCount) {
                    if (mifare.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT)) {
                        canReadAny = true
                        break
                    }
                }
                if (!canReadAny) {
                    isProtected = true
                    protectionReason = "Mifare Classic: все сектора защищены стандартным ключом. Карта криптована или нестандартная."
                    Log.w(TAG, protectionReason)
                }
                mifare.close()
                Log.d(TAG, "Mifare type: $type, protected: $isProtected")
            }
            
            if (techList.contains(IsoDep::class.java.name)) {
                val isoDep = IsoDep.get(tag)
                isoDep?.connect()
                val historicalBytes = isoDep?.historicalBytes?.toHexString() ?: ""
                isoDep?.close()
                if (cardType == "Unknown") cardType = "ISO-DEP"
                additionalData += " Historical: $historicalBytes"
                Log.d(TAG, "IsoDep - Historical bytes: $historicalBytes")
            }
            
            return CardData(
                uid = uid,
                type = cardType,
                techList = techList.toList(),
                additionalData = additionalData,
                isProtected = isProtected,
                protectionReason = protectionReason
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error reading card", e)
            return null
        }
    }
    
    fun saveCard(context: Context, cardData: CardData) {
        context.cachePrefs.save(
            "nfc_card_uid" to cardData.uid,
            "nfc_card_type" to cardData.type,
            "nfc_card_tech" to cardData.techList.joinToString(","),
            "nfc_card_data" to cardData.additionalData,
            "nfc_card_saved_at" to System.currentTimeMillis()
        )
        Log.d(TAG, "Card saved: ${cardData.uid}")
    }
    
    fun getSavedCard(context: Context): CardData? {
        val uid = context.cachePrefs.get<String>("nfc_card_uid") ?: return null
        val type = context.cachePrefs.get<String>("nfc_card_type") ?: "Unknown"
        val tech = context.cachePrefs.get<String>("nfc_card_tech")?.split(",") ?: emptyList()
        val data = context.cachePrefs.get<String>("nfc_card_data") ?: ""
        
        return CardData(uid, type, tech, data)
    }
    
    fun clearSavedCard(context: Context) {
        context.cachePrefs.save(
            "nfc_card_uid" to null,
            "nfc_card_type" to null,
            "nfc_card_tech" to null,
            "nfc_card_data" to null,
            "nfc_card_saved_at" to null
        )
        Log.d(TAG, "Card data cleared")
    }
    
    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }
}

data class CardData(
    val uid: String,
    val type: String,
    val techList: List<String>,
    val additionalData: String,
    val isProtected: Boolean = false,
    val protectionReason: String = ""
)



