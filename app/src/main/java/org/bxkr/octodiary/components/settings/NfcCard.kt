package org.bxkr.octodiary.components.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Nfc
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.LocalActivity
import org.bxkr.octodiary.R
import org.bxkr.octodiary.nfc.NfcCardManager

@Composable
fun NfcCard() {
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
                        isScanning = false
                    } else {
                        NfcCardManager.saveCard(context, cardData)
                        savedCard = cardData
                        lastScanProtected = false
                        lastProtectionReason = ""
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
    
    Category(stringResource(R.string.nfc_card_emulation)) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (!nfcAvailable) {
                Text(
                    text = stringResource(R.string.nfc_not_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                if (savedCard != null) {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.nfc_saved_card),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(Modifier.size(8.dp))
                            Text("UID: ${savedCard!!.uid}", style = MaterialTheme.typography.bodySmall)
                            Text("Type: ${savedCard!!.type}", style = MaterialTheme.typography.bodySmall)
                            if (savedCard!!.additionalData.isNotBlank()) {
                                Text(savedCard!!.additionalData, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    
                    OutlinedButton(
                        onClick = {
                            NfcCardManager.clearSavedCard(context)
                            savedCard = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.nfc_clear_card))
                    }
                } else if (lastScanProtected) {
                    Text(
                        text = stringResource(R.string.nfc_protected_card),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    if (lastProtectionReason.isNotBlank()) {
                        Text(
                            text = lastProtectionReason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.nfc_no_card),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                Spacer(Modifier.size(8.dp))
                
                Button(
                    onClick = { isScanning = !isScanning },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = nfcAvailable
                ) {
                    Icon(
                        if (isScanning) Icons.Rounded.Nfc else Icons.Rounded.CreditCard,
                        contentDescription = null
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        if (isScanning) stringResource(R.string.nfc_scanning)
                        else stringResource(R.string.nfc_scan_card)
                    )
                }
                
                if (isScanning) {
                    Text(
                        text = stringResource(R.string.nfc_scan_instruction),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                Spacer(Modifier.size(8.dp))
                
                Text(
                    text = stringResource(R.string.nfc_emulation_info),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
