package org.bxkr.octodiary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import org.bxkr.octodiary.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var token: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPref =
            this.getSharedPreferences(getString(R.string.auth_file_key), Context.MODE_PRIVATE)
                ?: return
        token = sharedPref.getString(getString(R.string.token), null)
        userId = sharedPref.getString(getString(R.string.user_id), null)
        if (token == null && userId == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            createDiary()
        }
    }

    private fun createDiary() {
        val call = NetworkService.api().diary(token, userId)
        call.enqueue(object : Callback<NetworkService.Diary> {
            override fun onResponse(
                call: Call<NetworkService.Diary>,
                response: Response<NetworkService.Diary>
            ) = if (response.isSuccessful) {
                val diaryData = response.body()!!.weeks
                binding.weeks.layoutManager = LinearLayoutManager(this@MainActivity)
                binding.weeks.adapter = WeeksAdapter(this@MainActivity, diaryData)
                binding.swipeRefresh.setOnRefreshListener { createDiary() }
            } else {
                Snackbar.make(binding.root, R.string.out_of_date, Snackbar.LENGTH_LONG).show()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            }

            override fun onFailure(call: Call<NetworkService.Diary>, t: Throwable) {
                Log.e(this::class.simpleName, getString(R.string.retrofit_error))
            }
        })
    }
}