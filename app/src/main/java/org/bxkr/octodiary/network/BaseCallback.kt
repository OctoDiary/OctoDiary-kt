package org.bxkr.octodiary.network

import android.content.Context
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import okhttp3.ResponseBody
import org.bxkr.octodiary.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class BaseCallback<T>(
    private val parentContext: Context,
    private val bindingRoot: View? = null,
    private val errorMessage: Int = 0,
    private val function: (response: Response<T>) -> Unit,
    private val errorFunction: ((errorBody: ResponseBody) -> Unit)? = null
) : Callback<T> {
    override fun onResponse(
        call: Call<T>,
        response: Response<T>
    ) {
        if (response.isSuccessful) {
            function(response)
        } else {
            if (errorMessage != 0 && bindingRoot != null) Snackbar.make(
                bindingRoot,
                errorMessage,
                Snackbar.LENGTH_LONG
            ).show()
            else errorFunction?.let { response.errorBody()?.let { it1 -> it(it1) } }
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        Log.e(this::class.simpleName, parentContext.getString(R.string.retrofit_error, t.message))
    }
}