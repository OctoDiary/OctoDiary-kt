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
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.ItemLessonsRecyclerBinding
import org.bxkr.octodiary.models.diary.Lesson
import org.bxkr.octodiary.models.shared.Mark
import org.bxkr.octodiary.ui.activities.LessonActivity
import org.bxkr.octodiary.ui.activities.MainActivity
import java.text.SimpleDateFormat

class LessonsAdapter(
    private val context: Context,
    private var lessons: List<Lesson>,
    private val compact: Boolean = false,
    private var personIdCommon: Long? = null,
    private var groupIdCommon: Long? = null
) : RecyclerView.Adapter<LessonsAdapter.LessonsViewHolder>() {

    class LessonsViewHolder(
        ItemLessonsRecyclerBinding: ItemLessonsRecyclerBinding, context: Context,
        private val compact: Boolean,
        private val personIdCommon: Long? = null,
        private val groupIdCommon: Long? = null
    ) : RecyclerView.ViewHolder(ItemLessonsRecyclerBinding.root) {
        private val binding = ItemLessonsRecyclerBinding
        private val parentContext = context
        fun bind(lesson: Lesson) {
            val toDate = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'", parentContext.resources.configuration.locales[0]
            )
            val toCommon =
                SimpleDateFormat("HH:mm", parentContext.resources.configuration.locales[0])
            binding.lessonName.text = lesson.subject.name
            if (lesson.startDateTime != null && lesson.endDateTime != null) {
                binding.lessonTime.text = parentContext.getString(R.string.time_from_to,
                    toDate.parse(lesson.startDateTime)?.let { toCommon.format(it) },
                    toDate.parse(lesson.endDateTime)?.let { toCommon.format(it) })
            }
            if (compact) {
                binding.lessonDesc.visibility = View.GONE
            } else {
                val description: String? =
                    when (PreferenceManager.getDefaultSharedPreferences(parentContext)
                        .getString("lesson_description", "homework")) {
                        "lesson_topic" -> lesson.theme
                        else -> lesson.homework?.text
                    }
                if (description != null) {
                    binding.lessonDesc.text = description
                    if (lesson.hasAttachment == true) {
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
            }
            var personId = personIdCommon
            var groupId = groupIdCommon
            if (parentContext is MainActivity) {
                personId =
                    parentContext.userData?.contextPersons?.get(0)?.personId!!
                groupId = parentContext.userData?.contextPersons?.get(0)?.group?.id!!
                binding.root.setOnClickListener {
                    val intent = Intent(parentContext, LessonActivity::class.java)
                    intent.putExtra("lesson_id", lesson.id)
                    intent.putExtra("person_id", personId)
                    intent.putExtra("group_id", groupId)
                    parentContext.startActivity(intent)
                }
            } else {
                binding.lessonTime.visibility = View.GONE
                binding.root.isClickable = false
                binding.root.isFocusable = false
            }

            val marks = mutableListOf<Mark>()
            for (workMark in lesson.workMarks) {
                marks.addAll(workMark.marks)
            }
            if (marks.size > 0 && !compact) {
                binding.markRecyclerView.visibility = View.VISIBLE
                binding.markRecyclerView.layoutManager =
                    LinearLayoutManager(parentContext, LinearLayoutManager.HORIZONTAL, false)
                binding.markRecyclerView.adapter =
                    MarkAdapter(parentContext, null, false, personId!!, groupId!!, marks)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonsViewHolder {
        val binding =
            ItemLessonsRecyclerBinding.inflate(LayoutInflater.from(context), parent, false)
        return LessonsViewHolder(binding, context, compact, personIdCommon, groupIdCommon)
    }

    override fun onBindViewHolder(holder: LessonsViewHolder, position: Int) {
        val lesson = lessons[position]
        holder.bind(lesson)
    }

    override fun getItemCount(): Int = lessons.size

    fun newData(lessons: List<Lesson>) {
        this.lessons = lessons
        @Suppress("NotifyDataSetChanged")
        notifyDataSetChanged()
    }
}