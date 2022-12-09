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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bxkr.octodiary.R
import org.bxkr.octodiary.Utils.getJsonRaw
import org.bxkr.octodiary.Utils.isDemo
import org.bxkr.octodiary.Utils.toOrdinal
import org.bxkr.octodiary.databinding.ActivityLessonBinding
import org.bxkr.octodiary.databinding.ItemHomeworkAttachmentBinding
import org.bxkr.octodiary.models.diary.Lesson
import org.bxkr.octodiary.models.lesson.Attachments
import org.bxkr.octodiary.network.BaseCallback
import org.bxkr.octodiary.network.NetworkService
import java.text.SimpleDateFormat
import java.util.Date

class LessonActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLessonBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLessonBinding.inflate(layoutInflater)


        setContentView(binding.root)

        val prefs =
            this.getSharedPreferences(getString(R.string.auth_file_key), Context.MODE_PRIVATE)
        val personId = intent.getLongExtra("person_id", 0)
        val groupId = intent.getLongExtra("group_id", 0)

        if (isDemo(this)) {
            val lessonRaw =
                getJsonRaw<org.bxkr.octodiary.models.lesson.Lesson>(resources.openRawResource(R.raw.sample_lesson_details_data))
            binding.attachmentsRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.attachmentsRecyclerView.adapter =
                AttachmentsAdapter(this, lessonRaw.attachments)
        }


        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val toDate = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            resources.configuration.locales[0]
        )
        val toCommon =
            SimpleDateFormat("HH:mm", resources.configuration.locales[0])

        if (!intent.getBooleanExtra("full_request", false)) {
            val lesson = Gson().fromJson<Lesson>(
                intent.getStringExtra("lesson_data"),
                object : TypeToken<Lesson>() {}.type
            )

            binding.bigProgressBar.visibility = View.GONE
            binding.topicCard.visibility = View.VISIBLE
            binding.infoCard.visibility = View.VISIBLE
            binding.homeworkCard.visibility = View.VISIBLE

            if (lesson.hasAttachment) {
                binding.attachmentsProgressBar.visibility = View.VISIBLE

                val call = NetworkService.api(
                    NetworkService.Server.values()[prefs.getInt(
                        getString(R.string.server_key),
                        0
                    )]
                ).lessonDetails(
                    personId,
                    groupId,
                    lesson.id,
                    prefs.getString(getString(R.string.token), null)
                )

                call.enqueue(object : BaseCallback<org.bxkr.octodiary.models.lesson.Lesson>(
                    this,
                    binding.root,
                    R.string.unexpected_error,
                    {
                        binding.attachmentsProgressBar.visibility = View.GONE
                        binding.attachmentsRecyclerView.layoutManager = LinearLayoutManager(this)
                        binding.attachmentsRecyclerView.adapter =
                            AttachmentsAdapter(this, it.body()!!.attachments)
                    }) {})
            }

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
                    lessonNumber.text = getString(R.string.lesson_n, toOrdinal(number))
                    if (homework?.text == null) {
                        homeworkCard.visibility = View.GONE
                    } else homeworkText.text = homework.text
                }
            }
        } else {
            val lessonId = intent.getLongExtra("lesson_id", 0)
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

            call.enqueue(object : BaseCallback<org.bxkr.octodiary.models.lesson.Lesson>(
                this,
                binding.root,
                R.string.unexpected_error,
                { response ->
                    if (response.body() != null) {
                        val lesson = response.body()!!
                        with(binding) {
                            bigProgressBar.visibility = View.GONE
                            topicCard.visibility = View.VISIBLE
                            infoCard.visibility = View.VISIBLE
                            homeworkCard.visibility = View.VISIBLE
                            lessonName.text = lesson.subject.name
                            lessonTopic.text = lesson.theme
                            lessonTime.text = getString(
                                R.string.time_from_to,
                                Date(lesson.startTime.toLong() * 1000).let { toCommon.format(it) },
                                Date(lesson.endTime.toLong() * 1000).let { toCommon.format(it) })
                            lessonTeacher.text = getString(
                                R.string.teacher_name_template,
                                lesson.teacher.lastName,
                                lesson.teacher.firstName,
                                lesson.teacher.middleName
                            )
                            lessonNumber.text =
                                getString(R.string.lesson_n, toOrdinal(lesson.number))
                            if (lesson.homework?.text == null) {
                                homeworkCard.visibility = View.GONE
                            } else homeworkText.text = lesson.homework.text
                            attachmentsRecyclerView.layoutManager =
                                LinearLayoutManager(this@LessonActivity)
                            attachmentsRecyclerView.adapter =
                                AttachmentsAdapter(this@LessonActivity, lesson.attachments)
                        }
                    }
                }) {})
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
        private val attachments: List<Attachments>
    ) :
        RecyclerView.Adapter<AttachmentsAdapter.AttachmentsViewHolder>() {

        class AttachmentsViewHolder(
            attachmentBinding: ItemHomeworkAttachmentBinding,
            context: Context
        ) :
            RecyclerView.ViewHolder(attachmentBinding.root) {
            private val binding = attachmentBinding
            private val parentContext = context
            fun bind(attachment: Attachments) {
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