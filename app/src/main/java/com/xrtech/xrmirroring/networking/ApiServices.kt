package com.xrtech.xrmirroring.networking

import android.util.Log
import com.xrtech.xrmirroring.listeners.CommonListener
import com.xrtech.xrmirroring.utils.Extensions.toDate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar


object ApiServices {
    const val TAG = "ApiServices"
    fun postLoginCode(
        url: String,
        deviceUID: String,
        mListener: CommonListener
    ) {
        var retrofitClient: RetrofitClient? = RetrofitClient(url)
        val service: ApiEndPoints = retrofitClient!!.client!!.create(ApiEndPoints::class.java)
        service.postLoginCode(
            ApiConstants.apiBaseXAuthToken(),
            ApiConstants.CLIENT_ID,
            deviceUID
        )?.enqueue(object :
            Callback<LoginRequestResponse> {
            override fun onResponse(
                call: Call<LoginRequestResponse>,
                response: Response<LoginRequestResponse>,
            ) {
               if (response.isSuccessful && response.body()?.data != null) {
                    val data = response.body()?.data!!
                    val loginCode = data.login_code
                    val finalDate = loginCode.validTill.toDate("yyyy-MM-dd HH:mm:ss")
                    val calendar: Calendar = Calendar.getInstance()
                    val currentTime = calendar.timeInMillis
                    var qrExpired = false

                    val qrCodeStatus = data.login_code.is_active

                    Log.e(TAG,"qrCodeStatus:${qrCodeStatus}")

                    if (finalDate?.time!!.toLong() < currentTime) {
                        qrExpired = true
                    }
                    mListener.data(loginCode.validTill,qrExpired)
                } else {
                    mListener.error(response.body()?.message!!)
                    mListener.data("2023-11-11 10:10:00",true)
                }
            }
            override fun onFailure(call: Call<LoginRequestResponse>, t: Throwable) {
                mListener.error("Your request is failed")
            }

        })
    }
}


