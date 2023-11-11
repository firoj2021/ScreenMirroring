package com.xrtech.xrmirroring.networking

import com.google.gson.annotations.SerializedName

data class LoginRespData(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("login_code") val login_code: LoginCode,
)