package org.bxkr.octodiary.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.bxkr.octodiary.databinding.ItemGeneralFeedBinding
import org.bxkr.octodiary.models.userfeed.PeriodMark
import org.bxkr.octodiary.models.userfeed.UsedFeedTypes
import org.bxkr.octodiary.ui.activities.MainActivity

class UserFeedAdapter(
    private val context: Context, private val feeds: List<PeriodMark>
) : RecyclerView.Adapter<UserFeedAdapter.UserFeedViewHolder>() {

    class UserFeedViewHolder(
        val binding: ItemGeneralFeedBinding, val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(feed: PeriodMark) {
            UsedFeedTypes.valueOf(feed.type.name).bind(feed, (context as MainActivity), binding)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserFeedViewHolder {
        val binding =
            ItemGeneralFeedBinding.inflate(LayoutInflater.from(context), parent, false)
        return UserFeedViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: UserFeedViewHolder, position: Int) {
        val feed = feeds[position]
        holder.bind(feed)
    }

    override fun getItemCount(): Int = feeds.size
}