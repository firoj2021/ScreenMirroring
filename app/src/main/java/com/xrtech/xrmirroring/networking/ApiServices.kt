package com.xrtech.xrmirroring.networking

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.xrtech.xrmirroring.QRCodeActivity
import com.xrtech.xrmirroring.listeners.CommonListener
import com.xrtech.xrmirroring.utils.Extensions.toDate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


object ApiServices {
    const val TAG = "ApiServices"
    fun postLoginCode(context: QRCodeActivity, url: String,deviceUID:String,mListener: CommonListener) {
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
                if (response.isSuccessful && response.body()?.data != null){
                    Log.d(TAG,"response.isSuccessful:${response.body()?.data!!.name}")
                    val data = response.body()?.data!!
                    val loginCode = data.login_code
                    val finalDate = loginCode.validTill.toDate("yyyy-MM-dd HH:mm:ss")
                    val calendar: Calendar = Calendar.getInstance()
                    val currentTime = calendar.timeInMillis
                    var qrExpired = false
                    if (finalDate?.time!!.toLong() < currentTime) {
                        qrExpired = true
                    }
                    val intent = Intent()
                    intent.putExtra("exp", loginCode.validTill)
                    intent.putExtra("qrExpired", qrExpired)
                    context.setResult(Activity.RESULT_OK, intent)
                    context.finish()
                }else{
                    mListener.error(response.body()?.message!!)
                    val intent = Intent()
                    intent.putExtra("exp", "2023-11-11 10:10:00")
                    intent.putExtra("qrExpired", true)
                    context.setResult(Activity.RESULT_OK, intent)
                    context.finish()
                }
            }
            override fun onFailure(call: Call<LoginRequestResponse>, t: Throwable) {
                Log.d(TAG,"onFailure:${t.message}")
            }

        })
    }
}


