package org.bxkr.octodiary.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.ItemPeriodSubjectBinding
import org.bxkr.octodiary.models.periodmarks.PeriodMark
import org.bxkr.octodiary.models.periodmarks.PeriodType
import org.bxkr.octodiary.ui.activities.MainActivity
import kotlin.math.round
import kotlin.math.roundToInt

class PeriodAdapter(
    private val context: MainActivity,
    private val periodType: PeriodType,
    private val subject: List<PeriodMark>
) : RecyclerView.Adapter<PeriodAdapter.PeriodViewHolder>() {

    var newSubject = subject

    class PeriodViewHolder(
        val binding: ItemPeriodSubjectBinding, val context: MainActivity
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(period: PeriodMark, periodType: PeriodType) {
            binding.subjectName.text = period.subject.name
            binding.marksRecyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.marksRecyclerView.adapter = MarkAdapter(
                context,
                null,
                simpleMarks = period.recentMarks.map { it.marks[0] },
                personId = context.userData!!.contextPersons[0].personId,
                groupId = context.userData!!.contextPersons[0].group.id
            )
            if (period.averageMarks.averageMark != null) {
                try {
                    val floatMark = period.averageMarks.averageMark.toFloat()
                    binding.subjectAverage.text = context.getString(
                        R.string.period_average_extended,
                        period.averageMarks.averageMark,
                        round(floatMark).toInt().toString(),
                        context.getString(periodType.stringRes)
                    )
                } catch (exception: NumberFormatException) {
                    binding.subjectAverage.text = context.getString(
                        R.string.period_average,
                        period.averageMarks.averageMark
                    )
                }
            } else {
                binding.subjectAverage.text = context.getString(
                    R.string.period_average,
                    context.getString(R.string.not_set_yet)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeriodViewHolder {
        val binding =
            ItemPeriodSubjectBinding.inflate(LayoutInflater.from(context), parent, false)
        return PeriodViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: PeriodViewHolder, position: Int) {
        val subject = newSubject[position]
        holder.bind(subject, periodType)
    }

    override fun getItemCount(): Int = newSubject.size

    @SuppressLint("NotifyDataSetChanged")
    fun onlyWithValues(shownValues: List<String>) {
        newSubject = subject.filter {
            it.averageMarks.averageMark?.toFloatOrNull()?.roundToInt().toString() in shownValues
        }
        notifyDataSetChanged()
    }
}