package org.bxkr.octodiary.ui.adapters

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.ItemMarkRatingMemberBinding
import org.bxkr.octodiary.models.mark.Category

class MarkRatingMemberAdapter(
    private val context: Context,
    private val members: List<Category>,
    private val fractional: Boolean
) :
    RecyclerView.Adapter<MarkRatingMemberAdapter.MarkRatingMemberViewHolder>() {

    class MarkRatingMemberViewHolder(
        ratingMemberBinding: ItemMarkRatingMemberBinding,
        context: Context,
        val fractional: Boolean
    ) :
        RecyclerView.ViewHolder(ratingMemberBinding.root) {
        private val parentContext = context
        private val binding = ratingMemberBinding
        fun bind(member: Category) {
            binding.let {
                it.markRatingValue.text = member.value
                if (fractional) {
                    val spannable = SpannableString(
                        parentContext.getString(
                            R.string.fractional_mark_rating_value,
                            member.value,
                            (member.markNumber + 1).toString()
                        )
                    )
                    spannable.setSpan(
                        RelativeSizeSpan(0.5f),
                        1,
                        2,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    it.markRatingValue.text = spannable
                }
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
        return MarkRatingMemberViewHolder(binding, context, fractional)
    }

    override fun onBindViewHolder(holder: MarkRatingMemberViewHolder, position: Int) {
        val member = members[position]
        holder.bind(member)
    }

    override fun getItemCount(): Int = members.size
}