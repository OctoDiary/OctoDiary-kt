package org.bxkr.octodiary.components.changelog


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.AddToHomeScreen
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.GlideSubcomposition
import com.bumptech.glide.integration.compose.RequestState
import com.bumptech.glide.integration.compose.placeholder
import kotlinx.coroutines.delay
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.MarkComp
import org.bxkr.octodiary.components.MarkConfig
import org.bxkr.octodiary.getDemoProperty
import org.bxkr.octodiary.models.events.Mark
import org.bxkr.octodiary.models.marklistsubject.MarkListSubjectItem
import org.bxkr.octodiary.pxToDp
import org.bxkr.octodiary.screens.navsections.marks.FinalsScreen
import org.bxkr.octodiary.screens.navsections.marks.SubjectCard
import org.bxkr.octodiary.simpleMark
import org.bxkr.octodiary.ui.theme.OctoDiaryTheme
import org.bxkr.octodiary.ui.theme.blue.DarkColorScheme
import org.bxkr.octodiary.ui.theme.blue.LightColorScheme
import org.bxkr.octodiary.widget.StatusWidgetReceiver
import kotlin.math.roundToInt


class Changelog26 : Changelog() {
    override val versionName: Int
        get() = R.string.c26_version_name
    override val versionShortname: Int
        get() = R.string.c26_version_shortname
    override val shortDescription: Int
        get() = R.string.c26_description
    override val elements = listOf(
        ChangelogItem(
            @Composable { NewDaybook() },
            R.string.c26_newdaybook_title,
            R.string.c26_newdaybook_subtitle
        ),
        ChangelogItem(
            @Composable { Finals() },
            R.string.c26_finals_title,
            R.string.c26_finals_subtitle
        ),
        ChangelogItem(
            @Composable { Widget() },
            R.string.c26_widget_title,
            R.string.c26_widget_subtitle
        ),
        ChangelogItem(
            @Composable { MarkCalculator() },
            R.string.c26_calc_title,
            R.string.c26_calc_subtitle
        ),
        ChangelogItem(
            @Composable { MarkHighlighting() },
            R.string.c26_highlighting_title,
            R.string.c26_highlighting_subtitle
        )
    )

