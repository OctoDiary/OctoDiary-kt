package org.bxkr.octodiary.ui.adapters

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.animation.doOnEnd
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.ItemRatingMemberBinding
import org.bxkr.octodiary.models.rating.RankingPlaces

class RatingMemberAdapter(private val context: Context, private val members: List<RankingPlaces>) :
    RecyclerView.Adapter<RatingMemberAdapter.RatingMemberViewHolder>() {
    var animated = false

    class RatingMemberViewHolder(
        ratingMemberBinding: ItemRatingMemberBinding,
        context: Context,
        private val adapter: RatingMemberAdapter
    ) :
        RecyclerView.ViewHolder(ratingMemberBinding.root) {
        private val parentContext = context
        private val binding = ratingMemberBinding
        fun bind(member: RankingPlaces, itemCount: Int) {
            with(binding) {
                place.text = member.place.toString()
                averageMark.text = member.averageMark
                progressBar.max = itemCount * 100
                if (!adapter.animated) {
                    val animator = ObjectAnimator.ofInt(
                        progressBar,
                        "progress",
                        (itemCount - member.place + 1) * 100
                    )
                        .setDuration(800)
                    animator.doOnEnd { adapter.animated = true }
                    animator.start()
                } else {
                    progressBar.progress = (itemCount - member.place + 1) * 100
                }
                val preferences = PreferenceManager.getDefaultSharedPreferences(parentContext)
                if (preferences.getBoolean("show_rating_images", true)) {
                    if (!member.imageUrl.isNullOrEmpty()) {
                        Picasso.get().load(member.imageUrl).into(avatar)
                        avatar.imageTintList = null
                    } else {
                        avatar.setImageDrawable(
                            AppCompatResources.getDrawable(
                                parentContext,
                                R.drawable.ic_round_person_24
                            )
                        )
                        val secondaryColor = TypedValue()
                        parentContext.theme.resolveAttribute(
                            com.google.android.material.R.attr.colorSecondary,
                            secondaryColor, true
                        )
                        avatar.imageTintList = ColorStateList.valueOf(secondaryColor.data)
                    }
                    if (member.isContextUser) {
                        card.strokeWidth =
                            parentContext.resources.getDimensionPixelSize(R.dimen.card_stroke_width)
                    } else {
                        card.strokeWidth = 0
                    }
                } else avatar.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingMemberViewHolder {
        val binding = ItemRatingMemberBinding.inflate(LayoutInflater.from(context), parent, false)
        return RatingMemberViewHolder(binding, context, this)
    }

    override fun onBindViewHolder(holder: RatingMemberViewHolder, position: Int) {
        val member = members[position]
        holder.bind(member, itemCount)
    }

    override fun getItemCount(): Int = members.size
}