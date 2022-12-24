package org.bxkr.octodiary.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import org.bxkr.octodiary.R
import org.bxkr.octodiary.Utils
import org.bxkr.octodiary.Utils.toOrdinal
import org.bxkr.octodiary.databinding.FragmentDashboardBinding
import org.bxkr.octodiary.models.diary.Lesson
import org.bxkr.octodiary.models.diary.Week
import org.bxkr.octodiary.models.rating.RatingClass
import org.bxkr.octodiary.models.userfeed.UserFeed
import org.bxkr.octodiary.ui.activities.MainActivity
import org.bxkr.octodiary.ui.adapters.DashboardMarkAdapter
import org.bxkr.octodiary.ui.adapters.LessonsAdapter
import org.bxkr.octodiary.ui.dialogs.RatingBottomSheet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment :
    BaseFragment<FragmentDashboardBinding>(FragmentDashboardBinding::inflate) {

    private lateinit var mainActivity: MainActivity
    private lateinit var preferences: SharedPreferences
    private lateinit var userFeedData: UserFeed
    private lateinit var diaryData: List<Week>
    private lateinit var ratingData: RatingClass

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity = activity as MainActivity
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
            userFeedData = mainActivity.userFeedData ?: return
            diaryData = mainActivity.diaryData ?: return
            ratingData = mainActivity.ratingData ?: return

            configureMarks()
            configureMiniDiary()
            configureRating()
        }
    }

    private fun configureMarks() {
        binding.dashboardMarkRecyclerView.layoutManager =
            LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false)
        binding.dashboardMarkRecyclerView.adapter =
            DashboardMarkAdapter(mainActivity, userFeedData.recentMarks)
    }

    private fun configureMiniDiary() {
        binding.miniDiaryRecyclerView.layoutManager = LinearLayoutManager(mainActivity)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val tomorrow =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(
                dateFormat.parse(dateFormat.format(Date(System.currentTimeMillis() + 86400000L)))!!
            )
        var tomorrowPosition: Int? = null
        for ((index, day) in diaryData[1].days.withIndex()) {
            if (day.date == tomorrow) {
                tomorrowPosition = index
            }
        }
        val today =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(
                dateFormat.parse(dateFormat.format(Date()))!!
            )
        var todayPosition: Int? = null
        for ((index, day) in diaryData[1].days.withIndex()) {
            if (day.date == today) {
                todayPosition = index
            }
        }
        if (tomorrowPosition == null || todayPosition == null) {
            binding.toggleButton.visibility = View.GONE
            return
        }
        val tomorrowLessons = diaryData[1].days[tomorrowPosition].lessons
        val todayLessons = diaryData[1].days[todayPosition].lessons
        binding.miniDiaryRecyclerView.adapter = LessonsAdapter(mainActivity, tomorrowLessons, true)
        if (tomorrowLessons.isEmpty()) binding.freeDay.visibility = View.VISIBLE
        val replaceFunction: (lessons: List<Lesson>) -> Unit = {
            if (it.isEmpty()) binding.freeDay.visibility = View.VISIBLE
            else {
                binding.freeDay.visibility = View.GONE
                (binding.miniDiaryRecyclerView.adapter as LessonsAdapter).newData(it)
            }
        }

        binding.toggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.todayButton -> replaceFunction(todayLessons)
                R.id.tomorrowButton -> replaceFunction(tomorrowLessons)
            }
        }
    }

    private fun configureRating() {
        val showRating = preferences.getBoolean("show_rating", true)
        val showRatingBackground = preferences.getBoolean("show_rating_background", true)

        if (showRating) {
            binding.ratingCard.visibility = View.VISIBLE
            binding.ratingStatus.text =
                getString(
                    R.string.rating_place,
                    toOrdinal(ratingData.history.rankingPosition.place)
                )
            if (showRatingBackground) {
                binding.ratingBackground.visibility = View.VISIBLE
                Picasso.get().load(ratingData.history.rankingPosition.backgroundImageUrl)
                    .into(binding.ratingBackground)
            } else binding.ratingBackground.visibility = View.INVISIBLE
            val openBottomSheet = { _: View ->
                val bottomSheet = RatingBottomSheet(ratingData.rating)
                bottomSheet.show(parentFragmentManager, null)
            }
            binding.ratingButton.setOnClickListener(openBottomSheet)
            binding.ratingCard.setOnClickListener(openBottomSheet)
        } else binding.ratingCard.visibility = View.GONE
    }
}