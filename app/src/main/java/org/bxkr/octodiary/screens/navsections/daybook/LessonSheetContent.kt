package org.bxkr.octodiary.screens.navsections.daybook

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.ErrorMessage
import org.bxkr.octodiary.components.MarkComp
import org.bxkr.octodiary.components.WebViewDialog
import org.bxkr.octodiary.formatToHumanDay
import org.bxkr.octodiary.models.lessonschedule.LessonSchedule
import org.bxkr.octodiary.parseFromDay

@Composable
fun LessonSheetContent(lessonId: Long) {
    var lessonInfo by remember { mutableStateOf<LessonSchedule?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var openWebView by remember { mutableStateOf(false) }
    var webViewUrl by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        DataService.getLessonInfo(lessonId, { errorText = it }) {
            lessonInfo = it
        }
    }

    if (openWebView) {
        WebViewDialog(url = webViewUrl) {
            openWebView = false
        }
    }

    Box(
        Modifier
            .heightIn(192.dp, Int.MAX_VALUE.dp)
            .fillMaxWidth()
    ) {
        if (lessonInfo != null) {
            Column(Modifier.padding(16.dp)) {
                with(lessonInfo!!) {
                    Text(
                        subjectName,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(teacher.run { "$lastName $firstName $middleName" })
                    Row {
                        Text(
                            stringResource(R.string.lesson_location),
                            Modifier
                                .padding(end = 3.dp)
                                .alpha(0.8f)
                        )
                        Text(roomNumber)
                    }
                    Text(
                        stringResource(
                            id = R.string.date_weekday,
                            date.parseFromDay().formatToHumanDay(),
                            stringResource(id = R.string.time_from_to, beginTime, endTime)
                        )
                    )
                    if (comment != null && comment is String) Text(comment)
                    HorizontalDivider(Modifier.padding(16.dp))
                    lessonHomeworks.forEach { homework ->
                        val clipboardManager = LocalClipboardManager.current
                        Text(homework.homework, modifier = Modifier.clickable {
                            clipboardManager.setText(AnnotatedString(homework.homework))
                        })
                        homework.materials.forEach { material ->
                            material.items.forEach {
                                val ctx = LocalContext.current
                                OutlinedButton(onClick = {
                                    if (material.type == "attachments") {
                                        val browserIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(
                                                it.link
                                                    ?: it.urls.firstOrNull { it.urlType == "view" }?.url
                                            )
                                        )
                                        ContextCompat.startActivity(ctx, browserIntent, null)
                                    } else {
                                        DataService.getLaunchUrl(
                                            homework.homeworkEntryId,
                                            it.uuid ?: "",
                                        ) {
                                            webViewUrl = it
                                            openWebView = true
                                        }
                                    }
                                }, contentPadding = ButtonDefaults.ButtonWithIconContentPadding) {
                                    Icon(
                                        material.icon,
                                        stringResource(id = R.string.image),
                                        Modifier.size(ButtonDefaults.IconSize)
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text(it.title)
                                }
                            }
                        }
                    }
                    LazyRow {
                        items(marks) {
                            MarkComp(it, subjectId = subjectId)
                        }
                    }
                }
            }
        } else if (errorText == null) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else {
            ErrorMessage(Modifier.align(Alignment.Center), errorText!!)
        }
    }
}