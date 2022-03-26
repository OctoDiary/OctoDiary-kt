package org.bxkr.octodiary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.bxkr.octodiary.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPref =
            this.getSharedPreferences(getString(R.string.auth_file_key), Context.MODE_PRIVATE)
                ?: return
        val token = sharedPref.getString(getString(R.string.token), null)
        val uid = sharedPref.getString(getString(R.string.user_id), null)
        if (token == null && uid == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            binding.textView.text = getString(R.string.user_id_is, uid)
        }
    }
}