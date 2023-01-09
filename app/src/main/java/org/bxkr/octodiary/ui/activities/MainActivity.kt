package org.bxkr.octodiary.ui.activities

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import org.bxkr.octodiary.R
import org.bxkr.octodiary.UpdateReceiver
import org.bxkr.octodiary.Utils.agedData
import org.bxkr.octodiary.Utils.getJsonRaw
import org.bxkr.octodiary.Utils.isDemo
import org.bxkr.octodiary.databinding.ActivityMainBinding
import org.bxkr.octodiary.models.diary.Diary
import org.bxkr.octodiary.models.diary.Week
import org.bxkr.octodiary.models.periodmarks.PeriodMarksResponse
import org.bxkr.octodiary.models.rating.RatingClass
import org.bxkr.octodiary.models.user.User
import org.bxkr.octodiary.models.userfeed.UserFeed
import org.bxkr.octodiary.network.BaseCallback
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.ui.fragments.DashboardFragment
import org.bxkr.octodiary.ui.fragments.DiaryFragment
import org.bxkr.octodiary.ui.fragments.PeriodMarksFragment
import org.bxkr.octodiary.ui.fragments.ProfileFragment
import java.util.Calendar


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var alarmManager: AlarmManager

    var token: String?
        get() = this.getSharedPreferences(getString(R.string.auth_file_key), Context.MODE_PRIVATE)
            .getString(getString(R.string.token), null)
        set(value) {
            this.getSharedPreferences(getString(R.string.auth_file_key), Context.MODE_PRIVATE)
                .edit {
                    putString(getString(R.string.token), value)
                }
        }

    var userId: String?
        get() = this.getSharedPreferences(getString(R.string.auth_file_key), Context.MODE_PRIVATE)
            .getString(getString(R.string.user_id), null)
        set(value) {
            this.getSharedPreferences(getString(R.string.auth_file_key), Context.MODE_PRIVATE)
                .edit {
                    putString(getString(R.string.user_id), value)
                }
        }

    var diaryData: MutableList<Week>?
        get() = agedData(this, R.string.diary_data_key)
        set(value) = agedData(this, R.string.diary_data_key, value)

    var userData: User?
        get() = agedData(this, R.string.user_data_key)
        set(value) = agedData(this, R.string.user_data_key, value)

    var ratingData: RatingClass?
        get() = agedData(this, R.string.rating_data_key)
        set(value) = agedData(this, R.string.rating_data_key, value)

    var userFeedData: UserFeed?
        get() = agedData(this, R.string.user_feed_data_key)
        set(value) = agedData(this, R.string.user_feed_data_key, value)

    var periodMarksData: PeriodMarksResponse?
        get() = agedData(this, R.string.period_marks_data_key)
        set(value) = agedData(this, R.string.period_marks_data_key, value)

    val server: Int
        get() = this.getSharedPreferences(
            getString(R.string.auth_file_key), Context.MODE_PRIVATE
        ).getInt(getString(R.string.server_key), 0)

    private val diaryFragment = DiaryFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (token == null && userId == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else if (isDemo(this)) {
            with(resources) {
                userData = getJsonRaw<User>(openRawResource(R.raw.sample_user_data))
                diaryData =
                    getJsonRaw<List<Week>>(openRawResource(R.raw.sample_diary_data)) as MutableList<Week>
                ratingData = getJsonRaw<RatingClass>(openRawResource(R.raw.sample_rating_data))
                userFeedData = getJsonRaw<UserFeed>(openRawResource(R.raw.sample_userfeed_data))
                periodMarksData =
                    getJsonRaw<PeriodMarksResponse>(openRawResource(R.raw.sample_period_marks_data))

                allDataLoaded()
            }
        } else createDiary()

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)

        val changeFragment: (Fragment) -> Boolean = {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, it).commitAllowingStateLoss()
            true
        }
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.diaryPage -> changeFragment(diaryFragment)
                R.id.dashboardPage -> changeFragment(DashboardFragment())
                R.id.periodMarksPage -> changeFragment(PeriodMarksFragment())
                R.id.profilePage -> changeFragment(ProfileFragment())

                else -> false
            }
        }
        binding.bottomNavigationView.setOnItemReselectedListener { }
    }

    fun createDiary(listener: () -> Unit = {}) {
        if ((diaryData != null) && (userData != null)) {
            allDataLoaded()
        }
        val call = userId?.toLong()
            ?.let { NetworkService.api(NetworkService.Server.values()[server]).user(it, token) }
        call?.enqueue(object : BaseCallback<User>(this, function = {
            userData = it.body()!!

            getRating(listener)
            getDiary(listener)
            getUserFeed(listener)
            getPeriodMarks(listener)

        }, errorFunction = { dataIsOutOfDate() }) {})

        /*
        It should work like this - first createDiary receives userData,
        and since the rest of the requests depend only on it,
        they are executed immediately after the userData is set.
        */

    }

    private fun getRating(listener: () -> Unit = {}) {
        with(userData!!) {
            val call = NetworkService.api(NetworkService.Server.values()[server]).rating(
                info.personId, contextPersons[0].group.id, token
            )
            call.enqueue(object : BaseCallback<RatingClass>(this@MainActivity, function = {
                ratingData = it.body()!!
                if (isAllDataLoaded()) allDataLoaded(listener)
            }, errorFunction = { dataIsOutOfDate() }) {})
        }
    }

    private fun getDiary(listener: () -> Unit = {}) {
        with(userData!!) {
            val call = NetworkService.api(NetworkService.Server.values()[server]).diary(
                info.personId, contextPersons[0].school.id, contextPersons[0].group.id, token
            )
            call.enqueue(object : BaseCallback<Diary>(this@MainActivity, function = {
                diaryData = it.body()!!.weeks
                if (isAllDataLoaded()) allDataLoaded(listener)
            }, errorFunction = { dataIsOutOfDate() }) {})
        }
    }

    private fun getUserFeed(listener: () -> Unit = {}) {
        with(userData!!) {
            val call = NetworkService.api(NetworkService.Server.values()[server]).userFeed(
                info.personId, contextPersons[0].group.id, token
            )
            call.enqueue(object : BaseCallback<UserFeed>(this@MainActivity, function = {
                userFeedData = it.body()
                if (isAllDataLoaded()) allDataLoaded(listener)
            }, errorFunction = { dataIsOutOfDate() }) {})
        }
    }

    private fun getPeriodMarks(listener: () -> Unit = {}) {
        with(userData!!) {
            val call = NetworkService.api(NetworkService.Server.values()[server]).periodMarks(
                info.personId,
                contextPersons[0].group.id,
                contextPersons[0].reportingPeriodGroup.periods.first { it.isCurrent }.id,
                token
            )
            call.enqueue(object : BaseCallback<PeriodMarksResponse>(this@MainActivity, function = {
                periodMarksData = it.body()
                if (isAllDataLoaded()) allDataLoaded(listener)
            }) {})
        }
    }


    private fun isAllDataLoaded(): Boolean =
        userData != null && diaryData != null && ratingData != null && userFeedData != null

    private fun allDataLoaded(listener: () -> Unit = {}) {

        listener()

        val defaultScreen =
            PreferenceManager.getDefaultSharedPreferences(this).getString("default_screen", "diary")

        val openedFragment = supportFragmentManager.findFragmentById(R.id.fragment)
        if (openedFragment == null && !supportFragmentManager.isDestroyed) {
            val openFragment: (Fragment, Int) -> Unit = { frag, id ->
                supportFragmentManager.beginTransaction().replace(R.id.fragment, frag)
                    .commitAllowingStateLoss()
                binding.bottomNavigationView.selectedItemId = id
            }
            when (defaultScreen) {
                "diary" -> openFragment(DiaryFragment(), R.id.diaryPage)
                "dashboard" -> openFragment(DashboardFragment(), R.id.dashboardPage)
                "period_marks" -> openFragment(PeriodMarksFragment(), R.id.periodMarksPage)
                "profile" -> openFragment(ProfileFragment(), R.id.profilePage)
            }
        } else if (openedFragment != null) {
            supportFragmentManager.beginTransaction().detach(openedFragment)
                .commitAllowingStateLoss() // Detach/attach refresh should be
            supportFragmentManager.beginTransaction().attach(openedFragment)
                .commitAllowingStateLoss() // ran with separate transactions
        }
        binding.progressBar.visibility = View.GONE
        binding.swipeRefresh.visibility = View.VISIBLE
        binding.swipeRefresh.setOnRefreshListener {
            createDiary { binding.swipeRefresh.isRefreshing = false }
        }
        binding.bottomNavigationView.visibility = View.VISIBLE

        val notifyTime: Calendar = Calendar.getInstance()

        val intent = Intent(this, UpdateReceiver::class.java)
        intent.putExtra("person_id", userData?.contextPersons?.get(0)?.personId)
        intent.putExtra("group_id", userData?.contextPersons?.get(0)?.group?.id)
        intent.putExtra("access_token", token)
        intent.putExtra("server", server)
        val prefs = getSharedPreferences(getString(R.string.saved_data_key), Context.MODE_PRIVATE)
        prefs.edit {
            putString("old_marks", Gson().toJson(userFeedData?.recentMarks))
        }
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        createNotificationChannel()
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            notifyTime.timeInMillis,
            300000,
            pendingIntent
        )
    }

    private fun dataIsOutOfDate() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.putExtra(getString(R.string.auth_out_of_date_extra), true)
        startActivity(intent)
        finish()
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
                userFeedData = null
                periodMarksData = null
                token = null
                userId = null
                this.getSharedPreferences(
                    getString(R.string.saved_data_key), Context.MODE_PRIVATE
                ).edit {
                    putLong(getString(R.string.data_age_key), -1L)
                }
                val prefs =
                    getSharedPreferences(getString(R.string.auth_file_key), Context.MODE_PRIVATE)
                prefs.edit { putInt(getString(R.string.server_key), 0) }
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun createNotificationChannel() {
        val name = getString(R.string.data_update_channel_name)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("data_update", name, importance)
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}