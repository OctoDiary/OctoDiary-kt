package org.bxkr.octodiary.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tbuonomo.viewpagerdotsindicator.compose.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.compose.model.DotGraphic
import com.tbuonomo.viewpagerdotsindicator.compose.type.WormIndicatorType
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.changelog.Changelog

@Composable
fun ChangelogDialog(onDismissRequest: () -> Unit) {
    Dialog(
        onDismissRequest,
        DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        ChangelogNavigation(Changelog.currentChangelog, onDismissRequest)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChangelogNavigation(currentChangelog: Changelog, onDismissRequest: () -> Unit) {
    var currentPage by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState { currentChangelog.elements?.size ?: 0 }
    val coroutineScope = rememberCoroutineScope()
    val scrollToPage = { page: Int ->
        coroutineScope.launch {
            pagerState.animateScrollToPage(page, animationSpec = tween(500))
//            pagerState.scroll {
//                scrollBy(scrollSize)
//            }
        }
    }
    var button1Style by remember { mutableStateOf("exit") }
    var button2Style by remember { mutableStateOf("exit") }
    button1Style = if (currentPage == 0) "exit" else "back"
    button2Style = if (currentPage == currentChangelog.elements?.lastIndex) "exit" else "next"

    val onButton1Click = {
        if (currentPage > 0) {
            currentPage -= 1
            scrollToPage(currentPage)
        } else onDismissRequest()
    }

    val onButton2Click = {
        if (currentPage < (currentChangelog.elements?.lastIndex ?: 0)) {
            currentPage += 1
            scrollToPage(currentPage)
        } else onDismissRequest()
    }

    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            HorizontalPager(pagerState, Modifier.width(100.dp)) { }
            Box {
                IconButton({ onDismissRequest() }, Modifier.padding(4.dp)) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        stringResource(R.string.close)
                    )
                }
                Column {
                    Text(
                        stringResource(currentChangelog.versionName),
                        Modifier
                            .fillMaxWidth()
                            .alpha(.8f)
                            .padding(top = 16.dp),
                        textAlign = TextAlign.Center,
                        lineHeight = 1.em,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        stringResource(R.string.changelog),
                        Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        lineHeight = 1.em,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            AnimatedContent(
                currentPage,
                Modifier
                    .weight(1f)
                    .fillMaxWidth(), label = "page_anim"
            ) { innerCurrentPage ->
                Column(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier
                            .weight(1f)
                            .zoomable(rememberZoomableState(ZoomSpec(maxZoomFactor = 2f)))
                    ) { currentChangelog.elements?.get(innerCurrentPage)?.composable?.invoke() }
                    Column(
                        Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        currentChangelog.elements?.get(innerCurrentPage)?.title?.let {
                            Text(
                                stringResource(it),
                                Modifier.padding(horizontal = 16.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Text(
                            stringResource(
                                currentChangelog.elements?.get(innerCurrentPage)?.subtitle
                                    ?: R.string.error_occurred
                            ),
                            Modifier.padding(horizontal = 16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

            }
            DotsIndicator(
                dotCount = currentChangelog.elements?.size ?: 0,
                dotSpacing = 8.dp,
                type = WormIndicatorType(
                    dotsGraphic = DotGraphic(
                        8.dp,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    wormDotGraphic = DotGraphic(
                        8.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                ),
                pagerState = pagerState
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center
            ) {
                AnimatedContent(button1Style, label = "controls_anim") { innerStyle ->
                    Box(
                        Modifier
                            .padding(horizontal = 4.dp)
                            .size(144.dp, 48.dp)
                            .border(
                                if (innerStyle == "exit") Dp.Unspecified else 1.dp,
                                MaterialTheme.colorScheme.outline,
                                MaterialTheme.shapes.medium
                            )
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onButton1Click() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                when (innerStyle) {
                                    "exit" -> Icons.Default.Close
                                    else -> Icons.AutoMirrored.Default.ArrowBack
                                },
                                stringResource(R.string.back_exit),
                                Modifier
                                    .padding(end = 8.dp)
                                    .size(18.dp),
                                MaterialTheme.colorScheme.primary
                            )
                            Text(
                                stringResource(
                                    when (innerStyle) {
                                        "exit" -> R.string.close
                                        else -> R.string.back
                                    }
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
                AnimatedContent(button2Style, label = "controls_anim") { innerStyle ->
                    Box(
                        Modifier
                            .padding(horizontal = 4.dp)
                            .size(144.dp, 48.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.shapes.medium
                            )
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onButton2Click() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(
                                    when (innerStyle) {
                                        "exit" -> R.string.close
                                        else -> R.string.next
                                    }
                                ),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Icon(
                                when (innerStyle) {
                                    "exit" -> Icons.Default.Done
                                    else -> Icons.AutoMirrored.Default.ArrowForward
                                },
                                stringResource(R.string.next),
                                Modifier
                                    .padding(start = 8.dp)
                                    .size(18.dp),
                                MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}


