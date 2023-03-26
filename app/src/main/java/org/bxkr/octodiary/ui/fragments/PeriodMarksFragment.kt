package org.bxkr.octodiary.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IdRes
import androidx.core.widget.doOnTextChanged
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import org.bxkr.octodiary.R
import org.bxkr.octodiary.Utils
import org.bxkr.octodiary.databinding.FragmentPeriodMarksBinding
import org.bxkr.octodiary.models.periodmarks.PeriodMarksResponse
import org.bxkr.octodiary.ui.activities.MainActivity
import org.bxkr.octodiary.ui.adapters.PeriodAdapter


class PeriodMarksFragment :
    BaseFragment<FragmentPeriodMarksBinding>(FragmentPeriodMarksBinding::inflate) {
    private lateinit var mainActivity: MainActivity
    private lateinit var preferences: SharedPreferences
    private lateinit var periodMarks: PeriodMarksResponse
    private var shownValues = mutableListOf("5", "4", "3", "2")

    enum class ChipsMarkTexts(val value: String, @IdRes val idRes: Int) {
        Five("5", R.id.will_be_5),
        Four("4", R.id.will_be_4),
        Three("3", R.id.will_be_3),
        Two("2", R.id.will_be_2),
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = activity as MainActivity
        mainActivity.title = getString(R.string.period_marks)
        preferences = PreferenceManager.getDefaultSharedPreferences(mainActivity)
        if (Utils.isSchoolDataOutOfDate(mainActivity)) {
            mainActivity.let { it ->
                it.binding.swipeRefresh.let { swipeRefresh ->
                    it.createDiary {
                        swipeRefresh.isRefreshing = false
                        onViewCreated(view, savedInstanceState)
                    }
                    swipeRefresh.isRefreshing = true
                    return@onViewCreated
                }
            }
        } else {
            periodMarks = mainActivity.periodMarksData!!
            configureChips()
            configureRecycler()
            if (PreferenceManager.getDefaultSharedPreferences(mainActivity)
                    .getBoolean("always_show_keyboard_in_period_marks", false)
            ) {
                binding.searchInput.requestFocus()
                val inputMethodManager =
                    mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(
                    binding.searchInput,
                    InputMethodManager.SHOW_IMPLICIT
                )
            }
        }
    }

    private fun configureChips() {
        if (periodMarks.periodMarks.map { it.recentMarks.firstOrNull()?.marks?.get(0)?.value }
                .mapNotNull { it?.toIntOrNull() }.isNotEmpty()) {
            binding.chipGroup.visibility = View.VISIBLE
            ChipsMarkTexts.values().forEach {
                binding.chipGroup.findViewById<Chip>(it.idRes).apply {
                    text = getString(R.string.period_marks_chip, it.value)
                    isChecked = true
                }
            }
            binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
                shownValues = (checkedIds.map {
                    ChipsMarkTexts.values().first { it1 -> it1.idRes == it }.value
                } as MutableList<String>)
                (binding.periodMarksRecyclerView.adapter as PeriodAdapter).updateList(shownValues)
            }
        }
    }

    private fun configureRecycler() {
        binding.periodMarksRecyclerView.layoutManager = LinearLayoutManager(mainActivity)
        binding.periodMarksRecyclerView.adapter = PeriodAdapter(
            mainActivity,
            mainActivity.userData!!.contextPersons[0].reportingPeriodGroup.periods.first { it.isCurrent }.type,
            periodMarks.periodMarks
        )
        binding.searchInput.doOnTextChanged { text, _, _, _ ->
            (binding.periodMarksRecyclerView.adapter as PeriodAdapter).updateList(text.toString())
        }
    }
}