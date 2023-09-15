package org.bxkr.octodiary

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class BaseCallback<T>(
    private val function: (response: Response<T>) -> Unit,
    private val errorFunction: ((errorBody: ResponseBody, httpCode: Int) -> Unit)? = null,
    private val noConnectionFunction: ((t: Throwable) -> Unit)? = null
) : Callback<T> {
    override fun onResponse(
        call: Call<T>,
        response: Response<T>
    ) {
        if (response.isSuccessful) {
            function(response)
        } else {
            errorFunction?.let {
                response.errorBody()?.let { it1 -> it(it1, response.code()) }
            }
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        noConnectionFunction?.invoke(t)
    }
}