package org.bxkr.octodiary

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bxkr.octodiary.databinding.ActivityLessonBinding
import org.bxkr.octodiary.databinding.ItemHomeworkAttachmentBinding
import org.bxkr.octodiary.models.diary.Lesson
import org.bxkr.octodiary.models.lesson.Attachment
import java.text.SimpleDateFormat

class LessonActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLessonBinding
    private lateinit var lesson: Lesson

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLessonBinding.inflate(layoutInflater)
        lesson = Gson().fromJson(
            intent.getStringExtra("lesson_data"),
            object : TypeToken<Lesson>() {}.type
        )

        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val toDate = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            resources.configuration.locales[0]
        )
        val toCommon =
            SimpleDateFormat("HH:mm", resources.configuration.locales[0])

        with(lesson) {
            with(binding) {
                lessonName.text = subject.name
                lessonTopic.text = theme
                lessonTime.text = getString(
                    R.string.time_from_to,
                    toDate.parse(lesson.startDateTime)?.let { toCommon.format(it) },
                    toDate.parse(lesson.endDateTime)?.let { toCommon.format(it) })
                lessonTeacher.text = getString(
                    R.string.teacher_name_template,
                    teacher.lastName,
                    teacher.firstName,
                    teacher.middleName
                )
                lessonNumber.text = getString(R.string.lesson_n, number.toString())
                homeworkText.text = homework?.text
                attachmentsRecyclerView.adapter =
                    homework?.attachments?.let { AttachmentsAdapter(this@LessonActivity, it) }
            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    class AttachmentsAdapter(
        private val context: Context,
        private val attachments: List<Attachment>
    ) :
        RecyclerView.Adapter<AttachmentsAdapter.AttachmentsViewHolder>() {

        class AttachmentsViewHolder(
            attachmentBinding: ItemHomeworkAttachmentBinding,
            context: Context
        ) :
            RecyclerView.ViewHolder(attachmentBinding.root) {
            private val binding = attachmentBinding
            private val parentContext = context
            fun bind(attachment: Attachment) {
                binding.root.text = attachment.fileName
                binding.root.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(attachment.fileDownloadLink)
                    parentContext.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentsViewHolder {
            val binding =
                ItemHomeworkAttachmentBinding.inflate(LayoutInflater.from(context), parent, false)
            return AttachmentsViewHolder(binding, context)
        }

        override fun onBindViewHolder(holder: AttachmentsViewHolder, positon: Int) {
            val attachments = attachments[positon]
            holder.bind(attachments)
        }

        override fun getItemCount(): Int = attachments.size
    }
}