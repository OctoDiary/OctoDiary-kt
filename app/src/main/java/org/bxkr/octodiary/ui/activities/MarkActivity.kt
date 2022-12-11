package org.bxkr.octodiary.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.bxkr.octodiary.R
import org.bxkr.octodiary.Utils.getJsonRaw
import org.bxkr.octodiary.Utils.isDemo
import org.bxkr.octodiary.databinding.ActivityMarkBinding
import org.bxkr.octodiary.models.mark.MarkDetails
import org.bxkr.octodiary.network.BaseCallback
import org.bxkr.octodiary.network.NetworkService
import java.text.SimpleDateFormat
import java.util.Date

class MarkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarkBinding
    private var personId = 0L
    private var groupId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (isDemo(this)) {
            configureMark(getJsonRaw(resources.openRawResource(R.raw.sample_mark_details_data)))
        } else {
            val prefs =
                this.getSharedPreferences(getString(R.string.auth_file_key), Context.MODE_PRIVATE)

            personId = intent.getLongExtra("person_id", 0)
            groupId = intent.getLongExtra("group_id", 0)
            val markId = intent.getLongExtra("mark_id", 0)

            val call = NetworkService.api(
                NetworkService.Server.values()[prefs.getInt(
                    getString(R.string.server_key),
                    0
                )]
            ).markDetails(
                personId,
                groupId,
                markId,
                prefs.getString(getString(R.string.token), null)
            )

            call.enqueue(object : BaseCallback<MarkDetails>(
                this,
                binding.root,
                R.string.unexpected_error,
                { response ->
                    if (response.body() != null) {
                        val mark = response.body()!!
                        configureMark(mark)
                    }
                }) {})

            supportActionBar?.setDisplayHomeAsUpEnabled(true)
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

    private fun configureMark(mark: MarkDetails) {

        val toCommon =
            SimpleDateFormat("dd MMMM", this.resources.configuration.locales[0])
        val toWeekday =
            SimpleDateFormat("EEEE", this.resources.configuration.locales[0])

        with(binding) {
            bigProgressBar.visibility = View.GONE
            contentScrollView.visibility = View.VISIBLE
            markValue.text = mark.markDetails.marks[0].value
            lessonDate.text = this@MarkActivity.getString(
                R.string.date_weekday,
                Date(mark.date.toLong() * 1000).let { toCommon.format(it) },
                Date(mark.date.toLong() * 1000).let { toWeekday.format(it) })
            lessonName.text = mark.subject.name
            workName.text = mark.markDetails.markTypeText
            markInfoCard.setOnClickListener {
                val intent = Intent(this@MarkActivity, LessonActivity::class.java)
                intent.putExtra("lesson_id", mark.lessonId)
                intent.putExtra("person_id", personId)
                intent.putExtra("group_id", groupId)
                this@MarkActivity.startActivity(intent)
            }
        }
    }
}