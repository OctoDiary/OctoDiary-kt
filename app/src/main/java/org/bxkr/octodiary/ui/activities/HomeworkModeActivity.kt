package org.bxkr.octodiary.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.bxkr.octodiary.databinding.ActivityHomeworkModeBinding

class HomeworkModeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeworkModeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeworkModeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}