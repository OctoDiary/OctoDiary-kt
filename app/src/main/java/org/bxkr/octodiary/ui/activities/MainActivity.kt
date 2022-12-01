package org.bxkr.octodiary.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.ActivityMainBinding
import org.bxkr.octodiary.models.diary.Diary
import org.bxkr.octodiary.models.diary.Week
import org.bxkr.octodiary.models.rating.RatingClass
import org.bxkr.octodiary.models.user.User
import org.bxkr.octodiary.network.BaseCallback
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.ui.fragments.DiaryFragment
import org.bxkr.octodiary.ui.fragments.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    var token: String?
        get() = this.getSharedPreferences(getString(R.string.auth_file_key), Context.MODE_PRIVATE)
            .getString(getString(R.string.token), null)
        set(value) {
            val prefs =
                this.getSharedPreferences(getString(R.string.auth_file_key), Context.MODE_PRIVATE)
            prefs.edit {
                putString(getString(R.string.token), value)
            }
        }

    var userId: String?
        get() = this.getSharedPreferences(getString(R.string.auth_file_key), Context.MODE_PRIVATE)
            .getString(getString(R.string.user_id), null)
        set(value) {
            val prefs =
                this.getSharedPreferences(getString(R.string.auth_file_key), Context.MODE_PRIVATE)
            prefs.edit {
                putString(getString(R.string.user_id), value)
            }
        }

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

    var ratingData: RatingClass?
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
            ).getString(getString(R.string.rating_data_key), null)
            if (jsonEncoded != null) {
                return Gson().fromJson<RatingClass>(
                    jsonEncoded,
                    object : TypeToken<RatingClass>() {}.type
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
                putString(getString(R.string.rating_data_key), jsonEncoded)
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
            val diaryRaw = resources.openRawResource(R.raw.sample_diary_data)
            val userRaw = resources.openRawResource(R.raw.sample_user_data)
            val ratingRaw = resources.openRawResource(R.raw.sample_rating_data)
            val diaryByteArray = ByteArray(diaryRaw.available())
            val userByteArray = ByteArray(userRaw.available())
            val ratingByteArray = ByteArray(ratingRaw.available())
            diaryRaw.read(diaryByteArray)
            userRaw.read(userByteArray)
            ratingRaw.read(ratingByteArray)
            userData = Gson().fromJson<User>(
                String(userByteArray),
                object : TypeToken<User>() {}.type
            )
            diaryData = Gson().fromJson<List<Week>>(
                String(diaryByteArray),
                object : TypeToken<List<Week>>() {}.type
            )
            ratingData = Gson().fromJson<RatingClass>(
                String(ratingByteArray),
                object : TypeToken<RatingClass>() {}.type
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
        if ((diaryData != null) && (userData != null)) {
            allDataLoaded()
        }
        val call = userId?.toLong()?.let { NetworkService.api().user(it, token) }
        call?.enqueue(object : BaseCallback<User>(this, function = {
            userData = it.body()!!
            getRating(listener)
        }, errorFunction = {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.putExtra(getString(R.string.auth_out_of_date_extra), true)
            startActivity(intent)
            finish()
        }) {})
    }

    fun getRating(listener: () -> Unit = {}) {
        with(userData!!) {
            val call = NetworkService.api().rating(
                info.personId, contextPersons[0].group.id, token
            )
            call.enqueue(object : BaseCallback<RatingClass>(this@MainActivity, function = {
                ratingData = it.body()!!
                getDiary(listener)
            }, errorFunction = {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.putExtra(getString(R.string.auth_out_of_date_extra), true)
                startActivity(intent)
                finish()
            }) {})
        }
    }

    fun getDiary(listener: () -> Unit = {}) {
        with(userData!!) {
            val call = NetworkService.api().diary(
                info.personId, contextPersons[0].school.id, contextPersons[0].group.id, token
            )
            call.enqueue(object : BaseCallback<Diary>(this@MainActivity, function = {
                diaryData = it.body()!!.weeks
                listener()
                allDataLoaded()
            }, errorFunction = {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.putExtra(getString(R.string.auth_out_of_date_extra), true)
                startActivity(intent)
                finish()
            }) {})
        }
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

            R.id.log_out -> {
                userData = null
                diaryData = null
                ratingData = null
                token = null
                userId = null
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}