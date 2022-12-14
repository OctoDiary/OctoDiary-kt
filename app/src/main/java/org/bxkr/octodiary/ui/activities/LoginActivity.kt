package org.bxkr.octodiary.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.material.snackbar.Snackbar
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.ActivityLoginBinding
import org.bxkr.octodiary.network.BaseCallback
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.network.NetworkService.Server
import org.bxkr.octodiary.ui.adapters.RecyclerBaseAdapter
import org.bxkr.octodiary.ui.adapters.SelectServerArrayAdapter
import retrofit2.Call

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var serverPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val servers = Server.values()
        (binding.server.editText as? AutoCompleteTextView)?.setAdapter(RecyclerBaseAdapter(servers.map {
            getString(
                it.serverName
            )
        }, SelectServerArrayAdapter(this, servers)))
        (binding.server.editText as? AutoCompleteTextView)?.setOnItemClickListener { _, _, position, _ ->
            serverPosition = position
            val prefs =
                getSharedPreferences(getString(R.string.auth_file_key), Context.MODE_PRIVATE)
            prefs.edit { putInt(getString(R.string.server_key), position) }
        }
        binding.password.editText?.setOnEditorActionListener { _, _, _ ->
            logIn()
            true
        }
        binding.logInButton.setOnClickListener { logIn() }
        binding.demoButton.setOnClickListener { logIn(demo = true) }
        if (intent.getBooleanExtra(getString(R.string.auth_out_of_date_extra), false)) {
            Snackbar.make(binding.root, R.string.auth_out_of_date, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun logIn(demo: Boolean = false) {
        if ((binding.username.editText?.text.toString() == getString(R.string.demousername) && binding.password.editText?.text.toString() == getString(
                R.string.demopassword
            )) || demo
        ) {
            val sharedPref = this@LoginActivity.getSharedPreferences(
                getString(R.string.auth_file_key), Context.MODE_PRIVATE
            ) ?: return
            with(sharedPref.edit()) {
                putString(
                    getString(R.string.user_id), getString(R.string.demo_user_id)
                )
                putString(
                    getString(R.string.token), getString(R.string.demo_token)
                )
                apply()
            }
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
        val server = Server.values()[serverPosition]
        val call: Call<NetworkService.AuthResult> = NetworkService.api(server).auth(
            NetworkService.AuthRequestBody(
                binding.username.editText?.text.toString(),
                binding.password.editText?.text.toString(),
                getString(server.clientId),
                getString(server.clientSecret),
                getString(R.string.default_scope),
            )
        )
        call.enqueue(object : BaseCallback<NetworkService.AuthResult>(
            this@LoginActivity,
            binding.root,
            R.string.wrong,
            {
                if ((it.body()?.credentials != null)) {
                    val sharedPref = this@LoginActivity.getSharedPreferences(
                        getString(R.string.auth_file_key), Context.MODE_PRIVATE
                    )
                    with(sharedPref.edit()) {
                        putString(
                            getString(R.string.user_id), it.body()?.credentials?.userId.toString()
                        )
                        putString(
                            getString(R.string.token), it.body()?.credentials?.accessToken
                        )
                        apply()
                    }
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else if (it.body()?.reason == getString(R.string.have_not_active_memberships_error_reason)) {
                    Snackbar.make(
                        binding.root,
                        R.string.have_not_active_memberships_message,
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else if (it.body()?.type == getString(R.string.error_type)) {
                    Snackbar.make(binding.root, R.string.wrong, Snackbar.LENGTH_SHORT).show()
                }
            }) {})
    }
}