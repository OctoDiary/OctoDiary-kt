package org.bxkr.octodiary.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.bxkr.octodiary.databinding.ActivityMarkBinding

class MarkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarkBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}