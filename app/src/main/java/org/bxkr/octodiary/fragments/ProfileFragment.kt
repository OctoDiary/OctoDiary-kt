package org.bxkr.octodiary.fragments

import android.content.SharedPreferences
import android.icu.text.MessageFormat
import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceManager
import com.squareup.picasso.Picasso
import org.bxkr.octodiary.MainActivity
import org.bxkr.octodiary.databinding.FragmentProfileBinding
import org.bxkr.octodiary.models.diary.Week
import org.bxkr.octodiary.models.rating.RatingClass
import org.bxkr.octodiary.models.user.User
import java.util.Calendar
import java.util.Locale


class ProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {

    private lateinit var userData: User
    private lateinit var diaryData: List<Week>
    private lateinit var ratingData: RatingClass

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as MainActivity).title = getString(org.bxkr.octodiary.R.string.profile)
        super.onViewCreated(view, savedInstanceState)
        userData = (activity as MainActivity).userData!!
        diaryData = (activity as MainActivity).diaryData!!
        ratingData = (activity as MainActivity).ratingData!!
        configureProfile()
        binding.swipeRefresh.setOnRefreshListener {
            (activity as MainActivity).createDiary { binding.swipeRefresh.isRefreshing = false }
        }
    }

    private fun configureProfile() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(activity as MainActivity)
        refreshPreferences(preferences)
        binding.studentName.text =
            getString(
                org.bxkr.octodiary.R.string.profile_name,
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
                getString(org.bxkr.octodiary.R.string.lessons_tomorrow_template),
                diaryData[1].days[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1].lessons.size
            )
        }

        if (showRating) {
            binding.ratingCard.visibility = View.VISIBLE
            binding.ratingStatus.text =
                getString(
                    org.bxkr.octodiary.R.string.rating_place,
                    toOrdinal(ratingData.history.rankingPosition.place).replace(".", "")
                )
            if (showRatingBackground) {
                binding.ratingBackground.visibility = View.VISIBLE
                Picasso.get().load(ratingData.history.rankingPosition.backgroundImageUrl)
                    .into(binding.ratingBackground)
            } else binding.ratingBackground.visibility = View.INVISIBLE
        } else binding.ratingCard.visibility = View.GONE
    }

    private fun toOrdinal(place: Int): String {
        val formatter = MessageFormat("{0,ordinal}", Locale.getDefault())
        return formatter.format(arrayOf(place))
    }
}