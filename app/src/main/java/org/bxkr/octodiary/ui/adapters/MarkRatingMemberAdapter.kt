package org.bxkr.octodiary.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.ItemMarkRatingMemberBinding
import org.bxkr.octodiary.models.mark.Category

class MarkRatingMemberAdapter(private val context: Context, private val members: List<Category>) :
    RecyclerView.Adapter<MarkRatingMemberAdapter.MarkRatingMemberViewHolder>() {

    class MarkRatingMemberViewHolder(
        ratingMemberBinding: ItemMarkRatingMemberBinding,
        context: Context
    ) :
        RecyclerView.ViewHolder(ratingMemberBinding.root) {
        private val parentContext = context
        private val binding = ratingMemberBinding
        fun bind(member: Category) {
            binding.let {
                it.markRatingValue.text = member.value
                it.markRatingPersons.text = parentContext.resources.getQuantityString(
                    R.plurals.student_count,
                    member.studentCount,
                    member.studentCount
                )
                it.markRatingProgress.progress = member.percent.toInt()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkRatingMemberViewHolder {
        val binding =
            ItemMarkRatingMemberBinding.inflate(LayoutInflater.from(context), parent, false)
        return MarkRatingMemberViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: MarkRatingMemberViewHolder, position: Int) {
        val member = members[position]
        holder.bind(member)
    }

    override fun getItemCount(): Int = members.size
}