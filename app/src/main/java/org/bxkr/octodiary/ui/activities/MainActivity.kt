package org.bxkr.octodiary.ui.activities

import android.Manifest
import android.app.AlarmManager
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import org.bxkr.octodiary.BuildConfig
import org.bxkr.octodiary.R
import org.bxkr.octodiary.UpdateReceiver
import org.bxkr.octodiary.Utils.agedData
import org.bxkr.octodiary.Utils.checkUpdate
import org.bxkr.octodiary.Utils.getJsonRaw
import org.bxkr.octodiary.Utils.isDemo
import org.bxkr.octodiary.databinding.ActivityMainBinding
import org.bxkr.octodiary.models.diary.Diary
import org.bxkr.octodiary.models.diary.Week
import org.bxkr.octodiary.models.rating.RatingClass
import org.bxkr.octodiary.models.user.User
import org.bxkr.octodiary.models.userfeed.UserFeed
import org.bxkr.octodiary.network.BaseCallback
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.ui.fragments.DashboardFragment
import org.bxkr.octodiary.ui.fragments.DiaryFragment
import org.bxkr.octodiary.ui.fragments.ProfileFragment
import java.util.Calendar


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var alarmManager: AlarmManager

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

    var diaryData: List<Week>?
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

    private val server: Int
        get() = this.getSharedPreferences(
            getString(R.string.auth_file_key), Context.MODE_PRIVATE
        ).getInt(getString(R.string.server_key), 0)

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
                diaryData = getJsonRaw<List<Week>>(openRawResource(R.raw.sample_diary_data))
                ratingData = getJsonRaw<RatingClass>(openRawResource(R.raw.sample_rating_data))
                allDataLoaded()
            }
        } else createDiary()

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.diaryPage -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment, DiaryFragment()).commitAllowingStateLoss()
                    true
                }

                R.id.dashboardPage -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment, DashboardFragment()).commitAllowingStateLoss()
                    true
                }

                R.id.profilePage -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment, ProfileFragment()).commitAllowingStateLoss()
                    true
                }

                else -> false
            }
        }
        binding.bottomNavigationView.setOnItemReselectedListener { }
    }

    fun createDiary(listener: () -> Unit = {}) {
        checkUpdate(this) {
            if (it.body() != null) with(it.body()!!) {
                if (tag_name != BuildConfig.ATTACHED_GIT_TAG) {
                    MaterialAlertDialogBuilder(
                        this@MainActivity,
                        com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
                    ).setTitle(getString(R.string.update_available, tag_name))
                        .setMessage(getString(R.string.update_available_message, name))
                        .setIcon(R.drawable.ic_outline_file_download_24)
                        .setNeutralButton(R.string.changes) { _, _ ->
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(html_url)
                            startActivity(intent)
                        }.setNegativeButton(R.string.later) { dialog, _ ->
                            dialog.dismiss()
                        }.setPositiveButton(R.string.yes) { _, _ ->
                            downloadUpdate(assets[0].browser_download_url, tag_name)
                            Snackbar.make(
                                binding.root, R.string.download_started, Snackbar.LENGTH_LONG
                            ).show()
                        }.show()
                }
            }
        }
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


    private fun isAllDataLoaded(): Boolean =
        userData != null && diaryData != null && ratingData != null && userFeedData != null

    private fun allDataLoaded(listener: () -> Unit = {}) {

        listener()

        val defaultScreen =
            PreferenceManager.getDefaultSharedPreferences(this).getString("default_screen", "diary")

        val openedFragment = supportFragmentManager.findFragmentById(R.id.fragment)
        if (openedFragment == null && !supportFragmentManager.isDestroyed) {

            var fragToOpen: Fragment? = null

            when (defaultScreen) {
                "diary" -> {
                    fragToOpen = DiaryFragment()
                    binding.bottomNavigationView.selectedItemId = R.id.diaryPage
                }

                "dashboard" -> {
                    fragToOpen = DashboardFragment()
                    binding.bottomNavigationView.selectedItemId = R.id.dashboardPage
                }

                "profile" -> {
                    fragToOpen = ProfileFragment()
                    binding.bottomNavigationView.selectedItemId = R.id.profilePage
                }
            }

            if (fragToOpen != null) {
                supportFragmentManager.beginTransaction().replace(R.id.fragment, fragToOpen)
                    .commitAllowingStateLoss()
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

    private fun downloadUpdate(url: String, name: String) {
        val request =
            DownloadManager.Request(Uri.parse(url)).setTitle(getString(R.string.update, name))
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    getString(R.string.update_file_name, getString(R.string.app_name), name)
                )
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val install = Intent(Intent.ACTION_VIEW)
                install.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                install.setDataAndType(
                    downloadManager.getUriForDownloadedFile(downloadId),
                    "application/vnd.android.package-archive"
                )
                startActivity(install)
                finish()
            }
        }, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
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