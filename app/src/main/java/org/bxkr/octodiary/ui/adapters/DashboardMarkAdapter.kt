package org.bxkr.octodiary.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.bxkr.octodiary.R
import org.bxkr.octodiary.Utils
import org.bxkr.octodiary.databinding.ItemDashboardMarkBinding
import org.bxkr.octodiary.models.shared.Mark
import org.bxkr.octodiary.models.userfeed.RecentMark
import org.bxkr.octodiary.ui.activities.MainActivity
import org.bxkr.octodiary.ui.activities.MarkActivity
import java.util.Date

class DashboardMarkAdapter(
    private val context: Context, private val marks: List<RecentMark>
) : RecyclerView.Adapter<DashboardMarkAdapter.DashboardMarkViewHolder>() {

    class DashboardMarkViewHolder(
        val binding: ItemDashboardMarkBinding, val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(mark: RecentMark) {
            binding.let {
                with(mark) {
                    if (mark.marks.size == 2) {
                        context.getString(
                            R.string.fractional_mark,
                            marks[0].value,
                            marks[1].value
                        )
                    } else {
                        marks[0].value
                    }.also { it1 -> it.markValue.text = it1 }
                    it.markLessonName.text = subject.name
                    var shortText = shortMarkTypeText
                    if (shortText == "Label.EduSchool.WorkType.PeriodMark.Short") {
                        shortText = "ПЕР"
                    }
                    it.markTypeAndDate.text = context.getString(
                        R.string.mark_type_and_date,
                        shortText,
                        Utils.toPatternedDate(
                            "MMM d",
                            Date(this.date * 1000L),
                            context.resources.configuration.locales[0]
                        )
                    )
                    it.markCard.setOnClickListener { goToMark(marks[0]) }
                }
            }
        }

        private fun goToMark(mark: Mark) {
            val mainActivity = context as MainActivity
            val intent = Intent(mainActivity, MarkActivity::class.java)
            intent.putExtra("person_id", mainActivity.userData!!.contextPersons[0].personId)
            intent.putExtra("group_id", mainActivity.userData!!.contextPersons[0].group.id)
            intent.putExtra("mark_id", mark.id)
            mainActivity.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardMarkViewHolder {
        val binding =
            ItemDashboardMarkBinding.inflate(LayoutInflater.from(context), parent, false)
        return DashboardMarkViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: DashboardMarkViewHolder, position: Int) {
        val mark = marks[position]
        holder.bind(mark)
    }

    override fun getItemCount(): Int = marks.size
}