    @OptIn(ExperimentalGlideComposeApi::class)
    @Suppress("DEPRECATION")
    @Composable
    private fun NewDaybook() {
        Box(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp), Alignment.Center
        ) {
            GlideImage(
                Uri.parse(ImageLinks.NEW_DAYBOOK_IMAGE),
                contentDescription = stringResource(R.string.c26_newdaybook_title),
                loading = placeholder {
                    CircularProgressIndicator()
                },
                failure = placeholder {
                    ImageFailure()
                },
                transition = CrossFade
            )
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    private fun Finals() {
        var frameSize by remember { mutableStateOf<IntSize?>(null) }
        var contentSize by remember { mutableStateOf<IntSize?>(null) }
        var imageLoaded by remember { mutableStateOf(false) }
        Box(
            Modifier
                .fillMaxSize(), Alignment.Center
        ) {
            GlideSubcomposition(
                Uri.parse(ImageLinks.FINALS_FRAME)
            ) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    when (state) {
                        RequestState.Failure -> ImageFailure()
                        RequestState.Loading -> CircularProgressIndicator()
                        is RequestState.Success -> {
                            imageLoaded = true
                            Image(
                                painter,
                                contentDescription = stringResource(R.string.c26_newdaybook_title),
                                Modifier.onSizeChanged { size ->
                                    frameSize = size
                                }
                            )
                        }
                    }
                }
            }
            val sSize = frameSize
            if (sSize != null && imageLoaded) {
                val context = LocalContext.current
                OctoDiaryTheme(
                    darkTheme = true,
                    dynamicColor = false,
                    amoledTheme = false,
                    LightColorScheme,
                    DarkColorScheme,
                    portable = true
                ) {
                    val scale = if (contentSize != null) {
                        val temp =
                            ((frameSize!!.width.toFloat() / contentSize!!.width.toFloat()) * 0.8223872f)
                        if (temp != 0f) temp else 1f
                    } else 1f
                    val temp = frameSize!!.width.toFloat() / 1751f
                    val imageScale = if (temp != 0f) temp else 1f
                    Box(
                        Modifier
                            .padding(
                                top = (55 * imageScale / scale)
                                    .roundToInt()
                                    .pxToDp()
                            )
                            .height(
                                (1968 * imageScale / scale)
                                    .roundToInt()
                                    .pxToDp()
                            )
                            .alpha(if (scale != 1f) 1f else 0f)
                            .scale(scale)
                            .onSizeChanged { size ->
                                contentSize = size
                            }
                    ) {
                        val scrollState = rememberScrollState()
                        FinalsScreen(
                            context.getDemoProperty(R.raw.demo_marks_subject),
                            scrollState
                        )
                        LaunchedEffect(Unit) {
                            while (true) {
                                delay(500)
                                scrollState.animateScrollTo(scrollState.maxValue, tween(2000))
                                delay(500)
                                scrollState.animateScrollTo(0, tween(2000))
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    private fun Widget() {
        val items = ImageLinks.WIDGET_IMAGES
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                ), Alignment.Center
        ) {
            GlideImage(
                ImageLinks.WIDGET_BACKGROUND, stringResource(R.string.background),
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
            )

            val listState = rememberLazyListState(Int.MAX_VALUE / 2)
            LaunchedEffect(Unit) {
                while (true) {
                    listState.animateScrollBy(
                        200f,
                        tween(500, easing = LinearEasing)
                    )
                }
            }
            LazyRow(
                state = listState, modifier = Modifier
                    .rotate(20f)
                    .requiredWidth(LocalConfiguration.current.screenWidthDp.dp * 2)
            ) {
                items(Int.MAX_VALUE) {
                    val index = it % items.size
                    GlideImage(
                        items[index],
                        stringResource(R.string.image),
                        Modifier
                            .padding(8.dp)
                            .height(192.dp)
                    )
                }
            }
            val context = LocalContext.current
            FloatingActionButton(
                {
                    pinWidget(context, StatusWidgetReceiver::class.java)
                },
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                elevation = FloatingActionButtonDefaults.loweredElevation()
            ) {
                Icon(
                    Icons.AutoMirrored.Default.AddToHomeScreen,
                    stringResource(R.string.add_to_home_screen)
                )
            }
        }
    }

    @Composable
    private fun ImageFailure() = Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Warning, "Error")
        Text(stringResource(R.string.error_occurred))
    }

    private fun <T> pinWidget(context: Context, className: Class<T>) {
        val myProvider = ComponentName(context, className)
        AppWidgetManager.getInstance(context).requestPinAppWidget(myProvider, null, null)
    }

    @Composable
    private fun MarkCalculator() {
        val context = LocalContext.current
        val demoMarks: List<MarkListSubjectItem> = context.getDemoProperty(R.raw.demo_marks_subject)

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            OctoDiaryTheme(
                darkTheme = true,
                dynamicColor = false,
                lightScheme = org.bxkr.octodiary.ui.theme.pink.LightColorScheme,
                darkScheme = org.bxkr.octodiary.ui.theme.pink.DarkColorScheme,
                portable = true
            ) {
                Surface(
                    Modifier.padding(horizontal = 16.dp), MaterialTheme.shapes.extraLarge
                ) {
                    val subject = demoMarks.first { it.subjectName == "Алгебра" }
                    val markConfig = MarkConfig(hideDefaultWeight = true, markHighlighting = true)
                    Box(
                        Modifier
                            .padding(16.dp)
                            .padding(vertical = 32.dp)
                    ) {
                        SubjectCard(
                            subject.periods?.get(0)!!,
                            subject.subjectId,
                            subject.subjectName,
                            false,
                            markConfig,
                            true
                        )
                        Box(
                            Modifier
                                .matchParentSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {})
                    }
                }
            }
        }
    }

    @Composable
    fun MarkHighlighting() {
        val getNewMap = {
            List(3) {
                List(4) {
                    (2..5).random() to (1..3).random()
                }
            }
        }
        var map by remember { mutableStateOf(getNewMap()) }
        LaunchedEffect(Unit) {
            while (true) {
                delay(2000)
                map = getNewMap()
            }
        }
        Box(
            Modifier
                .fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            AnimatedContent(map, label = "mark_highlighting_anim") { map ->
                Column {
                    repeat(3) { i1 ->
                        Row {
                            repeat(4) { i2 ->
                                MarkComp(
                                    Mark.fromMarkListSubject(
                                        simpleMark(
                                            map[i1][i2].first,
                                            map[i1][i2].second
                                        )
                                    ),
                                    subjectId = -1L,
                                    markConfig = MarkConfig(
                                        hideDefaultWeight = true,
                                        markHighlighting = true
                                    ),
                                    enabled = false,
                                    onClick = { _, _ -> }
                                )
                            }
                        }
                    }
                }
            }

        }
    }

    private object ImageLinks {
        const val NEW_DAYBOOK_IMAGE =
            "https://raw.githubusercontent.com/OctoDiary/.github/master/assets/c26_newdaybook_image.png"
        const val FINALS_FRAME =
            "https://raw.githubusercontent.com/OctoDiary/.github/master/assets/c26_finals_frame.png"
        const val WIDGET_BACKGROUND =
            "https://raw.githubusercontent.com/OctoDiary/.github/master/assets/c26_widget_background.webp"
        val WIDGET_IMAGES = listOf(
            "https://raw.githubusercontent.com/OctoDiary/.github/master/assets/c26_widget_rest.png",
            "https://raw.githubusercontent.com/OctoDiary/.github/master/assets/c26_widget_inclass.png",
            "https://raw.githubusercontent.com/OctoDiary/.github/master/assets/c26_widget_break.png",
            "https://raw.githubusercontent.com/OctoDiary/.github/master/assets/c26_widget_holidays.png"
        )
    }
}


