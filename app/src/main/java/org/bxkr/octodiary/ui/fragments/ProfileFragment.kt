package org.bxkr.octodiary.ui.fragments

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import org.bxkr.octodiary.R
import org.bxkr.octodiary.Utils
import org.bxkr.octodiary.databinding.FragmentProfileBinding
import org.bxkr.octodiary.models.diary.Week
import org.bxkr.octodiary.models.rating.RatingClass
import org.bxkr.octodiary.models.user.User
import org.bxkr.octodiary.models.userfeed.PeriodMark
import org.bxkr.octodiary.models.userfeed.UsedFeedTypes
import org.bxkr.octodiary.models.userfeed.UserFeed
import org.bxkr.octodiary.ui.activities.MainActivity
import org.bxkr.octodiary.ui.adapters.UserFeedAdapter
import java.util.Calendar

class ProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {

    private lateinit var mainActivity: MainActivity
    private lateinit var userData: User
    private lateinit var diaryData: List<Week>
    private lateinit var ratingData: RatingClass
    private lateinit var userFeedData: UserFeed

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainActivity = activity as MainActivity
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
            userData = mainActivity.userData ?: return
            diaryData = mainActivity.diaryData ?: return
            ratingData = mainActivity.ratingData ?: return
            userFeedData = mainActivity.userFeedData ?: return
        }

        configureProfile()
    }

    private fun configureProfile() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(mainActivity)
        refreshPreferences(preferences)
        binding.studentName.text =
            getString(
                R.string.profile_name,
                userData.contextPersons[0].firstName,
                userData.contextPersons[0].lastName
            )
        val avatarUrl = userData.contextPersons[0].avatarUrl
        if (avatarUrl?.isNotEmpty() == true) {
            Picasso.get().load(avatarUrl).transform(object : Transformation {
                override fun transform(source: Bitmap?): Bitmap = Utils.getCroppedBitmap(source!!)
                override fun key() = "circle"
            }).into(binding.bigAvatar)
        } else {
            binding.bigAvatar.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        val onlyUsedFeeds: List<PeriodMark> = userFeedData.feed.mapNotNull {
            if (it.type in UsedFeedTypes.values().map { it1 -> it1.feedType }) {
                return@mapNotNull it
            }
            return@mapNotNull null
        }
        binding.feedRecyclerView.layoutManager = LinearLayoutManager(mainActivity)
        binding.feedRecyclerView.adapter = UserFeedAdapter(mainActivity, onlyUsedFeeds)
    }

    private fun refreshPreferences(preferences: SharedPreferences) {
        when (preferences.getString("data_under_name", "school")) {
            "school" -> binding.dataUnderName.text = userData.contextPersons[0].school.name
            "class_name" -> binding.dataUnderName.text = userData.contextPersons[0].group.name
            "middle_name" -> binding.dataUnderName.text = userData.contextPersons[0].middleName
            "lessons_tomorrow" -> binding.dataUnderName.text = java.text.MessageFormat.format(
                getString(R.string.lessons_tomorrow_template),
                diaryData[1].days[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1].lessons.size
            )
        }
    }
}