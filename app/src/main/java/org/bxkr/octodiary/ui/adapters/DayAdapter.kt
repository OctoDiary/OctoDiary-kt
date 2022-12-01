package org.bxkr.octodiary.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.ItemDayBinding
import org.bxkr.octodiary.models.diary.Day
import java.text.SimpleDateFormat

class DayAdapter(private val context: Context, private val days: List<Day>) :
    RecyclerView.Adapter<DayAdapter.DaysViewHolder>() {

    class DaysViewHolder(daysRecyclerItemBinding: ItemDayBinding, context: Context) :
        RecyclerView.ViewHolder(daysRecyclerItemBinding.root) {
        private val binding = daysRecyclerItemBinding
        private val parentContext = context
        fun bind(day: Day) {
            val toDate = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                parentContext.resources.configuration.locales[0]
            )
            val toCommon =
                SimpleDateFormat("dd MMMM", parentContext.resources.configuration.locales[0])
            val toWeekday =
                SimpleDateFormat("EEEE", parentContext.resources.configuration.locales[0])
            binding.date.text = parentContext.getString(
                R.string.date_weekday,
                toDate.parse(day.date)?.let { toCommon.format(it) },
                toDate.parse(day.date)?.let { toWeekday.format(it) })
            binding.lessonsRecycler.layoutManager = LinearLayoutManager(parentContext)
            binding.lessonsRecycler.adapter = LessonsAdapter(parentContext, day.lessons)
            if (day.lessons.isEmpty()) {
                binding.freeDay.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DaysViewHolder {
        val binding = ItemDayBinding.inflate(LayoutInflater.from(context), parent, false)
        return DaysViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: DaysViewHolder, position: Int) {
        val day = days[position]
        holder.bind(day)
    }

    override fun getItemCount(): Int = days.size
}