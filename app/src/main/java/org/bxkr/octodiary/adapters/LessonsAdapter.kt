package org.bxkr.octodiary.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.LessonsRecyclerItemBinding
import org.bxkr.octodiary.models.diary.Lesson
import java.text.SimpleDateFormat

class LessonsAdapter(
    private val context: Context,
    private val lessons: List<Lesson>
) :
    RecyclerView.Adapter<LessonsAdapter.LessonsViewHolder>() {

    class LessonsViewHolder(
        lessonsRecyclerItemBinding: LessonsRecyclerItemBinding,
        context: Context
    ) :
        RecyclerView.ViewHolder(lessonsRecyclerItemBinding.root) {
        private val binding = lessonsRecyclerItemBinding
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
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonsViewHolder {
        val binding =
            LessonsRecyclerItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return LessonsViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: LessonsViewHolder, positon: Int) {
        val lesson = lessons[positon]
        holder.bind(lesson)
    }

    override fun getItemCount(): Int = lessons.size
}