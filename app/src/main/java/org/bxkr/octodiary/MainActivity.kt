package org.bxkr.octodiary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bxkr.octodiary.databinding.ActivityMainBinding
import org.bxkr.octodiary.fragments.DiaryFragment
import org.bxkr.octodiary.fragments.ProfileFragment
import org.bxkr.octodiary.models.diary.Diary
import org.bxkr.octodiary.models.diary.Week
import org.bxkr.octodiary.models.user.User
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

    var diaryData: List<Week>?
        get() {
            val dataAge = this.getSharedPreferences(
                getString(R.string.saved_data_key),
                Context.MODE_PRIVATE
            ).getLong(getString(R.string.data_age_key), (-1).toLong())

            if (dataAge == (-1).toLong() || ((System.currentTimeMillis() - dataAge) >= 3600000)) {
                return null
            }

            val jsonEncoded = this.getSharedPreferences(
                getString(R.string.saved_data_key),
                Context.MODE_PRIVATE
            ).getString(getString(R.string.diary_data_key), null)
            if (jsonEncoded != null) {
                return Gson().fromJson<List<Week>>(
                    jsonEncoded,
                    object : TypeToken<List<Week>>() {}.type
                )
            }
            return null
        }
        set(value) {
            val jsonEncoded = Gson().toJson(value)
            this.getSharedPreferences(
                getString(R.string.saved_data_key),
                Context.MODE_PRIVATE
            ).edit {
                putString(getString(R.string.diary_data_key), jsonEncoded)
                putLong(getString(R.string.data_age_key), System.currentTimeMillis())
            }
        }

    var userData: User?
        get() {
            val dataAge = this.getSharedPreferences(
                getString(R.string.saved_data_key),
                Context.MODE_PRIVATE
            ).getLong(getString(R.string.data_age_key), (-1).toLong())

            if (dataAge == (-1).toLong() || ((System.currentTimeMillis() - dataAge) >= 3600000)) {
                return null
            }

            val jsonEncoded = this.getSharedPreferences(
                getString(R.string.saved_data_key),
                Context.MODE_PRIVATE
            ).getString(getString(R.string.user_data_key), null)
            if (jsonEncoded != null) {
                return Gson().fromJson<User>(jsonEncoded, object : TypeToken<User>() {}.type)
            }
            return null
        }
        set(value) {
            val jsonEncoded = Gson().toJson(value)
            this.getSharedPreferences(
                getString(R.string.saved_data_key),
                Context.MODE_PRIVATE
            ).edit {
                putString(getString(R.string.user_data_key), jsonEncoded)
                putLong(getString(R.string.data_age_key), System.currentTimeMillis())
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (token == null && userId == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else if (token == getString(R.string.demo_token) && userId == getString(R.string.demo_user_id)) {
            val raw = resources.openRawResource(R.raw.sample_diary_data)
            val byteArray = ByteArray(raw.available())
            raw.read(byteArray)
            userData = Gson().fromJson<User>(
                getString(R.string.sample_user_data),
                object : TypeToken<User>() {}.type
            )
            diaryData = Gson().fromJson<List<Week>>(
                String(byteArray),
                object : TypeToken<List<Week>>() {}.type
            )
            allDataLoaded()
        } else createDiary()

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)

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

    fun createDiary(listener: () -> Unit = {}) {
        if (diaryData != null && userData != null) {
            allDataLoaded()
        }
        val call = NetworkService.api().diary(token, userId)
        call.enqueue(object : Callback<Diary> {
            override fun onResponse(
                call: Call<Diary>, response: Response<Diary>
            ) {
                if (response.isSuccessful) {
                    diaryData = response.body()!!.weeks
                    getProfile(listener)
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

    fun getProfile(listener: () -> Unit = {}) {
        val call = NetworkService.api().user(token, userId)
        call.enqueue(object : Callback<User> {
            override fun onResponse(
                call: Call<User>, response: Response<User>
            ) {
                if (response.isSuccessful) {
                    userData = response.body()!!
                    listener()
                    allDataLoaded()
                } else {
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    intent.putExtra(getString(R.string.out_of_date_extra), true)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(this::class.simpleName, getString(R.string.retrofit_error))
            }
        })
    }

    private fun allDataLoaded() {

        if (supportFragmentManager.findFragmentById(R.id.fragment) == null && !supportFragmentManager.isDestroyed)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, DiaryFragment()).commit()
        binding.progressBar.visibility = View.GONE
        binding.fragment.visibility = View.VISIBLE
        binding.bottomNavigationView.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (binding.fragment.getFragment<Fragment>() is ProfileFragment) {
            menuInflater.inflate(R.menu.profile_top_app_bar, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}