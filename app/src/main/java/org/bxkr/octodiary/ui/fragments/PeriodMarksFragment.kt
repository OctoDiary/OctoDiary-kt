package org.bxkr.octodiary.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceManager
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.FragmentPeriodMarksBinding
import org.bxkr.octodiary.models.periodmarks.PeriodMarksResponse
import org.bxkr.octodiary.ui.activities.MainActivity

class PeriodMarksFragment :
    BaseFragment<FragmentPeriodMarksBinding>(FragmentPeriodMarksBinding::inflate) {
    private lateinit var mainActivity: MainActivity
    private lateinit var preferences: SharedPreferences
    private lateinit var periodMarks: PeriodMarksResponse

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = activity as MainActivity
        mainActivity.title = getString(R.string.period_marks)
        preferences = PreferenceManager.getDefaultSharedPreferences(mainActivity)
        periodMarks = mainActivity.periodMarksData!!
    }
}