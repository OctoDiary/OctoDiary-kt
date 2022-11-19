package org.bxkr.octodiary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.bxkr.octodiary.databinding.ActivityLessonBinding

class LessonActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLessonBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLessonBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }
}