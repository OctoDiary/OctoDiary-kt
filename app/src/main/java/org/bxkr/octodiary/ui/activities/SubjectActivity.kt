package org.bxkr.octodiary.ui.activities

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.ActivitySubjectBinding
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

    }
}