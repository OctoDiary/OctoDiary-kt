package org.bxkr.octodiary


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.bxkr.octodiary.screens.navsections.access.AccessScreen
import org.bxkr.octodiary.screens.navsections.dashboard.DashboardScreen
import org.bxkr.octodiary.screens.navsections.daybook.ScheduleScreen
import org.bxkr.octodiary.screens.navsections.homeworks.HomeworksScreen
import org.bxkr.octodiary.screens.navsections.marks.MarksScreen
import org.bxkr.octodiary.screens.navsections.profile.ProfileScreen2

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Callback : Screen("callback")
    object MainNav : Screen("main_nav")
    object AiDashboard : Screen("ai_dashboard")
    object VocabularySmartScreen : Screen("vocabulary_smart")
    object LectureNotesScreen : Screen("lecture_notes")
    object LectureDetailScreen : Screen("lecture_detail")
    object TextbooksScreen : Screen("textbooks")
    object TextbookExtractorScreen : Screen("textbook_extractor")
    object WebViewScreen : Screen("webview")
}

enum class NavSection(
    val icon: ImageVector,
    @StringRes val title: Int,
    val route: String,
    val composable: @Composable () -> Unit
) {
    Daybook(Icons.AutoMirrored.Default.MenuBook, R.string.diary, "daybook", { ScheduleScreen() }),
    Homeworks(Icons.Default.HomeWork, R.string.homeworks, "homeworks", { HomeworksScreen() }),
    Dashboard(Icons.Default.Dashboard, R.string.dashboard, "dashboard", { DashboardScreen() }),
    Marks(Icons.AutoMirrored.Default.TrendingUp, R.string.marks, "marks", { MarksScreen() }),
    Access(Icons.Default.Nfc, R.string.access, "access", { AccessScreen() }),
    Profile(Icons.Default.Person, R.string.profile, "profile", { ProfileScreen2() })
}


