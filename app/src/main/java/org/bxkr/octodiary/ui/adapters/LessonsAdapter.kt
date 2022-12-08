package org.bxkr.octodiary.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.ItemLessonsRecyclerBinding
import org.bxkr.octodiary.models.diary.Lesson
import org.bxkr.octodiary.models.diary.Mark
import org.bxkr.octodiary.ui.activities.LessonActivity
import org.bxkr.octodiary.ui.activities.MainActivity
import java.text.SimpleDateFormat

class LessonsAdapter(
    private val context: Context,
    private val lessons: List<Lesson>
) :
    RecyclerView.Adapter<LessonsAdapter.LessonsViewHolder>() {

    class LessonsViewHolder(
        ItemLessonsRecyclerBinding: ItemLessonsRecyclerBinding,
        context: Context
    ) :
        RecyclerView.ViewHolder(ItemLessonsRecyclerBinding.root) {
        private val binding = ItemLessonsRecyclerBinding
        private val parentContext = context
        fun bind(lesson: Lesson) {
            val toDate = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                parentContext.resources.configuration.locales[0]
            )
            val toCommon =
                SimpleDateFormat("HH:mm", parentContext.resources.configuration.locales[0])
            binding.lessonName.text = lesson.subject.name
            binding.lessonTime.text = parentContext.getString(
                R.string.time_from_to,
                toDate.parse(lesson.startDateTime)?.let { toCommon.format(it) },
                toDate.parse(lesson.endDateTime)?.let { toCommon.format(it) })
            val description: String? =
                when (PreferenceManager.getDefaultSharedPreferences(parentContext as MainActivity)
                    .getString("lesson_description", "homework")) {
                    "lesson_topic" -> lesson.theme
                    else -> lesson.homework?.text
                }
            if (description != null) {
                binding.lessonDesc.text = description
                if (lesson.hasAttachment) {
                    binding.lessonDesc.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        AppCompatResources.getDrawable(
                            parentContext,
                            R.drawable.ic_round_attachment_24
                        ),
                        null
                    )
                }
            } else {
                binding.lessonDesc.visibility = View.GONE
            }
            binding.root.setOnClickListener {
                val intent = Intent(parentContext, LessonActivity::class.java)
                intent.putExtra("lesson_data", Gson().toJson(lesson))
                intent.putExtra("person_id", parentContext.userData?.info?.personId)
                parentContext.userData?.contextPersons?.get(0)?.group?.let { it1 ->
                    intent.putExtra(
                        "group_id",
                        it1.id
                    )
                }
                parentContext.startActivity(intent)
            }

            val marks = mutableListOf<Mark>()
            for (workMark in lesson.workMarks) {
                marks.addAll(workMark.marks)
            }
            if (marks.size > 0) {
                binding.markRecyclerView.visibility = View.VISIBLE
                binding.markRecyclerView.layoutManager =
                    LinearLayoutManager(parentContext, LinearLayoutManager.HORIZONTAL, false)
                binding.markRecyclerView.adapter = MarkDiaryAdapter(parentContext, marks)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonsViewHolder {
        val binding =
            ItemLessonsRecyclerBinding.inflate(LayoutInflater.from(context), parent, false)
        return LessonsViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: LessonsViewHolder, position: Int) {
        val lesson = lessons[position]
        holder.bind(lesson)
    }

    override fun getItemCount(): Int = lessons.size
}