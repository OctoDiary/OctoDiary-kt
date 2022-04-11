package org.bxkr.octodiary.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import org.bxkr.octodiary.MainActivity
import org.bxkr.octodiary.adapters.WeeksAdapter
import org.bxkr.octodiary.databinding.FragmentDiaryBinding

class DiaryFragment : BaseFragment<FragmentDiaryBinding>(FragmentDiaryBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureRecycler()
        binding.swipeRefresh.setOnRefreshListener {
            (activity as MainActivity).createDiary()
        }
    }

    private fun configureRecycler() {
        binding.weeks.layoutManager = LinearLayoutManager(binding.root.context)
        binding.weeks.adapter =
            WeeksAdapter(binding.root.context, (activity as MainActivity).diaryData!!)
    }
}