package org.bxkr.octodiary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import org.bxkr.octodiary.databinding.ActivityLoginBinding
import org.bxkr.octodiary.network.NetworkService
import retrofit2.Call

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.password.editText?.setOnEditorActionListener { _, _, _ ->
            logIn()
            true
        }
        binding.logInButton.setOnClickListener { logIn() }
        if (intent.getBooleanExtra(getString(R.string.out_of_date_extra), false)) {
            Snackbar.make(binding.root, R.string.out_of_date, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun logIn() {
        if (binding.username.editText?.text.toString() == getString(R.string.demousername) &&
            binding.password.editText?.text.toString() == getString(R.string.demopassword)
        ) {
            val sharedPref = this@LoginActivity.getSharedPreferences(
                getString(R.string.auth_file_key),
                Context.MODE_PRIVATE
            ) ?: return
            with(sharedPref.edit()) {
                putString(
                    getString(R.string.user_id),
                    getString(R.string.demo_user_id)
                )
                putString(
                    getString(R.string.token),
                    getString(R.string.demo_token)
                )
                apply()
            }
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
        val call: Call<NetworkService.AuthResult> = NetworkService.api().auth(
            NetworkService.AuthRequestBody(
                binding.username.editText?.text.toString(),
                binding.password.editText?.text.toString(),
                getString(R.string.client_id),
                getString(R.string.client_secret),
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
                        getString(R.string.auth_file_key),
                        Context.MODE_PRIVATE
                    )
                    with(sharedPref.edit()) {
                        putString(
                            getString(R.string.user_id),
                            it.body()?.credentials?.userId.toString()
                        )
                        putString(
                            getString(R.string.token),
                            it.body()?.credentials?.accessToken
                        )
                        apply()
                    }
                }
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }) {})
    }
}