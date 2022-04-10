package org.bxkr.octodiary.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import org.bxkr.octodiary.LoginActivity
import org.bxkr.octodiary.R
import org.bxkr.octodiary.adapters.WeeksAdapter
import org.bxkr.octodiary.databinding.FragmentDiaryBinding
import org.bxkr.octodiary.models.Diary
import org.bxkr.octodiary.network.NetworkService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DiaryFragment : BaseFragment<FragmentDiaryBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createDiary()
    }

    private fun createDiary() {
        val call = NetworkService.api().diary(token, userId)
        call.enqueue(object : Callback<Diary> {
            override fun onResponse(
                call: Call<Diary>, response: Response<Diary>
            ) {
                if (response.isSuccessful) {
                    val diaryData = response.body()!!.weeks
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.visibility = View.VISIBLE
                    binding.weeks.layoutManager = LinearLayoutManager(binding.root.context)
                    binding.weeks.adapter = WeeksAdapter(binding.root.context, diaryData)
                    binding.swipeRefresh.setOnRefreshListener { createDiary() }
                } else {
                    val intent = Intent(activity!!.baseContext, LoginActivity::class.java)
                    intent.putExtra(getString(R.string.out_of_date_extra), true)
                    startActivity(intent)
                    activity?.finish()
                }
                binding.swipeRefresh.isRefreshing = false
            }

            override fun onFailure(call: Call<Diary>, t: Throwable) {
                Log.e(this::class.simpleName, getString(R.string.retrofit_error))
                binding.swipeRefresh.isRefreshing = false
            }
        })
    }

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDiaryBinding {
        return FragmentDiaryBinding.inflate(inflater, container, false)
    }
}