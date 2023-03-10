package org.bxkr.octodiary.ui.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.bxkr.octodiary.R
import org.bxkr.octodiary.Utils.getJsonRaw
import org.bxkr.octodiary.Utils.isDemo
import org.bxkr.octodiary.Utils.toOrdinal
import org.bxkr.octodiary.databinding.ActivityLessonBinding
import org.bxkr.octodiary.databinding.ItemHomeworkAttachmentBinding
import org.bxkr.octodiary.models.lesson.Lesson
import org.bxkr.octodiary.models.shared.File
import org.bxkr.octodiary.models.shared.NamedMark
import org.bxkr.octodiary.network.BaseCallback
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.ui.adapters.ImportantWorkAdapter
import org.bxkr.octodiary.ui.adapters.MarkAdapter
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.properties.Delegates

class LessonActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLessonBinding
    private var personId by Delegates.notNull<Long>()
    private var groupId by Delegates.notNull<Long>()
    private var lessonId by Delegates.notNull<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLessonBinding.inflate(layoutInflater)

        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val prefs =
            this.getSharedPreferences(getString(R.string.auth_file_key), Context.MODE_PRIVATE)
        personId = intent.getLongExtra("person_id", 0)
        groupId = intent.getLongExtra("group_id", 0)
        lessonId = intent.getLongExtra("lesson_id", 0)

        if (isDemo(this)) configureLesson(
            getJsonRaw(resources.openRawResource(R.raw.sample_lesson_details_data))
        )
        else {
            val call = NetworkService.api(
                NetworkService.Server.values()[prefs.getInt(
                    getString(R.string.server_key),
                    0
                )]
            ).lessonDetails(
                personId,
                groupId,
                lessonId,
                prefs.getString(getString(R.string.token), null)
            )

            call.enqueue(object : BaseCallback<Lesson>(
                this,
                binding.root,
                R.string.unexpected_error,
                { response ->
                    val responseBody = response.body()
                    if (responseBody != null) configureLesson(responseBody)
                }) {})
        }
    }

    private fun configureLesson(lesson: Lesson) {
        val toCommon =
            SimpleDateFormat("HH:mm", resources.configuration.locales[0])
        with(binding) {
            bigProgressBar.visibility = View.GONE
            contentScrollView.visibility = View.VISIBLE
            if (lesson.homework != null && lesson.homework.text.isNotEmpty() && PreferenceManager.getDefaultSharedPreferences(
                    this@LessonActivity
                ).getBoolean("homework_mode", true)
            ) {
                floatingActionButton.visibility = View.VISIBLE
                floatingActionButton.setOnClickListener {
                    val toHumanDate =
                        SimpleDateFormat(
                            "d MMMM",
                            this@LessonActivity.resources.configuration.locales[0]
                        )
                    val intent = Intent(this@LessonActivity, HomeworkModeActivity::class.java)
                    intent.putExtra("lesson_name", lesson.subject.name)
                    intent.putExtra(
                        "lesson_date",
                        Date(lesson.startTime.toLong() * 1000).let { toHumanDate.format(it) })
                    intent.putExtra("homework_text", lesson.homework.text)
                    startActivity(intent)
                }
            }

            lessonName.text = lesson.subject.name
            lessonTopic.text = lesson.theme
            lessonTime.text = getString(
                R.string.time_from_to,
                Date(lesson.startTime.toLong() * 1000).let { toCommon.format(it) },
                Date(lesson.endTime.toLong() * 1000).let { toCommon.format(it) })
            if (lesson.teacher != null) {
                lessonTeacher.text = getString(
                    R.string.teacher_name_template,
                    lesson.teacher.lastName,
                    lesson.teacher.firstName,
                    lesson.teacher.middleName
                )
            } else {
                lessonTeacher.visibility = View.GONE
            }
            lessonNumber.text =
                getString(R.string.lesson_n, toOrdinal(lesson.number))
            val marks = mutableListOf<NamedMark>()
            lesson.lessonDetailsMarks.forEach {
                marks.addAll(it.marks.map { it1 ->
                    NamedMark(
                        it1.id,
                        it1.value,
                        it.markTypeText
                    )
                })
            }
            if (lesson.homework?.text == null) {
                homeworkCard.visibility = View.GONE
            } else homeworkText.text = lesson.homework.text
            if (lesson.attachments != null) {
                attachmentsRecyclerView.layoutManager =
                    LinearLayoutManager(this@LessonActivity)
                attachmentsRecyclerView.adapter =
                    AttachmentsAdapter(this@LessonActivity, lesson.attachments)
            }
            if (lesson.importantWorks.isNotEmpty()) {
                worksRecyclerView.layoutManager =
                    LinearLayoutManager(this@LessonActivity)
                worksRecyclerView.adapter =
                    ImportantWorkAdapter(
                        this@LessonActivity,
                        lesson.importantWorks.map { it.workTypeName })
            } else worksCard.visibility = View.GONE
            if (marks.isNotEmpty()) {
                marksRecyclerView.layoutManager =
                    LinearLayoutManager(this@LessonActivity)
                marksRecyclerView.adapter =
                    MarkAdapter(this@LessonActivity, marks, true, personId, groupId)
            } else marksCard.visibility = View.GONE
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
        private val attachments: List<File>
    ) :
        RecyclerView.Adapter<AttachmentsAdapter.AttachmentsViewHolder>() {

        class AttachmentsViewHolder(
            attachmentBinding: ItemHomeworkAttachmentBinding,
            context: Context
        ) :
            RecyclerView.ViewHolder(attachmentBinding.root) {
            private val binding = attachmentBinding
            private val parentContext = context
            fun bind(attachment: File) {
                binding.root.text = attachment.fileName
                binding.root.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(attachment.fileLink)
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