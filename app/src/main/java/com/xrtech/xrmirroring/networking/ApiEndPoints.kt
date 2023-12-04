package com.xrtech.xrmirroring.networking

import retrofit2.Call
import retrofit2.http.*


interface ApiEndPoints {
    @POST("./")
    @FormUrlEncoded
    fun postLoginCode(
        @Header("X-Auth-Token") token: String?,
        @Header("client-id") clientId: Int, @Field("device_uid") deviceUID: String?
    ): Call<LoginRequestResponse>?


}
