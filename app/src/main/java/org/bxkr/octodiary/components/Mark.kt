package org.bxkr.octodiary.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.Shape
import org.bxkr.octodiary.CloverShape
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.NavSection
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.settings.CommonPrefs
import org.bxkr.octodiary.get
import org.bxkr.octodiary.getDemoProperty
import org.bxkr.octodiary.getMarkConfig
import org.bxkr.octodiary.isDemo
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.modalBottomSheetContentLive
import org.bxkr.octodiary.modalBottomSheetStateLive
import org.bxkr.octodiary.models.events.Mark
import org.bxkr.octodiary.models.mark.MarkInfo
import org.bxkr.octodiary.models.marklistsubject.MarkListSubjectItem
import org.bxkr.octodiary.navControllerLive
import org.bxkr.octodiary.parseSimpleLongAndFormatToLong
import org.bxkr.octodiary.rememberMarker
import org.bxkr.octodiary.screens.navsections.marks.SubjectRatingBottomSheet
import org.bxkr.octodiary.screens.navsections.marks.scrollToSubjectIdLive
import kotlin.math.roundToInt

data class MarkConfig(
    val hideDefaultWeight: Boolean,
    val markHighlighting: Boolean,
)

@Composable
fun MarkComp(
    mark: Mark,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    subjectId: Long,
    markConfig: MarkConfig,
    showPlus: Boolean = false,
    onClick: (Mark, Long) -> Unit = ::defaultMarkClick,
) {
    val color = MaterialTheme.colorScheme.run {
        if (markConfig.markHighlighting) {
            when (mark.value) {
                "3" -> tertiaryContainer
                "2" -> errorContainer
                else -> secondaryContainer
            }
        } else secondaryContainer
    }

    FilledTonalIconButton(
        onClick = { if (enabled) onClick(mark, subjectId) },
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = color)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(2.dp)
        ) {
            Text(
                mark.value,
                Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.labelLarge
            )
            if ((!markConfig.hideDefaultWeight || mark.weight != 1) || showPlus) {
                Text(
                    if (showPlus) "+" else mark.weight.toString(),
                    Modifier.align(Alignment.BottomEnd),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun MarkSimple(value: String, markConfig: MarkConfig, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.run {
        if (markConfig.markHighlighting) {
            when (value) {
                "3" -> tertiaryContainer
                "2" -> errorContainer
                else -> secondaryContainer
            }
        } else secondaryContainer
    }

    Box(
        modifier
            .size(40.dp)
            .background(color, MaterialTheme.shapes.small)
    ) {
        Text(
            value, Modifier.align(Alignment.Center), MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun MarkSheetContent(mark: Mark, subjectId: Long) {
    var markInfo by remember { mutableStateOf<MarkInfo?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (context.isDemo) {
            markInfo = context.getDemoProperty(R.raw.demo_mark_info)
        } else {
            DataService.getMarkInfo(mark.id, { errorMessage = it }) { markInfo = it }
        }
    }
    val subject = remember {
        if (DataService.hasMarksSubject) {
            DataService.marksSubject.firstOrNull { it.subjectId == subjectId }
        } else null
    }

    Box(
        Modifier
            .heightIn(192.dp, Int.MAX_VALUE.dp)
            .fillMaxWidth()
    ) {
        if (markInfo != null) {
            MarkInfo(markInfo!!, subject, mark)
        } else if (errorMessage == null) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else {
            ErrorMessage(Modifier.align(Alignment.Center), errorMessage!!)
        }
    }
}

@Composable
fun BoxScope.MarkInfo(markInfo: MarkInfo, subject: MarkListSubjectItem?, mark: Mark) {
    Column(Modifier.padding(16.dp)) {
        MarkDescription(subject, markInfo)
        if (markInfo.classResults != null) {
            ProvideVicoTheme(theme = rememberM3VicoTheme()) {
                ClassResults(markInfo)
            }
        }
    }
    Column(
        Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MarkComp(mark, enabled = false, subjectId = 0L, markConfig = getMarkConfig())
        subject?.let {
            RatingButton(it)
            AverageChip(it)
        }
    }
}

@Composable
fun MarkDescription(subject: MarkListSubjectItem?, markInfo: MarkInfo) {
    if (subject != null) {
        Text(
            subject.subjectName,
            style = MaterialTheme.typography.titleMedium
        )
    } else {
        Text(
            stringResource(R.string.mark),
            style = MaterialTheme.typography.titleMedium
        )
    }
    with(markInfo) {
        Text(teacher.run { "$lastName $firstName $middleName" })
        Text(controlFormName)
        if (commentExists) Text(comment!!)
        Text(
            stringResource(
                R.string.mark_created,
                parseSimpleLongAndFormatToLong(
                    updatedAt,
                    stringResource(id = R.string.at_time)
                )
            ),
            Modifier.padding(vertical = 16.dp)
        )
    }
}

@Composable
fun ClassResults(markInfo: MarkInfo) {
    val results = markInfo.classResults?.marksDistributions?.sortedByDescending { it.markValue.five }
        ?: throw IllegalStateException("Unexpected")
    val labelListKey = ExtraStore.Key<List<String>>()
    val secondaryColor = MaterialTheme.colorScheme.secondary.toArgb()
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val layer = rememberColumnCartesianLayer(
        columnProvider = object : ColumnCartesianLayer.ColumnProvider {
            override fun getColumn(
                entry: ColumnCartesianLayerModel.Entry,
                seriesIndex: Int,
                extraStore: ExtraStore,
            ): LineComponent {
                val isCurrent =
                    results[entry.x.roundToInt()].markValue.five.toString() == markInfo.value
                return LineComponent(
                    if (isCurrent) primaryColor else secondaryColor,
                    8f,
                    Shape.rounded(40)
                )
            }

            override fun getWidestSeriesColumn(
                seriesIndex: Int,
                extraStore: ExtraStore,
            ): LineComponent {
                return LineComponent(
                    secondaryColor,
                    8f,
                    Shape.rounded(40)
                )
            }
        }
    )
    val modelProducer = CartesianChartModelProducer.build()
    val chart = rememberCartesianChart(
        layer,
        startAxis = rememberStartAxis(
            title = stringResource(R.string.students_count),
            titleComponent = rememberTextComponent(color = MaterialTheme.colorScheme.onSurface)
        ),
        bottomAxis = rememberBottomAxis(
            valueFormatter = { x, chartValues, _ ->
                chartValues.model.extraStore[labelListKey][x.toInt()]
            },
            title = stringResource(R.string.mark),
            titleComponent = rememberTextComponent(color = MaterialTheme.colorScheme.onSurface)
        ),
    )
    Text(
        stringResource(R.string.class_results),
        Modifier
            .padding(top = 16.dp)
            .fillMaxWidth(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleMedium
    )
    CartesianChartHost(
        chart,
        modelProducer,
        marker = rememberMarker()
    )
    modelProducer.tryRunTransaction {
        columnSeries {
            series(results.map { it.numberOfStudents })
        }
        updateExtras { it[labelListKey] = results.map { it.markValue.five.toString() }.toList() }
    }
}

@Composable
fun RatingButton(subject: MarkListSubjectItem) {
    val context = LocalContext.current
    if (context.mainPrefs.get(CommonPrefs.subjectRating.prefKey) ?: true) {
        DataService.subjectRanking.firstOrNull { it.subjectId == subject.subjectId }
            ?.let {
                FilledIconButton(
                    {
                        modalBottomSheetContentLive.postValue {
                            SubjectRatingBottomSheet(
                                subject.subjectId,
                                subject.subjectName
                            )
                        }
                    },
                    Modifier.padding(top = 6.dp),
                    shape = CloverShape
                ) {
                                                Text(
                                                    it.rank.rankPlace?.toString() ?: "?",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )                }
            }
    }
}

@Composable
fun AverageChip(subject: MarkListSubjectItem) {
    val navController = navControllerLive.observeAsState().value
    if (subject.currentPeriod != null) {
        Row(
            Modifier
                .padding(top = 8.dp)
                .clip(CircleShape)
                .let {
                    if (navController != null) {
                        it.clickable {
                            modalBottomSheetStateLive.postValue(false)
                            scrollToSubjectIdLive.value = subject.subjectId
                            navController.navigate(route = NavSection.Marks.route)
                        }
                    } else it
                }
                .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val textStyle = MaterialTheme.typography.labelLarge
            Icon(
                Icons.AutoMirrored.Default.TrendingUp,
                stringResource(R.string.average_mark),
                Modifier
                    .padding(horizontal = 4.dp)
                    .size(textStyle.fontSize.value.toInt().dp),
                MaterialTheme.colorScheme.tertiary
            )
            Text(
                subject.currentPeriod?.fixedValue ?: subject.currentPeriod?.value ?: "",
                Modifier.padding(end = 4.dp),
                color = MaterialTheme.colorScheme.tertiary,
                style = textStyle
            )
        }
    }
}

fun defaultMarkClick(mark: Mark, subjectId: Long) {
    modalBottomSheetStateLive.postValue(true)
    modalBottomSheetContentLive.postValue { MarkSheetContent(mark, subjectId) }
}


