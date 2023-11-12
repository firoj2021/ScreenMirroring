package com.xrtech.xrmirroring.networking

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.util.concurrent.TimeUnit

//import constants.ApiConstants;
//import okhttp3.logging.HttpLoggingInterceptor;

class RetrofitClient(Base_URL:String) {

    private var retrofit: Retrofit? = null
     var Base_URL = Base_URL

     val client: Retrofit?
        get() {
            if (retrofit == null) {
                val protocols: List<Protocol> = object : ArrayList<Protocol>() {
                    init {
                        add(Protocol.HTTP_1_1)
                    }
                }
                val client = OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.MINUTES)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .protocols(protocols)
                    .build()

                retrofit = Retrofit.Builder()
                    .baseUrl(Base_URL).client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit
        }
    internal class OkHttpClientExt : OkHttpClient() {

        override fun newCall(request: Request): Call {
            val requestBuilder = request.newBuilder()
            requestBuilder.tag(TAG_CALL)
            return super.newCall(requestBuilder.build())
        }

        companion object {
            val TAG_CALL = Any()
        }
    }


}
