package org.bxkr.octodiary


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.util.Log
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.derivedStateOf
import java.time.LocalTime
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.graphics.scale
import androidx.activity.ComponentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.bxkr.octodiary.components.DebugMenu
import org.bxkr.octodiary.components.MigrationDialog
import org.bxkr.octodiary.components.ProfileChooser
import org.bxkr.octodiary.components.SettingsDialog
import org.bxkr.octodiary.components.TokenLogin
import org.bxkr.octodiary.components.settings.About
import org.bxkr.octodiary.screens.CallbackScreen
import org.bxkr.octodiary.screens.CallbackType
import org.bxkr.octodiary.screens.LoginScreen
import org.bxkr.octodiary.screens.NavScreen
import org.bxkr.octodiary.utils.PerformanceMonitor
import org.bxkr.octodiary.screens.navsections.daybook.DayChooser
import org.bxkr.octodiary.screens.navsections.profile.avatarTriggerLive
import org.bxkr.octodiary.services.McpServerService
import org.bxkr.octodiary.ui.theme.CustomColorScheme
import org.bxkr.octodiary.ui.theme.OctoDiaryTheme
import java.io.ByteArrayOutputStream
import java.io.File
import com.google.gson.Gson
import android.app.PendingIntent
import android.app.AlarmManager
import java.util.Calendar

val modalBottomSheetStateLive = MutableLiveData(false)
val modalBottomSheetContentLive = MutableLiveData<@Composable () -> Unit> {}
val snackbarHostStateLive = MutableLiveData(SnackbarHostState())
val navControllerLive = MutableLiveData<NavHostController?>(null)
val showFilterLive = MutableLiveData(false)
val contentDependentActionLive = MutableLiveData<@Composable () -> Unit> {}
val contentDependentActionIconLive = MutableLiveData(Icons.Default.FilterAlt)
val screenLive = MutableLiveData<Screen>()
val modalDialogStateLive = MutableLiveData(false)
val modalDialogContentLive = MutableLiveData<@Composable () -> Unit> {}
val modalDialogCloseListenerLive = MutableLiveData<() -> Unit> {}
val reloadEverythingLive = MutableLiveData {}
val darkThemeLive = MutableLiveData<Boolean>(null)
val colorSchemeLive = MutableLiveData(-1)
val launchUrlLive = MutableLiveData<Uri?>(null)
val launchPickerLive = MutableLiveData<() -> Unit>({})
val LocalActivity = staticCompositionLocalOf<ComponentActivity> {
    error("No LocalActivity provided!")
}

