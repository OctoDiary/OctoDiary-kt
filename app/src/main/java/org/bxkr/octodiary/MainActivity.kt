package org.bxkr.octodiary

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.bxkr.octodiary.components.ProfileChooser
import org.bxkr.octodiary.components.SettingsDialog
import org.bxkr.octodiary.screens.CallbackScreen
import org.bxkr.octodiary.screens.CallbackType
import org.bxkr.octodiary.screens.LoginScreen
import org.bxkr.octodiary.screens.NavScreen
import org.bxkr.octodiary.ui.theme.CustomColorScheme
import org.bxkr.octodiary.ui.theme.OctoDiaryTheme
import java.util.UUID

val modalBottomSheetStateLive = MutableLiveData(false)
val modalBottomSheetContentLive = MutableLiveData<@Composable () -> Unit> {}
val snackbarHostStateLive = MutableLiveData(SnackbarHostState())
val navControllerLive = MutableLiveData<NavHostController?>(null)
val showFilterLive = MutableLiveData(false)
val contentDependentActionLive = MutableLiveData<@Composable () -> Unit> {}
val contentDependentActionIconLive = MutableLiveData(Icons.Rounded.FilterAlt)
val screenLive = MutableLiveData<Screen>()
val modalDialogStateLive = MutableLiveData(false)
val modalDialogContentLive = MutableLiveData<@Composable () -> Unit> {}
val reloadEverythingLive = MutableLiveData {}
val darkThemeLive = MutableLiveData<Boolean>(null)
val colorSchemeLive = MutableLiveData(-1)
val launchUrlLive = MutableLiveData<Uri?>(null)
val LocalActivity = staticCompositionLocalOf<FragmentActivity> {
    error("No LocalActivity provided!")
}

class MainActivity : FragmentActivity() {
    private fun createNotificationChannel() {
        val name = getString(R.string.data_update_channel_name)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("data_update", name, importance)
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        if (mainPrefs.get<String>("deviceId") == null) {
            mainPrefs.save("deviceId" to UUID.randomUUID().toString())
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
            AnimatedContent(targetState = darkTheme to colorScheme, label = "theme_anim") {
                val currentScheme = when {
                    it.second == -1 -> CustomColorScheme.Yellow
                    else -> CustomColorScheme.values()[it.second]
                }
                OctoDiaryTheme(
                    it.first,
                    colorScheme == -1,
                    currentScheme.lightColorScheme,
                    currentScheme.darkColorScheme
                ) {
                    MyApp(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MyApp(
        modifier: Modifier = Modifier
    ) {
        var title by rememberSaveable { mutableIntStateOf(R.string.app_name) }
        screenLive.value = if (authPrefs.get<Boolean>("auth") == true) {
            Screen.MainNav
        } else Screen.Login
        val pinFinished = remember { mutableStateOf(false) }
        val currentScreen = screenLive.observeAsState()
        val showBottomSheet by modalBottomSheetStateLive.observeAsState()
        val bottomSheetContent by modalBottomSheetContentLive.observeAsState()
        val sheetState = rememberModalBottomSheetState()
        val snackbarHostState = snackbarHostStateLive.value!!
        if (navControllerLive.value == null) {
            navControllerLive.value = rememberNavController()
        }
        val navController = navControllerLive.observeAsState()
        val surfaceColor = MaterialTheme.colorScheme.surface
        val elevatedColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        var topAppBarColor by remember { mutableStateOf(surfaceColor) }
        val contentDependentAction = contentDependentActionLive.observeAsState()
        val showDialog = modalDialogStateLive.observeAsState()
        val dialogContent = modalDialogContentLive.observeAsState()
        val showFilter = showFilterLive.observeAsState(false)
        val launchUrl = launchUrlLive.observeAsState()


        if (launchUrl.value != null) {
            val tabIntent = CustomTabsIntent.Builder().build()
            tabIntent.launchUrl(LocalContext.current, launchUrl.value!!)
            launchUrlLive.postValue(null)
        }

        SideEffect {
            navController.value?.addOnDestinationChangedListener { _, destination, _ ->
                topAppBarColor =
                    (if (destination.route == NavSection.Daybook.route) elevatedColor else surfaceColor)
            }
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
                    TopAppBar(title = {
                        AnimatedContent(targetState = title, label = "title_anim") {
                            if ((currentScreen.value == Screen.MainNav && localLoadedState) || currentScreen.value != Screen.MainNav) {
                                Text(stringResource(it))
                            } else {
                                Text(stringResource(R.string.app_name))
                            }
                        }
                    }, actions = {
                        if (localLoadedState && currentScreen.value == Screen.MainNav) {
                            val currentRoute =
                                navController.value!!.currentBackStackEntryAsState().value?.destination?.route
                            AnimatedVisibility(currentRoute == NavSection.Profile.route) {
                                Row(Modifier) {
                                    IconButton(onClick = {
                                        modalDialogContentLive.value = { ProfileChooser() }
                                        modalDialogStateLive.postValue(true)
                                    }) {
                                        Icon(
                                            Icons.Rounded.Groups,
                                            stringResource(id = R.string.choose_context_profile)
                                        )
                                    }
                                    IconButton(onClick = { settingsShown = true }) {
                                        Icon(
                                            Icons.Rounded.Settings,
                                            stringResource(id = R.string.settings)
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
                                        contentDependentActionIconLive.observeAsState(Icons.Rounded.FilterAlt)
                                    IconButton(onClick = { expanded = !expanded }) {
                                        AnimatedContent(
                                            targetState = icon.value,
                                            label = "action_icon_anim"
                                        ) {
                                            Icon(it, "action")
                                        }
                                    }
                                    DropdownMenu(expanded, { expanded = false }) {
                                        contentDependentAction.value?.invoke()
                                    }

                                }
                            }
                        }
                    }, colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = topAppBarColor
                    )
                    )
                }
            }, snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, bottomBar = {
                if ((currentScreen.value != Screen.MainNav) || !localLoadedState) return@Scaffold
                NavigationBar {
                    val navBackStackEntry by navController.value!!.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    NavSection.values().forEach {
                        val selected =
                            currentDestination?.hierarchy?.any { destination -> destination.route == it.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (it == NavSection.Homeworks) {
                                    showFilterLive.postValue(true)
                                } else {
                                    showFilterLive.postValue(false)
                                }
                                navController.value!!.navigate(it.route) {
                                    popUpTo(navController.value!!.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
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
                            NavScreen(Modifier.padding(padding), pinFinished)
                            val navBackStackEntry by navController.value!!.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route
                            NavSection.values().firstOrNull { it.route == currentRoute }?.title
                                ?: R.string.app_name
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
                    Dialog(onDismissRequest = { modalDialogStateLive.postValue(false) }) {
                        Card(
                            Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            dialogContent.value?.invoke()
                        }
                    }
                }
                AnimatedVisibility(visible = settingsShown) {
                    SettingsDialog { settingsShown = false }
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