package org.bxkr.octodiary.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.view.setMargins
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import org.bxkr.octodiary.LessonActivity
import org.bxkr.octodiary.MainActivity
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.ItemLessonsRecyclerBinding
import org.bxkr.octodiary.models.diary.Lesson
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
            } else {
                binding.lessonDesc.visibility = View.GONE
                val newLayoutParams = RelativeLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                newLayoutParams.setMargins(parentContext.resources.getDimensionPixelSize(R.dimen.lesson_padding))
                binding.lessonName.layoutParams = newLayoutParams
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