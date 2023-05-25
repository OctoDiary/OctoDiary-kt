package org.bxkr.octodiary.ui.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.bxkr.octodiary.R
import org.bxkr.octodiary.Utils
import org.bxkr.octodiary.Utils.isDemo
import org.bxkr.octodiary.databinding.ActivitySubjectBinding
import org.bxkr.octodiary.models.diary.Homework
import org.bxkr.octodiary.models.diary.Lesson
import org.bxkr.octodiary.models.diary.Subject
import org.bxkr.octodiary.models.diary.WorkMark
import org.bxkr.octodiary.models.rating.RankingPlaces
import org.bxkr.octodiary.models.subject.Mark
import org.bxkr.octodiary.models.subject.Rating
import org.bxkr.octodiary.models.subject.SubjectDetails
import org.bxkr.octodiary.network.BaseCallback
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.ui.adapters.LessonsAdapter
import org.bxkr.octodiary.ui.dialogs.RatingBottomSheet
import kotlin.properties.Delegates

class SubjectActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubjectBinding
    private var personId by Delegates.notNull<Long>()
    private var groupId by Delegates.notNull<Long>()
    private var subjectId by Delegates.notNull<Long>()
    private var periodId by Delegates.notNull<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySubjectBinding.inflate(layoutInflater)

        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val prefs =
            this.getSharedPreferences(getString(R.string.auth_file_key), Context.MODE_PRIVATE)
        personId = intent.getLongExtra("person_id", 0)
        groupId = intent.getLongExtra("group_id", 0)
        subjectId = intent.getLongExtra("subject_id", 0)
        periodId = intent.getLongExtra("period_id", 0)

        if (isDemo(this)) configureSubject(
            Utils.getJsonRaw(resources.openRawResource(R.raw.sample_subject_details_data)),
            prefs
        ) else {
            val call = NetworkService.api(
                NetworkService.Server.values()[prefs.getInt(
                    getString(R.string.server_key),
                    0
                )]
            ).subjectDetails(
                personId,
                groupId,
                subjectId,
                periodId,
                prefs.getString(getString(R.string.token), null)
            )

            call.enqueue(object : BaseCallback<SubjectDetails>(
                this,
                binding.root,
                R.string.unexpected_error,
                { response ->
                    val responseBody = response.body()
                    if (responseBody != null) configureSubject(responseBody, prefs)
                }) {})
        }
    }

    private fun configureSubject(subject: SubjectDetails, prefs: SharedPreferences) {
        binding.scrollView.visibility = View.VISIBLE
        binding.bigProgressBar.visibility = View.GONE
        binding.subjectContent.visibility = View.VISIBLE
        binding.subjectName.text = subject.subject.name
        binding.periodName.text = getString(
            R.string.period_name,
            subject.period.number.toString(),
            getString(subject.period.type.stringRes)
        )
        if (subject.reportsPlot.averageMarks.averageMark != null) {
            binding.avg.text = subject.reportsPlot.averageMarks.averageMark
        } else {
            binding.avg.visibility = View.GONE
            binding.avgDesc.visibility = View.GONE
        }
        if (subject.reportsPlot.averageMarks.weightedAverageMark != null) {
            binding.weightedAvg.text = subject.reportsPlot.averageMarks.weightedAverageMark
        } else {
            binding.weightedAvg.visibility = View.GONE
            binding.weightedAvgDesc.visibility = View.GONE
        }
        if (subject.reportsPlot.averageMarks.averageMarkByImportantWork != null) {
            binding.avgImportant.text =
                subject.reportsPlot.averageMarks.averagemarkByImportantWorkTrend
        } else {
            binding.avgImportant.visibility = View.GONE
            binding.avgImportantDesc.visibility = View.GONE
        }
        configureRating(prefs, subject.rating)
        configureMarks(subject.marks)
    }

    private fun configureRating(preferences: SharedPreferences, ratingData: Rating) {
        val showRating = preferences.getBoolean("show_rating", true)

        if (showRating) {
            binding.ratingCard.visibility = View.VISIBLE
            binding.ratingStatus.text =
                getString(
                    R.string.rating_place_by_subject,
                    Utils.toOrdinal(ratingData.rankingPlaces.first { it.isContextUser }.place)
                )
            val openBottomSheet = { _: View ->
                val bottomSheet =
                    RatingBottomSheet(org.bxkr.octodiary.models.rating.Rating(ratingData.rankingPlaces.map {
                        RankingPlaces(
                            it.place,
                            it.imageUrl,
                            it.averageMark,
                            it.isContextUser,
                            it.trend
                        )
                    }))
                bottomSheet.show(supportFragmentManager, null)
            }
            binding.ratingButton.setOnClickListener(openBottomSheet)
            binding.ratingCard.setOnClickListener(openBottomSheet)
        } else binding.ratingCard.visibility = View.GONE
    }

    private fun configureMarks(marks: List<Mark>) {
        binding.marksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.marksRecyclerView.adapter = LessonsAdapter(
            this, compact = false, personIdCommon = personId, groupIdCommon = groupId,
            lessons = marks.map {
                Lesson(
                    subject = Subject(
                        name = it.markTypeText,
                        id = 0,
                        knowledgeArea = String(),
                        subjectMood = null
                    ),
                    theme = it.lesson.theme,
                    homework = Homework(
                        attachments = listOf(),
                        isCompleted = false,
                        text = it.lesson.theme,
                        workIsAttachRequired = false
                    ),
                    workMarks = listOf(
                        WorkMark(it.marks, 0)
                    ),
                    id = 0,
                    comment = null,
                    endDateTime = null,
                    group = null,
                    hasAttachment = null,
                    importantWorks = null,
                    isCanceled = null,
                    isEmpty = null,
                    number = null,
                    place = null,
                    startDateTime = null,
                    teacher = null,
                )
            }
        )
    }
}