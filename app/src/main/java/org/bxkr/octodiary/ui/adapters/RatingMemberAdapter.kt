package org.bxkr.octodiary.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.bxkr.octodiary.databinding.ItemRatingMemberBinding
import org.bxkr.octodiary.models.rating.RankingPlaces

class RatingMemberAdapter(private val context: Context, private val members: List<RankingPlaces>) :
    RecyclerView.Adapter<RatingMemberAdapter.RatingMemberViewHolder>() {

    class RatingMemberViewHolder(ratingMemberBinding: ItemRatingMemberBinding) :
        RecyclerView.ViewHolder(ratingMemberBinding.root) {
        private val binding = ratingMemberBinding
        fun bind(member: RankingPlaces, itemCount: Int) {
            with(binding) {
                place.text = member.place.toString()
                averageMark.text = member.averageMark
                progressBar.max = itemCount
                progressBar.progress = itemCount - member.place + 1
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingMemberViewHolder {
        val binding = ItemRatingMemberBinding.inflate(LayoutInflater.from(context), parent, false)
        return RatingMemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RatingMemberViewHolder, position: Int) {
        val member = members[position]
        holder.bind(member, itemCount)
    }

    override fun getItemCount(): Int = members.size
}