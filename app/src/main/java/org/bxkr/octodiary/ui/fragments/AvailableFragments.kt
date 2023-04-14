package org.bxkr.octodiary.ui.fragments

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import org.bxkr.octodiary.R

enum class AvailableFragments(
    @IdRes val menuId: Int,
    val instance: Fragment,
    val preferencesName: String,
    @StringRes val activityTitle: Int
) {
    Diary(R.id.diaryPage, DiaryFragment(), "diary", R.string.diary),
    Dashboard(R.id.dashboardPage, DashboardFragment(), "dashboard", R.string.dashboard),
    PeriodMarks(R.id.periodMarksPage, PeriodMarksFragment(), "period_marks", R.string.period_marks),
    Profile(R.id.profilePage, ProfileFragment(), "profile", R.string.profile),
//    Chats(R.id.chatsPage, ChatListFragment(), "chats", R.string.chats)
}