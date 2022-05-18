package org.bxkr.octodiary.fragments

import android.icu.text.MessageFormat
import android.os.Bundle
import android.view.View
import com.squareup.picasso.Picasso
import org.bxkr.octodiary.MainActivity
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.FragmentProfileBinding
import org.bxkr.octodiary.models.user.User
import java.util.*

class ProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {

    private lateinit var userData: User

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userData = (activity as MainActivity).userData!!
        configureProfile()
        binding.swipeRefresh.setOnRefreshListener {
            (activity as MainActivity).createDiary { binding.swipeRefresh.isRefreshing = false }
        }
    }

    private fun configureProfile() {
        binding.studentName.text =
            getString(R.string.profile_name, userData.firstName, userData.lastName)
        binding.schoolName.text = userData.schoolName
        binding.ratingStatus.text =
            getString(
                R.string.rating_place,
                toOrdinal(userData.ranking.history.rankingPosition.place).replace(".", "")
            )
        Picasso.get().load(userData.avatarUrl).into(binding.bigAvatar)
        Picasso.get().load(userData.ranking.history.rankingPosition.backgroundImageUrl)
            .into(binding.ratingBackground)
    }

    private fun toOrdinal(place: Int): String {
        val formatter = MessageFormat("{0,ordinal}", Locale.getDefault())
        return formatter.format(arrayOf(place))
    }
}