class MainActivity : ComponentActivity() {
    private fun createNotificationChannel() {
        val name = getString(R.string.data_update_channel_name)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("data_update", name, importance)
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Обработка изменения ориентации экрана
        // При необходимости можно добавить логику для адаптации UI
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        // Применяем сохранённый масштаб текста
        val textScale = mainPrefs.get<Float>("text_scale") ?: 1.0f
        val config = resources.configuration
        config.fontScale = textScale
        resources.updateConfiguration(config, resources.displayMetrics)

        // Инициализируем мониторинг производительности
        PerformanceMonitor.initialize(this)

        // Запускаем Battery Monitor
        org.bxkr.octodiary.utils.BatteryMonitor(this).startMonitoring { isActive ->
            // Battery saver активирован/деактивирован
            if (isActive) {
                // Очищаем кэш при активации battery saver
                DataService.clearCacheIfLowMemory(this)
            }
        }
        
        // Запускаем Bell Schedule Worker
        org.bxkr.octodiary.workers.BellScheduleWorker.scheduleNotifications(this)

        // Запускаем MCP Server Service
        startService(Intent(this, McpServerService::class.java))

        // Запускаем автоматическую запись лекций если включена в настройках
        val aiPrefs = getSharedPreferences("ai_prefs", Context.MODE_PRIVATE)
        if (aiPrefs.getBoolean("auto_record_lectures", false)) {
            org.bxkr.octodiary.audio.AutomaticLectureRecordingService.startAutomaticRecording(this)
        }

        // Запускаем сервис автоматического обновления данных, если включено в настройках
        if (mainPrefs.get<Boolean>("auto_update_enabled") != false) {
            startService(Intent(this, AutoUpdateService::class.java))
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }
        val picker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri ?: return@registerForActivityResult
            lifecycleScope.launch(Dispatchers.IO) {
                val cursor = contentResolver.query(uri, null, null, null)
                val result: String =
                    if (cursor == null) { // Source is Dropbox or other similar local file path
                        uri.path!!
                    } else {
                        cursor.moveToFirst()
                        val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                        val string = cursor.getString(idx)
                        cursor.close()
                        string
                    }
                val file = File(result)
                val bitmap = BitmapFactory.decodeFile(file.path)
                val byteOutputStream = ByteArrayOutputStream()
                bitmap.run {
                    if (height > width) {
                        scale(200, height / (width / 200))
                    } else if (width > height) {
                        scale(width / (height / 200), 200)
                    } else scale(200, 200)
                }.compress(Bitmap.CompressFormat.PNG, 100, byteOutputStream)
                val requestFile = RequestBody.create(
                    "multipart/form-data".toMediaType(),
                    byteOutputStream.toByteArray()
                )
                val part = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val upload = {
                    DataService.secondaryApi.uploadAvatar(
                        "Bearer ${DataService.token}",
                        DataService.profile.children[DataService.currentProfile].contingentGuid,
                        part
                    ).baseEnqueueOrNull {
                        DataService.updateAvatars {
                            modalDialogStateLive.postValue(false)
                            avatarTriggerLive.postValue(avatarTriggerLive.value?.not() ?: true)
                        }
                    }
                }

                DataService.run {
                    if (avatars.isNotEmpty()) {
                        secondaryApi.deleteAvatar(
                            "Bearer $token",
                            profile.children[currentProfile].contingentGuid,
                            avatars.first().id.toString()
                        ).baseEnqueueOrNull {
                            upload()
                        }
                    } else upload()
                }
            }
        }
        launchPickerLive.postValue {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        enableEdgeToEdge()
        setContent {
            colorSchemeLive.value = mainPrefs.get("theme") ?: -1
            darkThemeLive.value = mainPrefs.get("is_dark_theme") ?: isSystemInDarkTheme()
            val colorScheme by colorSchemeLive.observeAsState(-1)
            val darkTheme by darkThemeLive.observeAsState(isSystemInDarkTheme())
            /**
             * When `colorScheme == -1`, it uses dynamic colors **if available**.
             * If not, it uses default (yellow).
             **/
            val themeStartTime = System.currentTimeMillis()
            val animationStartTime = System.currentTimeMillis()
            // Оптимизация: используем remember для избежания лишних рекомпозиций
            val currentThemeState = remember(darkTheme, colorScheme) {
                darkTheme to colorScheme
            }

            AnimatedContent(
                targetState = currentThemeState,
                label = "theme_anim",
                transitionSpec = { androidx.compose.animation.ContentTransform(
                    targetContentEnter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(150)),
                    initialContentExit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(150))
                ) }
            ) {
                val currentScheme = remember(it.second) {
                    when {
                        it.second == -1 -> CustomColorScheme.Yellow
                        else -> CustomColorScheme.values()[it.second]
                    }
                }
                val amoledTheme = mainPrefs.get<Boolean>("amoled_theme") ?: false
                if (BuildConfig.DEBUG) {
                    Log.d("Performance", "Theme selection completed in ${System.currentTimeMillis() - themeStartTime}ms")
                    Log.d("Performance", "Theme animation transition time: ${System.currentTimeMillis() - animationStartTime}ms")
                    LaunchedEffect(Unit) {
                        Log.d("Performance", "Theme AnimatedContent recomposed")
                    }
                }
                // Оптимизация: проверяем влияние тем на производительность
                val themeApplicationStart = System.currentTimeMillis()
                val themeKey = "${it.first}_${it.second}_$amoledTheme"
                if (BuildConfig.DEBUG) {
                    Log.d("Performance", "Applying theme with key: $themeKey")
                }
                OctoDiaryTheme(
                    it.first,
                    colorScheme == -1,
                    amoledTheme,
                    currentScheme.lightColorScheme,
                    currentScheme.darkColorScheme
                ) {
                    if (BuildConfig.DEBUG) {
                        Log.d("Performance", "Theme application completed in ${System.currentTimeMillis() - themeApplicationStart}ms")
                    }
                    // Мониторим производительность MyApp
                    PerformanceMonitor.TrackComposablePerformance("MyApp")
                    MyApp(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    @Composable
    private fun MyApp(
        modifier: Modifier = Modifier,
    ) {
        val startTime = System.currentTimeMillis()
        val recomposeScope = currentRecomposeScope
        if (BuildConfig.DEBUG) {
            Log.d("Performance", "MyApp composition started - recompose scope: ${recomposeScope.hashCode()}")
        }

        var title by rememberSaveable { mutableIntStateOf(R.string.app_name) }
        screenLive.value = if (authPrefs.get<Boolean>("auth") == true) {
            Screen.MainNav
        } else Screen.Login
        val pinFinished = remember { mutableStateOf(false) }
        val currentScreen = screenLive.observeAsState()
        val showBottomSheet by modalBottomSheetStateLive.observeAsState()
        val bottomSheetContent by modalBottomSheetContentLive.observeAsState()
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val snackbarHostState = snackbarHostStateLive.value!!
        if (navControllerLive.value == null) {
            navControllerLive.value = rememberNavController()
        }
        val navController = navControllerLive.observeAsState()
        val surfaceColor = MaterialTheme.colorScheme.surface
        val elevatedColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        // Оптимизация: используем derivedStateOf для topAppBarColor вместо прямого изменения
        val navBackStackEntry by navController.value?.currentBackStackEntryAsState() ?: mutableStateOf(null)
        val currentRoute by derivedStateOf { navBackStackEntry?.destination?.route }
        val topAppBarColor by derivedStateOf { if (currentRoute == NavSection.Daybook.route) elevatedColor else surfaceColor }
        val contentDependentAction = contentDependentActionLive.observeAsState()
        val showDialog = modalDialogStateLive.observeAsState()
        val dialogContent = modalDialogContentLive.observeAsState()
        val showFilter = showFilterLive.observeAsState(false)
        val launchUrl = launchUrlLive.observeAsState()

        if (BuildConfig.DEBUG) {
            Log.d("Performance", "MyApp basic setup completed in ${System.currentTimeMillis() - startTime}ms")
        }
        if (authPrefs.get<String>("access_token") != null) {
            if ((mainPrefs.get<Int>("version") ?: 25) <= 25) {
                modalDialogCloseListenerLive.value = {
                    mainPrefs.save("version" to BuildConfig.VERSION_CODE)
                    logOut("Migration from version 25")
                }
                modalDialogContentLive.value = {
                    MigrationDialog { modalDialogCloseListenerLive.value?.invoke() }
                }
                modalDialogStateLive.value = true
            } else if ((mainPrefs.get<Int>("version") ?: 31) <= 31) {
                logOut("Migration from version 31")
            } else if (mainPrefs.get<Int>("version") != BuildConfig.VERSION_CODE) {
                mainPrefs.save("version" to BuildConfig.VERSION_CODE)
            }
        } else if (mainPrefs.get<Int>("version") != BuildConfig.VERSION_CODE) {
            mainPrefs.save("version" to BuildConfig.VERSION_CODE)
        }

        if (launchUrl.value != null) {
            val tabIntent = CustomTabsIntent.Builder().build()
            tabIntent.launchUrl(LocalContext.current, launchUrl.value!!)
            launchUrlLive.postValue(null)
        }

        // Оптимизация: убираем ненужные логи в продакшене
        if (BuildConfig.DEBUG) {
            Log.d("Performance", "Optimized navigation listener removed - using derivedStateOf instead")
        }

        val intentData = intent.dataString
        intent.setData(null)
        if (intentData != null) {
            if (authPrefs.get<Boolean>("auth") == true) {
                LaunchedEffect(Unit) {
                    snackbarHostState.showSnackbar(getString(R.string.already_auth))
                }
            } else {
                screenLive.value = Screen.Callback
            }
        }

        var localLoadedState by remember { mutableStateOf(false) }
        var settingsShown by remember { mutableStateOf(false) }
        LaunchedEffect(rememberCoroutineScope()) {
            snapshotFlow { DataService.loadedEverything.value }.onEach { localLoadedState = it }
                .launchIn(this)
        }
        CompositionLocalProvider(LocalActivity provides this) {
            Scaffold(modifier, topBar = {
                Column {
                    val topAppBarStartTime = System.currentTimeMillis()
                    TopAppBar(title = {
                        val titleAnimStart = System.currentTimeMillis()
                        val titleAnimationStart = System.currentTimeMillis()
                        AnimatedContent(targetState = title, label = "title_anim") {
                            if ((currentScreen.value == Screen.MainNav && localLoadedState) || currentScreen.value != Screen.MainNav) {
                                Text(stringResource(it))
                            } else {
                                Text(stringResource(R.string.app_name))
                            }
                            if (BuildConfig.DEBUG) {
                                Log.d("Performance", "Title AnimatedContent animation time: ${System.currentTimeMillis() - titleAnimationStart}ms")
                                LaunchedEffect(Unit) {
                                    Log.d("Performance", "Title AnimatedContent recomposed")
                                }
                            }
                        }
                        Log.d("Performance", "Title animation completed in ${System.currentTimeMillis() - titleAnimStart}ms")
                    }, actions = {
                        if (BuildConfig.DEBUG || mainPrefs.get<Boolean>("force_debug") == true) {
                            DebugMenu(this@MainActivity)
                        }
                        if (localLoadedState && currentScreen.value == Screen.MainNav) {
                            val localCurrentRoute =
                                navController.value!!.currentBackStackEntryAsState().value?.destination?.route
                            AnimatedVisibility(localCurrentRoute == NavSection.Profile.route) {
                                Row(Modifier) {
                                    IconButton(onClick = {
                                        modalDialogContentLive.value = { ProfileChooser() }
                                        modalDialogStateLive.postValue(true)
                                    }) {
                                        Icon(
                                            Icons.Default.Groups,
                                            stringResource(id = R.string.choose_context_profile)
                                        )
                                    }
                                    IconButton(onClick = { settingsShown = true }) {
                                        Icon(
                                            Icons.Default.Settings,
                                            stringResource(id = R.string.settings)
                                        )
                                    }
                                }
                            }
                            AnimatedVisibility(currentRoute == NavSection.Daybook.route) {
                                Row {
                                    IconButton(onClick = {
                                        modalDialogContentLive.value = { DayChooser() }
                                        modalDialogStateLive.postValue(true)
                                    }) {
                                        Icon(
                                            Icons.Default.CalendarMonth,
                                            stringResource(id = R.string.by_date)
                                        )
                                    }
                                }
                            }
                            AnimatedVisibility(showFilter.value) {
                                var expanded by remember {
                                    mutableStateOf(false)
                                }
                                Box(contentAlignment = Alignment.Center) {
                                    val icon =
                                        contentDependentActionIconLive.observeAsState(Icons.Default.FilterAlt)
                                    IconButton(onClick = { expanded = !expanded }) {
                                        val actionIconAnimationStart = System.currentTimeMillis()
                                        AnimatedContent(
                                            targetState = icon.value,
                                            label = "action_icon_anim"
                                        ) {
                                            Icon(it, "action")
                                            if (BuildConfig.DEBUG) {
                                                Log.d("Performance", "Action icon animation time: ${System.currentTimeMillis() - actionIconAnimationStart}ms")
                                            }
                                        }
                                    }
                                    DropdownMenu(expanded, { expanded = false }) {
                                        contentDependentAction.value?.invoke()
                                    }

                                }
                            }
                        } else if (currentScreen.value == Screen.Login) {
                            var expanded by remember { mutableStateOf(false) }
                            var showAboutDialog by remember { mutableStateOf(false) }
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    stringResource(R.string.menu)
                                )
                            }
                            DropdownMenu(expanded, { expanded = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.log_in_by_token)) },
                                    onClick = {
                                        modalDialogContentLive.value = { TokenLogin() }
                                        modalDialogStateLive.postValue(true)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.about)) },
                                    onClick = { showAboutDialog = true })
                            }
                            AnimatedVisibility(showAboutDialog) {
                                Dialog(
                                    onDismissRequest = { showAboutDialog = false },
                                    properties = DialogProperties(
                                        usePlatformDefaultWidth = false
                                    )
                                ) {
                                    Surface(Modifier.fillMaxSize()) {
                                        Column(Modifier.verticalScroll(rememberScrollState())) {
                                            IconButton(
                                                onClick = { showAboutDialog = false },
                                                Modifier.padding(8.dp)
                                            ) {
                                                Icon(
                                                    Icons.AutoMirrored.Default.ArrowBack,
                                                    stringResource(R.string.back)
                                                )
                                            }
                                            About()
                                        }
                                    }
                                }
                            }
                        }
                    }, colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = topAppBarColor
                    )
                )
                if (BuildConfig.DEBUG) {
                    Log.d("Performance", "TopAppBar rendering completed in ${System.currentTimeMillis() - topAppBarStartTime}ms")
                }
            }
        }, snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, bottomBar = {
                if ((currentScreen.value != Screen.MainNav) || !localLoadedState) return@Scaffold
                val navBarStartTime = System.currentTimeMillis()
                NavigationBar {
                    val navBackStackEntry by navController.value!!.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    Log.d("Performance", "NavigationBar rendering started")
                    NavSection.values().forEach {
                        val selected =
                            currentDestination?.hierarchy?.any { destination -> destination.route == it.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                val clickStart = System.currentTimeMillis()
                                if (it == NavSection.Homeworks) {
                                    showFilterLive.postValue(true)
                                } else {
                                    showFilterLive.postValue(false)
                                }
                                val navigationStart = System.currentTimeMillis()
                                navController.value!!.navigate(it.route) {
                                    popUpTo(navController.value!!.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                                if (BuildConfig.DEBUG) {
                                    Log.d("Performance", "Navigation to ${it.route} completed in ${System.currentTimeMillis() - clickStart}ms - total nav time: ${System.currentTimeMillis() - navigationStart}ms")
                                }
                            },
                            icon = {
                                Icon(it.icon, stringResource(id = it.title))
                            },
                            label = {
                                Text(stringResource(id = it.title))
                            })
                    }
                }
                if (BuildConfig.DEBUG) {
                    Log.d("Performance", "NavigationBar rendering completed in ${System.currentTimeMillis() - navBarStartTime}ms")
                }
            }) { padding ->
                Surface {
                    title = when (currentScreen.value!!) {
                        Screen.Login -> {
                            LoginScreen(Modifier.padding(padding))
                            R.string.log_in
                        }

                        Screen.Callback -> {
                            val uri = Uri.parse(intentData)
                            val callbackType =
                                CallbackType.values().firstOrNull { it.host == uri.host }
                            val code = uri.getQueryParameter("code")
                            val subsystem = uri.getQueryParameter("system")?.toIntOrNull()
                            if (code != null && callbackType != null) {
                                CallbackScreen(code, callbackType, subsystem)
                            } else {
                                screenLive.postValue(Screen.Login)
                            }
                            R.string.log_in
                        }

                        Screen.MainNav -> {
                            val screenStartTime = System.currentTimeMillis()
                            NavScreen(Modifier.padding(padding), pinFinished)
                            if (BuildConfig.DEBUG) {
                                if (BuildConfig.DEBUG) {
                                    Log.d("Performance", "NavScreen rendered in ${System.currentTimeMillis() - screenStartTime}ms")
                                }
                            }
                            val mainNavBackStackEntry = navController.value?.currentBackStackEntryAsState()
                            val mainCurrentRoute = mainNavBackStackEntry?.value?.destination?.route
                            NavSection.values().firstOrNull { it.route == mainCurrentRoute }?.title ?: R.string.app_name
                        }
                        else -> {
                            val screenStartTime = System.currentTimeMillis()
                            NavScreen(Modifier.padding(padding), pinFinished)
                            Log.d("Performance", "NavScreen rendered in ${System.currentTimeMillis() - screenStartTime}ms")
                            val elseNavBackStackEntry = navController.value?.currentBackStackEntryAsState()
                            val elseCurrentRoute = elseNavBackStackEntry?.value?.destination?.route
                            NavSection.values().firstOrNull { it.route == elseCurrentRoute }?.title ?: R.string.app_name
                        }
                    }
                }
                if (showBottomSheet == true) {
                    ModalBottomSheet(
                        onDismissRequest = { modalBottomSheetStateLive.postValue(false) },
                        sheetState = sheetState
                    ) {
                        bottomSheetContent?.invoke()
                    }
                }
                if (showDialog.value == true) {
                    Dialog(onDismissRequest = {
                        modalDialogStateLive.postValue(false)
                        modalDialogCloseListenerLive.value?.invoke()
                        modalDialogCloseListenerLive.value = {}
                    }) {
                        Card(
                            Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            dialogContent.value?.invoke()
                        }
                    }
                }
                AnimatedVisibility(visible = settingsShown) {
                    SettingsDialog { settingsShown = false }
                }

                if (BuildConfig.DEBUG) {
                    Log.d("Performance", "MyApp composition completed in ${System.currentTimeMillis() - startTime}ms")
                    LaunchedEffect(Unit) {
                        Log.d("Performance", "MyApp recomposed")
                    }
                }
            }
        }
    }

    @Preview(
        name = "Not Night", locale = "ru"
    )
    @Preview(
        name = "Night", uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "ru"
    )
    @Composable
    fun AppPreview() {
        OctoDiaryTheme {
            MyApp()
        }
    }
}



