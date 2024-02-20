package org.bxkr.octodiary.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.R
import org.bxkr.octodiary.network.NetworkService

@Composable
fun ErrorMessage(modifier: Modifier = Modifier, errorText: String) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.Warning,
            stringResource(R.string.error_occurred),
            Modifier.size(64.dp),
            MaterialTheme.colorScheme.secondary
        )
        Text(
            stringResource(R.string.error_occurred),
            style = MaterialTheme.typography.titleLarge
        )
        val clipboardManager = LocalClipboardManager.current
        Text(errorText,
            Modifier
                .padding(horizontal = 16.dp)
                .clickable {
                    clipboardManager.setText(
                        AnnotatedString(errorText)
                    )
                }, textAlign = TextAlign.Center)
        val uriHandler = LocalUriHandler.current
        OutlinedButton(
            onClick = { uriHandler.openUri(NetworkService.ExternalIntegrationConfig.TELEGRAM_REPORT_URL) },
            modifier = Modifier.padding(16.dp),
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        ) {
            Icon(
                Icons.Rounded.BugReport,
                stringResource(id = R.string.report_issue),
                Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.report_issue))
        }
    }
}