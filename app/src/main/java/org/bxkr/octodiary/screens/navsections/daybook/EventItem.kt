package org.bxkr.octodiary.screens.navsections.daybook

import android.webkit.URLUtil
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.bxkr.octodiary.R
import android.util.Log
import org.bxkr.octodiary.components.MarkComp
import org.bxkr.octodiary.formatToTime
import org.bxkr.octodiary.getMarkConfig
import org.bxkr.octodiary.modalBottomSheetContentLive
import org.bxkr.octodiary.modalBottomSheetStateLive
import org.bxkr.octodiary.navControllerLive
import org.bxkr.octodiary.models.events.Event
import org.bxkr.octodiary.parseLongDate
import org.bxkr.octodiary.snackbarHostStateLive

@Composable
fun EventItem(event: Event, index: Int = -1, showLessonNumbers: Boolean = true) {
    var isExpanded by remember { mutableStateOf(false) }
    val enterTransition = remember {
        expandVertically(
            expandFrom = Alignment.Top, animationSpec = tween(200)
        )
    }
    val exitTransition = remember {
        shrinkVertically(
            shrinkTowards = Alignment.Top, animationSpec = tween(200)
        )
    }
    val markConfig = getMarkConfig()
    // Load lesson details when expanded (for lessons)
    var lessonInfo by remember { mutableStateOf<org.bxkr.octodiary.models.lesson2.LessonResponse?>(null) }
    var lessonError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(isExpanded) {
        if (isExpanded && event.source in listOf("AE", "CE", "PLAN")) {
            org.bxkr.octodiary.DataService.getLessonInfo(event.id, { lessonError = it }) {
                lessonInfo = it
            }
        }
    }
    Column(Modifier.clickable {
        isExpanded = !isExpanded
    }) {
        Column(
            Modifier.padding(
                top = 16.dp, start = 16.dp, end = 16.dp
            )
        ) {
            Row(
                Modifier
                    .padding(
                        bottom = 16.dp
                    )
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (index >= 0 && showLessonNumbers) {
                        Box(
                            Modifier
                                .padding(end = 8.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            Alignment.Center
                        ) {
                            Text(
                                (index + 1).toString(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    Text(
                        event.subjectName ?: (event.title ?: ""),
                        Modifier
                            .weight(1f, false)
                            .animateContentSize(),
                        maxLines = if (!isExpanded) 1 else Int.MAX_VALUE,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (event.source == "PLAN") {
                        EventIndicators(event, Modifier.padding(horizontal = 8.dp))
                    }
                }
                Text(
                    if (event.isAllDay != true) stringResource(
                        id = R.string.time_from_to,
                        event.startAt.parseLongDate().formatToTime(),
                        event.finishAt.parseLongDate().formatToTime()
                    ) else stringResource(id = R.string.all_day),
                    Modifier
                        .alpha(0.8f),
                    maxLines = 1,
                    softWrap = false
                )

            }
            AnimatedVisibility(
                visible = isExpanded, enter = enterTransition, exit = exitTransition
            ) {
                when (event.source) {
                    in listOf("AE", "CE", "PLAN") -> Box {
                        Column(
                            Modifier
                                .padding(bottom = 16.dp)
                                .fillMaxWidth()
                        ) {
                            // Theme/topic
                            val topic = event.lessonTheme ?: (lessonInfo?.comment as? String)
                            if (!topic.isNullOrBlank()) {
                                Text(
                                    topic,
                                    Modifier
                                        .padding(bottom = 8.dp)
                                        .animateContentSize(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            // Teacher
                            val teacherFio = lessonInfo?.teacher?.run { "$lastName $firstName $middleName" }
                            if (!teacherFio.isNullOrBlank()) {
                                Text(teacherFio)
                            }
                            // Location: room, building, address
                            val roomText = (lessonInfo?.roomNumber ?: event.roomNumber)
                            if (!roomText.isNullOrBlank()) {
                                Row {
                                    Text(
                                        stringResource(R.string.lesson_location),
                                        Modifier.padding(end = 3.dp).alpha(0.8f)
                                    )
                                    Text(roomText)
                                }
                            }
                            val building = lessonInfo?.buildingName ?: event.buildingName
                            if (!building.isNullOrBlank()) {
                                Text(building, Modifier.alpha(0.8f))
                            }
                            val address = event.address as? String
                            if (!address.isNullOrBlank()) {
                                Text(address, Modifier.alpha(0.8f))
                            }
                            // Marks row
                            if (event.marks != null) {
                                Row(Modifier.padding(top = 8.dp)) {
                                    event.marks.forEach {
                                        if (event.subjectId != null) {
                                            MarkComp(it, subjectId = event.subjectId, markConfig = markConfig)
                                        }
                                    }
                                }
                            }
                            // Homework presence
                            if ((lessonInfo?.lessonHomeworks?.isNotEmpty() == true) || (event.homework != null)) {
                                Text(stringResource(id = R.string.homework), Modifier.padding(top = 8.dp))
                                OutlinedButton(
                                    onClick = { navControllerLive.value?.navigate(org.bxkr.octodiary.NavSection.Homeworks.route) },
                                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.OpenInNew,
                                        stringResource(id = R.string.open),
                                        Modifier.size(ButtonDefaults.IconSize)
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text(stringResource(id = R.string.open))
                                }
                            }
                        }
                    }

                    "EVENTS" -> Column(
                        Modifier
                            .padding(
                                bottom = 16.dp
                            )
                            .fillMaxWidth()
                    ) {
                        Text(
                            event.description ?: stringResource(R.string.no_description)
                        )
                        if (event.place != null) {
                            Row {
                                Text(
                                    stringResource(R.string.lesson_location),
                                    Modifier
                                        .padding(end = 3.dp)
                                        .alpha(0.8f)
                                )
                                Text(event.place)
                            }
                        }
                        if (event.conferenceLink != null) {
                            val uriHandler = LocalUriHandler.current
                            val clipboardManager = LocalClipboardManager.current
                            val coroutineScope = rememberCoroutineScope()
                            val urlCopiedMessage = stringResource(R.string.url_copied_to_clipboard)
                            val unsupportedUrlMessage = stringResource(R.string.unsupported_url)
                            val interactionSource = remember { MutableInteractionSource() }
                            val viewConfiguration = LocalViewConfiguration.current


                            LaunchedEffect(interactionSource) {
                                var isLongClick = false
                                Log.d("EventItem", "Launched interactionSource effect for conference link")

                                interactionSource.interactions.collectLatest { interaction ->
                                    Log.d("EventItem", "Conference link interaction: $interaction")
                                    when (interaction) {
                                        is PressInteraction.Press -> {
                                            Log.d("EventItem", "Press start for conference link")
                                            isLongClick = false
                                            delay(viewConfiguration.longPressTimeoutMillis)
                                            Log.d("EventItem", "Long press timeout reached for conference link")
                                            isLongClick = true
                                            clipboardManager.setText(
                                                AnnotatedString(event.conferenceLink)
                                            )
                                            coroutineScope.launch {
                                                snackbarHostStateLive.value?.showSnackbar(
                                                    urlCopiedMessage
                                                )
                                            }
                                            Log.d("EventItem", "Long click action completed for conference link")
                                        }

                                        is PressInteraction.Release -> {
                                            Log.d("EventItem", "Press release for conference link, isLongClick=$isLongClick")
                                            if (!isLongClick && URLUtil.isValidUrl(event.conferenceLink)) {
                                                Log.d("EventItem", "Opening URL: ${event.conferenceLink}")
                                                uriHandler.openUri(event.conferenceLink)
                                            } else if (!URLUtil.isValidUrl(event.conferenceLink)) {
                                                Log.d("EventItem", "Invalid URL, showing error")
                                                coroutineScope.launch {
                                                    snackbarHostStateLive.value?.showSnackbar(
                                                        unsupportedUrlMessage
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            LaunchedEffect(interactionSource) {
                                var isLongClick = false

                                interactionSource.interactions.collectLatest { interaction ->
                                    when (interaction) {
                                        is PressInteraction.Press -> {
                                            isLongClick = false
                                            delay(viewConfiguration.longPressTimeoutMillis)
                                            isLongClick = true
                                            clipboardManager.setText(
                                                AnnotatedString(event.conferenceLink)
                                            )
                                            coroutineScope.launch {
                                                snackbarHostStateLive.value?.showSnackbar(
                                                    urlCopiedMessage
                                                )
                                            }
                                        }

                                        is PressInteraction.Release -> {
                                            if (!isLongClick && URLUtil.isValidUrl(event.conferenceLink)) {
                                                uriHandler.openUri(event.conferenceLink)
                                            } else if (!URLUtil.isValidUrl(event.conferenceLink)) {
                                                coroutineScope.launch {
                                                    snackbarHostStateLive.value?.showSnackbar(
                                                        unsupportedUrlMessage
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            OutlinedButton(
                                onClick = {},
                                modifier = Modifier
                                    .alpha(if (URLUtil.isValidUrl(event.conferenceLink)) 1f else .5f)
                                    .padding(top = 16.dp),
                                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                interactionSource = interactionSource
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.OpenInNew,
                                    stringResource(id = R.string.conference),
                                    Modifier.size(ButtonDefaults.IconSize),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text(
                                    stringResource(id = R.string.conference),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}