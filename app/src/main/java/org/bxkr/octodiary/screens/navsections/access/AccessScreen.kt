package org.bxkr.octodiary.screens.navsections.access


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Settings
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import org.bxkr.octodiary.LocalActivity
import org.bxkr.octodiary.R
import org.bxkr.octodiary.nfc.NfcCardManager

@Composable
fun AccessScreen() {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var isScanning by remember { mutableStateOf(false) }
    var savedCard by remember { mutableStateOf(NfcCardManager.getSavedCard(context)) }
    var nfcAvailable by remember { mutableStateOf(NfcCardManager.isNfcAvailable(context)) }
    var lastScanProtected by remember { mutableStateOf(false) }
    var lastProtectionReason by remember { mutableStateOf("") }
    
    DisposableEffect(isScanning) {
        if (isScanning) {
            NfcCardManager.enableReaderMode(activity) { tag ->
                val cardData = NfcCardManager.readCard(context, tag)
                if (cardData != null) {
                    if (cardData.isProtected) {
                        lastScanProtected = true
                        lastProtectionReason = cardData.protectionReason
                        performHaptic(context, HapticType.ERROR)
                        isScanning = false
                    } else {
                        NfcCardManager.saveCard(context, cardData)
                        savedCard = cardData
                        lastScanProtected = false
                        lastProtectionReason = ""
                        performHaptic(context, HapticType.SUCCESS)
                        isScanning = false
                    }
                }
            }
        }
        
        onDispose {
            if (isScanning) {
                NfcCardManager.disableReaderMode(activity)
            }
        }
    }
    
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- NFC анимация сверху ---
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            NFCAnimView(isScanning = isScanning, hasCard = savedCard != null, isProtected = lastScanProtected)
        }
        // --- Кнопки внизу ---
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { isScanning = !isScanning },
                modifier = Modifier.weight(1f),
                enabled = nfcAvailable
            ) {
                Icon(
                    if (isScanning) Icons.Default.Nfc else Icons.Default.CreditCard,
                    contentDescription = null
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    if (isScanning) stringResource(R.string.nfc_scanning)
                    else stringResource(R.string.nfc_scan_card)
                )
            }
            OutlinedButton(
                onClick = {
                    NfcCardManager.clearSavedCard(context)
                    savedCard = null
                },
                modifier = Modifier.weight(1f),
                enabled = savedCard != null
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.nfc_clear_card))
            }
        }
        // --- Плашка с данными карты в самом низу ---
        if (!nfcAvailable) {
            Card(
                Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = stringResource(R.string.nfc_not_available),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Text(
                        text = stringResource(R.string.nfc_enable_instruction),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_NFC_SETTINGS)
                            ContextCompat.startActivity(context, intent, null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.open_nfc_settings))
                    }
                }
            }
        } else if (savedCard != null) {
            Card(
                Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("UID: ${savedCard!!.uid}", style = MaterialTheme.typography.bodyMedium)
                    Text("Тип: ${savedCard!!.type}", style = MaterialTheme.typography.bodySmall)
                    if (savedCard!!.additionalData.isNotBlank()) {
                        Text(savedCard!!.additionalData, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        text = stringResource(R.string.nfc_emulation_active),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else if (lastScanProtected) {
            Card(
                Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.nfc_protected_card),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    if (lastProtectionReason.isNotBlank()) {
                        Text(
                            text = lastProtectionReason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        } else {
            Text(
                text = stringResource(R.string.nfc_no_card_scan),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}



