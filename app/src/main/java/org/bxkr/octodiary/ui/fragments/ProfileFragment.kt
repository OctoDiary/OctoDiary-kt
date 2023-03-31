package org.bxkr.octodiary.ui.fragments

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import okhttp3.MediaType
import okhttp3.MultipartBody.Part
import okhttp3.RequestBody
import org.bxkr.octodiary.R
import org.bxkr.octodiary.Utils
import org.bxkr.octodiary.databinding.FragmentProfileBinding
import org.bxkr.octodiary.models.avatar.Avatar
import org.bxkr.octodiary.models.diary.Week
import org.bxkr.octodiary.models.rating.RatingClass
import org.bxkr.octodiary.models.user.User
import org.bxkr.octodiary.models.userfeed.PeriodMark
import org.bxkr.octodiary.models.userfeed.UsedFeedTypes
import org.bxkr.octodiary.models.userfeed.UserFeed
import org.bxkr.octodiary.network.BaseCallback
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.ui.activities.MainActivity
import org.bxkr.octodiary.ui.adapters.UserFeedAdapter
import java.io.File
import java.util.Calendar


class ProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {

    private lateinit var mainActivity: MainActivity
    private lateinit var userData: User
    private lateinit var diaryData: List<Week>
    private lateinit var ratingData: RatingClass
    private lateinit var userFeedData: UserFeed

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri ?: return@registerForActivityResult
            val cursor = mainActivity.contentResolver.query(uri, null, null, null)
            val result: String =
                if (cursor == null) { // Source is Dropbox or other similar local file path
                    uri.path!!
                } else {
                    cursor.moveToFirst()
                    val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    val string = cursor.getString(idx)
                    cursor.close()
                    string
                }
            val file = File(result)
            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val part = Part.createFormData("image", file.name, requestFile)
            setAvatarImage(file)

            val call = NetworkService.api(
                NetworkService.Server.values().first { it.ordinal == mainActivity.server })
                .uploadAvatar(
                    mainActivity.token,
                    mainActivity.userId!!.toLong(),
                    part
                )
            call.enqueue(object : BaseCallback<Avatar>(mainActivity, binding.root, function = {
                mainActivity.createDiary(true) {}
            }) {})
        }

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
            setAvatarImage(avatarUrl)
        } else {
            binding.bigAvatar.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        binding.bigAvatar.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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

    private fun setAvatarImage(url: Any) {
        val loaded = when (url) {
            is File -> Picasso.get().load(url)
            else -> Picasso.get().load(url as String)
        }
        loaded.transform(object : Transformation {
            override fun transform(source: Bitmap?): Bitmap = Utils.getCroppedBitmap(source!!)
            override fun key() = "circle"
        }).into(binding.bigAvatar)
    }
}