package org.bxkr.octodiary.ui.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.bxkr.octodiary.R
import org.bxkr.octodiary.Utils
import org.bxkr.octodiary.databinding.ActivitySubjectBinding
import org.bxkr.octodiary.models.subject.SubjectDetails
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

        configureSubject(Utils.getJsonRaw(resources.openRawResource(R.raw.sample_subject_details_data)))
    }

    private fun configureSubject(subject: SubjectDetails) {
        binding.bigProgressBar.visibility = View.GONE
        binding.subjectName.text = subject.subject.name
        binding.periodName.text = getString(
            R.string.period_name,
            subject.period.number.toString(),
            getString(subject.period.type.stringRes)
        )
    }
}