package org.bxkr.octodiary.screens.navsections

import android.content.Intent
import android.net.Uri
import android.webkit.URLUtil
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.Mark
import org.bxkr.octodiary.formatToDay
import org.bxkr.octodiary.formatToHumanDay
import org.bxkr.octodiary.formatToTime
import org.bxkr.octodiary.modalBottomSheetContentLive
import org.bxkr.octodiary.modalBottomSheetStateLive
import org.bxkr.octodiary.models.events.Event
import org.bxkr.octodiary.models.lessonschedule.LessonSchedule
import org.bxkr.octodiary.parseFromDay
import org.bxkr.octodiary.parseLongDate
import org.bxkr.octodiary.snackbarHostStateLive
import org.bxkr.octodiary.weekOfYear
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.roundToInt


@Composable
@OptIn(ExperimentalFoundationApi::class)
fun DaybookScreen() {
    val recompositionTrigger = remember { mutableStateOf(false) }
    key(recompositionTrigger.value) {
        val eventCalendar = DataService.eventCalendar
        val weekSplitCalendar = key(eventCalendar) {
            eventCalendar.fold(mutableListOf<MutableList<Event>>()) { sum, it ->
                if (sum.isEmpty() || sum.last().first().startAt.parseLongDate()
                        .formatToDay() != it.startAt.parseLongDate().formatToDay()
                ) {
                    sum.add(mutableListOf(it))
                } else {
                    sum.last().add(it)
                }
                sum
            }.fold(mutableListOf<MutableList<MutableList<Event>>>()) { sum, it ->
                if (sum.isEmpty() ||
                    Calendar.getInstance().run {
                        time = sum.last().first().first().startAt.parseLongDate()
                        get(Calendar.WEEK_OF_YEAR)
                    } != Calendar.getInstance().run {
                        time = it.first().startAt.parseLongDate()
                        get(Calendar.WEEK_OF_YEAR)
                    }
                ) {
                    sum.add(mutableListOf(it))
                } else {
                    sum.last().add(it)
                }
                sum
            }
        }
        val currentWeekIndex = weekSplitCalendar.indexOfFirst {
            Date().weekOfYear == it.first().first().startAt.parseLongDate().weekOfYear
        }
        val currentWeeksAfter = weekSplitCalendar.lastIndex - currentWeekIndex
        val addWeekBefore = { onFinish: () -> Unit ->
            DataService.updateEventCalendar(currentWeekIndex + 1, currentWeeksAfter) {
                recompositionTrigger.value = !recompositionTrigger.value
                onFinish()
            }
        }
        val addWeekAfter = { onFinish: () -> Unit ->
            DataService.updateEventCalendar(currentWeekIndex, currentWeeksAfter + 1) {
                recompositionTrigger.value = !recompositionTrigger.value
                onFinish()
            }
        }
        val weekPosition = remember {
            mutableFloatStateOf(currentWeekIndex.toFloat())
        }
        if (eventCalendar.isNotEmpty()) {
            val firstDayOfWeekInitial = remember {
                weekSplitCalendar[weekPosition.floatValue.roundToInt()][0][0].startAt.parseLongDate()
                    .let {
                        Calendar.getInstance().run {
                            time = it
                            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                            time
                        }
                    }
            }
            val weekDaysInitial = remember {
                (0..6).toList().map {
                    Calendar.getInstance().run {
                        time = firstDayOfWeekInitial
                        set(
                            Calendar.DAY_OF_WEEK, listOf(
                                Calendar.MONDAY,
                                Calendar.TUESDAY,
                                Calendar.WEDNESDAY,
                                Calendar.THURSDAY,
                                Calendar.FRIDAY,
                                Calendar.SATURDAY,
                                Calendar.SUNDAY
                            )[it]
                        )
                        time
                    }
                }
            }
            val todayInitial =
                remember {
                    Date().formatToDay()
                        .let { weekDaysInitial.indexOfFirst { it1 -> it1.formatToDay() == it } }
                        .takeIf {
                            it != -1
                        } ?: 0
                }
            val dayPosition = rememberPagerState(todayInitial, pageCount = { 7 })
            Column(Modifier.fillMaxSize()) {
                DateSeeker(
                    weekPosition,
                    dayPosition,
                    weekSplitCalendar.lastIndex,
                    addWeekBefore,
                    addWeekAfter
                )
                AnimatedContent(
                    targetState = weekPosition.floatValue,
                    label = "week_anim"
                ) { mWeekPosition ->
                    val daySplitCalendar = weekSplitCalendar[mWeekPosition.roundToInt()]
                    val firstDayOfWeek =
                        weekSplitCalendar[mWeekPosition.roundToInt()][0][0].startAt.parseLongDate()
                            .let {
                                Calendar.getInstance().run {
                                    time = it
                                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                                    time
                                }
                            }
                    val weekDays =
                        (0..6).toList().map {
                            Calendar.getInstance().run {
                                time = firstDayOfWeek
                                set(
                                    Calendar.DAY_OF_WEEK, listOf(
                                        Calendar.MONDAY,
                                        Calendar.TUESDAY,
                                        Calendar.WEDNESDAY,
                                        Calendar.THURSDAY,
                                        Calendar.FRIDAY,
                                        Calendar.SATURDAY,
                                        Calendar.SUNDAY
                                    )[it]
                                )
                                time
                            }
                        }
                    HorizontalPager(state = dayPosition, beyondBoundsPageCount = 6) { page ->
                        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
                            Text(
                                weekDays[page].let {
                                    SimpleDateFormat(
                                        "d MMMM, EEEE", LocalConfiguration.current.locales[0]
                                    ).format(it)
                                },
                                Modifier.padding(bottom = 8.dp),
                                style = MaterialTheme.typography.titleLarge
                            )
                            if (daySplitCalendar.size > page) {
                                LazyColumn(Modifier.fillMaxSize()) {
                                    dayItem(day = daySplitCalendar[page])
                                }
                            } else {
                                Column(
                                    Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = stringResource(id = R.string.free_day))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DateSeeker(
    weekPosition: MutableState<Float>,
    dayPosition: PagerState,
    size: Int,
    addWeekBefore: (() -> Unit) -> Unit,
    addWeekAfter: (() -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var sliderValue by remember { mutableFloatStateOf(dayPosition.currentPage.toFloat()) }
    sliderValue = dayPosition.currentPage.toFloat()
    ElevatedCard(
        shape = MaterialTheme.shapes.medium.copy(
            topStart = CornerSize(0.dp), topEnd = CornerSize(0.dp)
        )
    ) {
        Column(
            Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(id = R.string.weeks), style = MaterialTheme.typography.labelLarge
            )
            Row {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text(stringResource(id = R.string.add_week_before))
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    val loadingFinished = remember { mutableStateOf(true) }
                    IconButton(onClick = {
                        loadingFinished.value = false
                        addWeekBefore { loadingFinished.value = true }
                    }, enabled = loadingFinished.value) {
                        AnimatedVisibility(visible = loadingFinished.value) {
                            Icon(
                                Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                stringResource(id = R.string.add_week_before),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        AnimatedVisibility(visible = !loadingFinished.value) {
                            CircularProgressIndicator(
                                Modifier
                                    .size(20.dp, 20.dp),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
                Slider(
                    modifier = Modifier.weight(1f, true),
                    value = weekPosition.value,
                    onValueChange = { weekPosition.value = it },
                    valueRange = 0f..size.toFloat(),
                    steps = size - 1
                )
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text(stringResource(id = R.string.add_week_after))
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    val loadingFinished = remember { mutableStateOf(true) }
                    IconButton(onClick = {
                        loadingFinished.value = false
                        addWeekAfter { loadingFinished.value = true }
                    }, enabled = loadingFinished.value) {
                        AnimatedVisibility(visible = loadingFinished.value) {
                            Icon(
                                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                stringResource(id = R.string.add_week_after),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        AnimatedVisibility(visible = !loadingFinished.value) {
                            CircularProgressIndicator(
                                Modifier
                                    .size(20.dp, 20.dp),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
            Slider(value = sliderValue, onValueChange = {
                sliderValue = it
            }, onValueChangeFinished = {
                coroutineScope.launch {
                    dayPosition.animateScrollToPage(sliderValue.roundToInt())
                }
            }, valueRange = 0f..6f, steps = 5
            )
            Text(
                stringResource(id = R.string.days), style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

fun LazyListScope.dayItem(
    day: List<Event>,
    addBelow: @Composable () -> Unit = {}
) {
    items(day) {
        val cardShape =
            if (day.size == 1) MaterialTheme.shapes.large else if (day.indexOf(it) == 0) MaterialTheme.shapes.extraSmall.copy(
                topStart = MaterialTheme.shapes.large.topStart,
                topEnd = MaterialTheme.shapes.large.topEnd
            ) else if (day.indexOf(it) == day.lastIndex) MaterialTheme.shapes.extraSmall.copy(
                bottomStart = MaterialTheme.shapes.large.bottomStart,
                bottomEnd = MaterialTheme.shapes.large.bottomEnd
            ) else MaterialTheme.shapes.extraSmall
        val cardColor = MaterialTheme.colorScheme.run {
            when (it.source) {
                "AE" -> secondaryContainer
                "EC" -> primaryContainer
                "EVENTS" -> tertiaryContainer
                else -> surfaceContainer
            }
        }
        Card(
            Modifier
                .padding(bottom = 2.dp)
                .fillMaxWidth(),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            EventItem(event = it)
        }
    }
    item {
        addBelow()
    }
}

@Composable
fun EventItem(event: Event) {
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
                    .fillMaxWidth()
            ) {
                Text(
                    event.subjectName ?: (event.title ?: ""),
                    Modifier
                        .weight(1f)
                        .animateContentSize(),
                    maxLines = if (!isExpanded) 2 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (event.isAllDay != true) stringResource(
                        id = R.string.time_from_to,
                        event.startAt.parseLongDate().formatToTime(),
                        event.finishAt.parseLongDate().formatToTime()
                    ) else stringResource(id = R.string.all_day),
                    Modifier
                        .alpha(0.8f)
                        .weight(1f),
                    textAlign = TextAlign.End
                )

            }
            AnimatedVisibility(
                visible = isExpanded, enter = enterTransition, exit = exitTransition
            ) {
                when (event.source) {
                    in listOf("AE", "CE", "PLAN") -> Box {
                        Column(
                            Modifier
                                .padding(
                                    bottom = 16.dp
                                )
                                .fillMaxWidth()
                        ) {
                            if (event.roomNumber != null) {
                                Row {
                                    Text(
                                        stringResource(R.string.lesson_location),
                                        Modifier
                                            .padding(end = 3.dp)
                                            .alpha(0.8f)
                                    )
                                    Text(event.roomNumber)
                                }
                            }
                            if (event.homework != null && event.homework.descriptions.isNotEmpty()) {
                                Row {
                                    Text(
                                        stringResource(R.string.homework),
                                        Modifier
                                            .padding(end = 3.dp)
                                            .alpha(0.8f)
                                    )
                                    Column {
                                        event.homework.descriptions.forEach { Text(it) }
                                    }
                                }
                            }
                            if (event.marks != null) {
                                Row {
                                    event.marks.forEach {
                                        Mark(it)
                                    }
                                }
                            }
                        }
                        FilledTonalIconButton(
                            onClick = {
                                modalBottomSheetContentLive.postValue { LessonSheetContent(event.id) }
                                modalBottomSheetStateLive.postValue(true)
                            }, modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 16.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.MenuOpen,
                                stringResource(id = R.string.expand)
                            )
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

@Composable
fun LessonSheetContent(lessonId: Long) {
    var lessonInfo by remember { mutableStateOf<LessonSchedule?>(null) }
    LaunchedEffect(Unit) {
        DataService.getLessonInfo(lessonId) {
            lessonInfo = it
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
                        SelectionContainer {
                            Text(homework.homework)
                        }
                        homework.materials.forEach { material ->
                            material.items.forEach {
                                val ctx = LocalContext.current
                                OutlinedButton(onClick = {
                                    if (material.type == "test_spec_binding") {
                                        DataService.getLaunchUrl(
                                            homework.homeworkEntryId,
                                            it.uuid ?: "",
                                        ) {
                                            val browserIntent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(it)
                                            )
                                            startActivity(ctx, browserIntent, null)
                                        }
                                    } else {
                                        val browserIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(
                                                it.link
                                                    ?: it.urls.firstOrNull { it.urlType == "view" }?.url
                                            )
                                        )
                                        startActivity(ctx, browserIntent, null)
                                    }
                                }) {
                                    Text(it.title)
                                }
                            }
                        }
                    }
                    LazyRow {
                        items(marks) {
                            Mark(it)
                        }
                    }
                }
            }
        } else {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}