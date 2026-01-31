package org.bxkr.octodiary.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.Diary
import org.bxkr.octodiary.LocalActivity
import org.bxkr.octodiary.NavSection
import org.bxkr.octodiary.R
import org.bxkr.octodiary.Screen
import org.bxkr.octodiary.authPrefs
import org.bxkr.octodiary.cachePrefs
import org.bxkr.octodiary.components.EnterPinDialog
import org.bxkr.octodiary.components.SetPinDialog
import org.bxkr.octodiary.get
import org.bxkr.octodiary.isDemo
import org.bxkr.octodiary.isOnline
import org.bxkr.octodiary.logOut
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.navControllerLive
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.network.interfaces.DSchoolAPI
import org.bxkr.octodiary.network.interfaces.MainSchoolAPI
import org.bxkr.octodiary.network.interfaces.SchoolSessionAPI
import org.bxkr.octodiary.network.interfaces.SecondaryAPI
import org.bxkr.octodiary.save
import org.bxkr.octodiary.screenLive
import org.bxkr.octodiary.screens.AiDashboardScreen
import org.bxkr.octodiary.screens.LectureNotesScreen
import org.bxkr.octodiary.screens.VocabularySmartScreen
import org.bxkr.octodiary.screens.navsections.homeworks.HomeworkDetailScreen
import org.bxkr.octodiary.utils.PerformanceMonitor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavScreen(modifier: Modifier, pinFinished: MutableState<Boolean>) {
    PerformanceMonitor.TrackComposablePerformance("NavScreen")

    with(LocalContext.current) {
        val initialPin = remember { mutableStateOf(emptyList<Int>()) }
        val secondPin = remember { mutableStateOf(emptyList<Int>()) }
        val navController = navControllerLive.observeAsState()

        if (initialPin.value.size == 4 && secondPin.value.size == 4) {
            if (initialPin.value == secondPin.value) {
                pinFinished.value = true
                mainPrefs.save(
                    "has_pin" to true,
                    "pin" to secondPin.value.joinToString(""),
                    "first_launch" to false
                )
            } else {
                initialPin.value = emptyList()
                secondPin.value = emptyList()
                pinFinished.value = false

            }
        }

        Surface(modifier.fillMaxSize()) {
            if (mainPrefs.get<Boolean>("has_pin") != true || pinFinished.value) {
                DataService.token = authPrefs.get<String>("access_token")!!

                DataService.subsystem = Diary.values()[authPrefs.get<Int>("subsystem")!!]

                val diary = DataService.subsystem
                DataService.mainSchoolApi =
                    NetworkService.mainSchoolApi(MainSchoolAPI.getBaseUrl(diary))
                DataService.dSchoolApi = NetworkService.dSchoolApi(DSchoolAPI.getBaseUrl(diary))
                DataService.schoolSessionApi =
                    NetworkService.schoolSessionApi(SchoolSessionAPI.getBaseUrl(diary))
                DataService.secondaryApi =
                    NetworkService.secondaryApi(SecondaryAPI.getBaseUrl(diary))

                val context = LocalActivity.current
                // registerNotifier теперь в MainActivity, здесь не нужен вызов, если он вызывается там
                // Но логика инициализации DataService происходит здесь.
                // Мы можем вызвать ((context as? MainActivity)?.registerNotifier()) если сделаем метод public
                
                AnimatedVisibility(DataService.loadedEverything.value) {
                    val pullToRefreshStart = System.currentTimeMillis()
                    val refreshState = rememberPullToRefreshState()
                    var duringRefresh by rememberSaveable { mutableStateOf(false) }
                    var lastRefreshTime by rememberSaveable { mutableStateOf(0L) }

                    if (refreshState.isRefreshing && !duringRefresh &&
                        (System.currentTimeMillis() - lastRefreshTime) > 30000) {
                        val refreshStart = System.currentTimeMillis()
                        Log.d("Performance", "Pull-to-refresh started")
                        if (!isDemo) {
                            DataService.loadedEverything.value = false
                            DataService.loadingStarted = false
                            DataService.updateAll(context)
                            duringRefresh = true
                            lastRefreshTime = refreshStart
                        } else {
                            refreshState.endRefresh()
                            Log.d("Performance", "Pull-to-refresh completed (demo) in ${System.currentTimeMillis() - refreshStart}ms")
                        }
                    } else if (refreshState.isRefreshing && (System.currentTimeMillis() - lastRefreshTime) <= 30000) {
                        refreshState.endRefresh()
                        Log.d("Performance", "Pull-to-refresh cancelled - too frequent")
                    }

                    if (DataService.loadedEverything.value) {
                        refreshState.endRefresh()
                        duringRefresh = false
                        if (duringRefresh) {
                            Log.d("Performance", "Pull-to-refresh data load completed in ${System.currentTimeMillis() - pullToRefreshStart}ms")
                        }
                    }
                    LaunchedEffect(Unit) {
                        DataService.sendStatistic {}
                    }
                    val startDestinationRoute = remember {
                        val pref = context.mainPrefs.get<String>("start_destination")
                        val validRoutes = NavSection.values().map { it.route }.toSet()
                        if (pref != null && pref in validRoutes) pref else NavSection.Dashboard.route
                    }
                    NavHost(
                        navController = navController.value!!,
                        startDestination = startDestinationRoute
                    ) {
                        NavSection.values().forEach {
                            composable(it.route) { _ ->
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .nestedScroll(refreshState.nestedScrollConnection)
                                ) {
                                    it.composable()
                                    PullToRefreshContainer(
                                        refreshState, Modifier.align(Alignment.TopCenter)
                                    )
                                }
                            }
                        }
                        composable("homework/{entryStudentId}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("entryStudentId")?.toLongOrNull()
                            if (id != null) {
                                PerformanceMonitor.TrackComposablePerformance("HomeworkDetailScreen")
                                HomeworkDetailScreen(entryStudentId = id)
                            } else {
                                NavSection.Homeworks.composable()
                            }
                        }
                        
                        composable("formulas") { FormulaBaseScreen() }
                        composable(Screen.AiDashboard.route) { AiDashboardScreen() }
                        composable(Screen.VocabularySmartScreen.route) { VocabularySmartScreen() }
                        composable(Screen.LectureNotesScreen.route) { LectureNotesScreen() }
                        composable(Screen.TextbookExtractorScreen.route) { TextbookExtractorScreen() }
                        composable(Screen.TextbooksScreen.route) {
                            TextbooksScreen(onBack = { navController.value?.navigateUp() })
                        }
                        composable("quotes") { QuoteBookScreen() }
                        composable("vocabulary") { VocabularyScreen() }
                        composable("notes") { NotesScreen() }
                        composable("analytics") { AnalyticsScreen() }
                        composable("recommendations") { RecommendationsScreen() }
                        composable("parent_reports") { ParentReportsScreen() }
                        composable("theme_creator") { ThemeCreatorScreen() }
                        composable("comparison") { ComparisonScreen() }
                    }
                }
                AnimatedVisibility(!DataService.loadedEverything.value) {
                    var progress by remember { mutableFloatStateOf(0f) }
                    val progressAnimated by animateFloatAsState(
                        progress, tween(200), label = "progress_anim"
                    )
                    val coroutineScope = rememberCoroutineScope()
                    DataService.onSingleItemInUpdateAllLoadedHandler = { name, progressParam ->
                        coroutineScope.launch { progress = progressParam }
                        if (name in listOf("profile", "marksSubject", "homeworks", "schedule")) {
                            // Кэширование отключено временно
                            /*
                            cachePrefs.save(
                                name to Gson().toJson(
                                    DataService::class.java.getDeclaredField(name).get(DataService)
                                )
                            )
                            */
                        }
                        cachePrefs.save("age" to System.currentTimeMillis())
                    }
                    val activity = LocalActivity.current
                    DataService.tokenExpirationHandler = {
                        activity.logOut("Performed from token expiration handler")
                    }
                    if (!DataService.loadingStarted) {
                        val cachedAge = cachePrefs.get<Long>("age")
                        val online = activity.isOnline()
                        if (cachedAge != null) {
                            val isFresh = (System.currentTimeMillis() - cachedAge) < 86400000
                            if (!online || isFresh) {
                                DataService.loadingStarted = true
                                DataService.subsystem = Diary.values()[authPrefs.get<Int>("subsystem") ?: 0]
                                // DataService.loadFromCache { cachePrefs.get<String>(it) ?: "" }
                                DataService.loadedEverything.value = true
                            } else {
                                DataService.updateAll(context)
                            }
                        } else if (isDemo) {
                            DataService.subsystem = Diary.MES
                            // DataService.run { loadDemoCache() }
                            DataService.loadedEverything.value = true
                        } else if (online) {
                            DataService.updateAll(context)
                        }
                    }
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        LinearProgressIndicator(
                            progress = { progressAnimated },
                        )
                        val showOffline = !LocalActivity.current.isOnline()
                        if (showOffline) {
                            Spacer(Modifier.size(12.dp))
                            Text(
                                text = stringResource(id = R.string.offline_mode),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.alpha(0.8f)
                            )
                        }
                    }
                }
                screenLive.value = Screen.MainNav
            } else if (mainPrefs.get<Boolean>("has_pin") == true && !pinFinished.value) {
                EnterPinDialog(pinFinished = pinFinished)
            }
            if ((mainPrefs.get<Boolean>("first_launch") == true) && !pinFinished.value) {
                SetPinDialog(
                    pinFinished = pinFinished, initialPin = initialPin, secondPin = secondPin
                )
            }
        }
    }
}