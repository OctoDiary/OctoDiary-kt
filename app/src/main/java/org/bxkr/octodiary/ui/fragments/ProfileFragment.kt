package org.bxkr.octodiary.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceManager
import com.squareup.picasso.Picasso
import org.bxkr.octodiary.R
import org.bxkr.octodiary.Utils
import org.bxkr.octodiary.Utils.toOrdinal
import org.bxkr.octodiary.databinding.FragmentProfileBinding
import org.bxkr.octodiary.models.diary.Week
import org.bxkr.octodiary.models.rating.RatingClass
import org.bxkr.octodiary.models.user.User
import org.bxkr.octodiary.ui.activities.MainActivity
import org.bxkr.octodiary.ui.dialogs.RatingBottomSheet
import java.util.Calendar


class ProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {

    private lateinit var mainActivity: MainActivity
    private lateinit var userData: User
    private lateinit var diaryData: List<Week>
    private lateinit var ratingData: RatingClass

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainActivity = activity as MainActivity
        mainActivity.title = getString(R.string.profile)
        super.onViewCreated(view, savedInstanceState)

        if (Utils.isSchoolDataOutOfDate(mainActivity)) {
            binding.studentName.text = null
            binding.dataUnderName.text = null
            mainActivity.createDiary {
                mainActivity.binding.swipeRefresh.isRefreshing = false
                onViewCreated(view, savedInstanceState)
            }
            mainActivity.binding.swipeRefresh.isRefreshing = true
            return
        } else {
            userData = mainActivity.userData!!
            diaryData = mainActivity.diaryData!!
            ratingData = mainActivity.ratingData!!
        }

        configureProfile()
    }

    private fun configureProfile() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(mainActivity)
        refreshPreferences(preferences)
        binding.studentName.text =
            getString(
                R.string.profile_name,
                userData.info.firstName,
                userData.info.lastName
            )
        Picasso.get().load(userData.info.avatarUrl).into(binding.bigAvatar)
    }

    private fun refreshPreferences(preferences: SharedPreferences) {
        val showRating = preferences.getBoolean("show_rating", true)
        val showRatingBackground = preferences.getBoolean("show_rating_background", true)

        when (preferences.getString("data_under_name", "school")) {
            "school" -> binding.dataUnderName.text = userData.contextPersons[0].school.name
            "class_name" -> binding.dataUnderName.text = userData.contextPersons[0].group.name
            "middle_name" -> binding.dataUnderName.text = userData.info.middleName
            "lessons_tomorrow" -> binding.dataUnderName.text = java.text.MessageFormat.format(
                getString(R.string.lessons_tomorrow_template),
                diaryData[1].days[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1].lessons.size
            )
        }

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