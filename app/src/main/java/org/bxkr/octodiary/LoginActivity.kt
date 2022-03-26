package org.bxkr.octodiary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.bxkr.octodiary.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.logInButton.setOnClickListener {
            val call: Call<NetworkService.AuthResult> = NetworkService.api().auth(
                binding.username.editText?.text.toString(),
                binding.password.editText?.text.toString()
            )
            call.enqueue(object : Callback<NetworkService.AuthResult> {
                override fun onResponse(
                    call: Call<NetworkService.AuthResult>,
                    response: Response<NetworkService.AuthResult>
                ) {
                    val sharedPref = this@LoginActivity.getSharedPreferences(
                        getString(R.string.auth_file_key),
                        Context.MODE_PRIVATE
                    ) ?: return
                    with(sharedPref.edit()) {
                        putString(getString(R.string.user_id), response.body()?.user_id.toString())
                        putString(getString(R.string.token), response.body()?.access_token)
                        apply()
                    }
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                }

                override fun onFailure(call: Call<NetworkService.AuthResult>, t: Throwable) {
                    Log.e(this::class.simpleName, "Retrofit error")
                }
            })
        }
    }
}