package org.bxkr.octodiary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.bxkr.octodiary.databinding.ActivityMainBinding
import org.bxkr.octodiary.fragments.DiaryFragment
import org.bxkr.octodiary.fragments.ProfileFragment
import org.bxkr.octodiary.models.Diary
import org.bxkr.octodiary.models.Week
import org.bxkr.octodiary.network.NetworkService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val token: String?
        get() = this.getSharedPreferences(getString(R.string.auth_file_key), Context.MODE_PRIVATE)
            .getString(getString(R.string.token), null)

    val userId: String?
        get() = this.getSharedPreferences(getString(R.string.auth_file_key), Context.MODE_PRIVATE)
            .getString(getString(R.string.user_id), null)

    var diaryData: List<Week>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (token == null && userId == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else createDiary()

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.diaryPage -> {
                    val transaction = supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment, DiaryFragment())
                    transaction.commit()
                    true
                }
                R.id.profilePage -> {
                    val transaction = supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment, ProfileFragment())
                    transaction.commit()
                    true
                }
                else -> false
            }
        }
        binding.bottomNavigationView.setOnItemReselectedListener { }
    }

    fun createDiary() {
        val call = NetworkService.api().diary(token, userId)
        call.enqueue(object : Callback<Diary> {
            override fun onResponse(
                call: Call<Diary>, response: Response<Diary>
            ) {
                if (response.isSuccessful) {
                    diaryData = response.body()!!.weeks
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment, DiaryFragment()).commit()
                    binding.progressBar.visibility = View.GONE
                    binding.fragment.visibility = View.VISIBLE
                    binding.bottomNavigationView.visibility = View.VISIBLE
                } else {
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    intent.putExtra(getString(R.string.out_of_date_extra), true)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onFailure(call: Call<Diary>, t: Throwable) {
                Log.e(this::class.simpleName, getString(R.string.retrofit_error))
            }
        })
    }
}