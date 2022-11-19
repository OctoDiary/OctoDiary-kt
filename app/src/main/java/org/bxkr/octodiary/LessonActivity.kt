package org.bxkr.octodiary

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bxkr.octodiary.databinding.ActivityLessonBinding
import org.bxkr.octodiary.models.diary.Lesson
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
}