package org.bxkr.octodiary.ui.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import org.bxkr.octodiary.BuildConfig
import org.bxkr.octodiary.R
import org.bxkr.octodiary.Utils.checkUpdate
import org.bxkr.octodiary.Utils.getJsonRaw
import org.bxkr.octodiary.Utils.isDemo
import org.bxkr.octodiary.databinding.ActivityMainBinding
import org.bxkr.octodiary.models.diary.Diary
import org.bxkr.octodiary.models.diary.Week
import org.bxkr.octodiary.models.rating.RatingClass
import org.bxkr.octodiary.models.user.User
import org.bxkr.octodiary.network.BaseCallback
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.ui.fragments.DiaryFragment
import org.bxkr.octodiary.ui.fragments.ProfileFragment
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

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

    val server: Int
        get() = this.getSharedPreferences(
            getString(R.string.auth_file_key),
            Context.MODE_PRIVATE
        ).getInt(getString(R.string.server_key), 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        checkUpdate(this) {
            if (it.body() != null) with(it.body()!!) {
                if (tag_name != BuildConfig.ATTACHED_GIT_TAG) {
                    MaterialAlertDialogBuilder(
                        this@MainActivity,
                        com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
                    )
                        .setTitle(getString(R.string.update_available, tag_name))
                        .setMessage(getString(R.string.update_available_message, name))
                        .setIcon(R.drawable.ic_outline_file_download_24)
                        .setNeutralButton(R.string.changes) { _, _ ->
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(html_url)
                            startActivity(intent)
                        }
                        .setNegativeButton(R.string.later) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setPositiveButton(R.string.yes) { _, _ ->
                            downloadUpdate(assets[0].browser_download_url)
                        }
                        .show()
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
        }, errorFunction = {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.putExtra(getString(R.string.auth_out_of_date_extra), true)
            startActivity(intent)
            finish()
        }) {})
    }

    fun getRating(listener: () -> Unit = {}) {
        with(userData!!) {
            val call = NetworkService.api(NetworkService.Server.values()[server]).rating(
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
            val call = NetworkService.api(NetworkService.Server.values()[server]).diary(
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
        binding.swipeRefresh.visibility = View.VISIBLE
        binding.swipeRefresh.setOnRefreshListener {
            createDiary { binding.swipeRefresh.isRefreshing = false }
        }
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
                val prefs =
                    getSharedPreferences(getString(R.string.auth_file_key), Context.MODE_PRIVATE)
                prefs.edit { putInt(getString(R.string.server_key), 0) }
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun downloadUpdate(url: String) {
        NetworkService.updateApi(getString(R.string.api_git_host)).download(url)
            .enqueue(object : BaseCallback<ResponseBody>(this, function = { response ->
                val file = Uri.parse(url).lastPathSegment?.let { File(cacheDir, it) }
                response.body()?.byteStream()?.use {
                    FileOutputStream(file).use { targetOutputStream ->
                        val uri = file?.let { it1 ->
                            FileProvider.getUriForFile(
                                this, BuildConfig.APPLICATION_ID + ".provider",
                                it1
                            )
                        }
                        it.copyTo(targetOutputStream)
                        val install = Intent(Intent.ACTION_VIEW)
                        install.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        install.setDataAndType(
                            uri,
                            "application/vnd.android.package-archive"
                        )
                        startActivity(install)
                        finish()
                    }
                } ?: throw RuntimeException("Failed to download: $url")
            }) {})
    }
}