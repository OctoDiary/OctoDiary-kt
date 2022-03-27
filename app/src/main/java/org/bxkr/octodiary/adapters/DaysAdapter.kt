package org.bxkr.octodiary.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.bxkr.octodiary.databinding.DaysRecyclerItemBinding
import org.bxkr.octodiary.network.NetworkService
import java.text.SimpleDateFormat

class DaysAdapter(private val context: Context, private val days: List<NetworkService.Day>) :
    RecyclerView.Adapter<DaysAdapter.DaysViewHolder>() {

    class DaysViewHolder(daysRecyclerItemBinding: DaysRecyclerItemBinding, context: Context) :
        RecyclerView.ViewHolder(daysRecyclerItemBinding.root) {
        private val binding = daysRecyclerItemBinding
        private val parentContext = context
        fun bind(day: NetworkService.Day) {
            val toDate = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                parentContext.resources.configuration.locales[0]
            )
            val toCommon =
                SimpleDateFormat("dd MMMM", parentContext.resources.configuration.locales[0])
            binding.date.text = toDate.parse(day.date)?.let { toCommon.format(it) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DaysViewHolder {
        val binding = DaysRecyclerItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return DaysViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: DaysViewHolder, positon: Int) {
        val day = days[positon]
        holder.bind(day)
    }

    override fun getItemCount(): Int = days.size
}