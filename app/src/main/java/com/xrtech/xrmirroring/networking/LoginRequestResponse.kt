package com.xrtech.xrmirroring.networking


import com.google.gson.annotations.SerializedName

data class LoginRequestResponse(
    @SerializedName("message") val message : String,
    @SerializedName("data") val data : LoginRespData,
)
