package org.bxkr.octodiary

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.bxkr.octodiary.databinding.WeeksRecyclerItemBinding
import java.text.SimpleDateFormat


class WeeksAdapter(private val context: Context, private val weeks: List<NetworkService.Week>) :
    RecyclerView.Adapter<WeeksAdapter.WeeksViewHolder>() {

    class WeeksViewHolder(weeksRecyclerItemBinding: WeeksRecyclerItemBinding, context: Context) :
        RecyclerView.ViewHolder(weeksRecyclerItemBinding.root) {
        private val binding = weeksRecyclerItemBinding
        private val parentContext = context
        fun bind(week: NetworkService.Week) {
            val toDate = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                parentContext.resources.configuration.locales[0]
            )
            val toCommon =
                SimpleDateFormat("dd MMMM", parentContext.resources.configuration.locales[0])
            binding.weekName.text = parentContext.getString(R.string.week_n,
                toDate.parse(week.firstWeekDayDate)?.let { toCommon.format(it) },
                toDate.parse(week.lastWeekDayDate)?.let { toCommon.format(it) })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeeksViewHolder {
        val binding = WeeksRecyclerItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return WeeksViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: WeeksViewHolder, positon: Int) {
        val week = weeks[positon]
        holder.bind(week)
    }

    override fun getItemCount(): Int = weeks.size
}