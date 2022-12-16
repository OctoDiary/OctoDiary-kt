package org.bxkr.octodiary.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceManager
import com.squareup.picasso.Picasso
import org.bxkr.octodiary.R
import org.bxkr.octodiary.Utils
import org.bxkr.octodiary.Utils.toOrdinal
import org.bxkr.octodiary.databinding.FragmentDashboardBinding
import org.bxkr.octodiary.models.diary.Week
import org.bxkr.octodiary.models.rating.RatingClass
import org.bxkr.octodiary.ui.activities.MainActivity
import org.bxkr.octodiary.ui.dialogs.RatingBottomSheet

class DashboardFragment :
    BaseFragment<FragmentDashboardBinding>(FragmentDashboardBinding::inflate) {

    private lateinit var mainActivity: MainActivity
    private lateinit var preferences: SharedPreferences
    private lateinit var TODO: Any // todo
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
            TODO = mainActivity.userData ?: return // todo
            diaryData = mainActivity.diaryData ?: return
            ratingData = mainActivity.ratingData ?: return

            configureMarks()
            configureMiniDiary()
            configureRating()
        }
    }

    private fun configureMarks() {
        // todo
    }

    private fun configureMiniDiary() {
        // todo
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