package com.xrtech.xrmirroring.networking
import com.google.gson.annotations.SerializedName

data class LoginCode(
    @SerializedName("id") var id: Int,
    @SerializedName("uuid") var uuid: String,
    @SerializedName("device_id") var deviceId: Int,
    @SerializedName("device_uid") var deviceUid: String,
    @SerializedName("valid_from") var validFrom: String,
    @SerializedName("valid_till") var validTill: String,
    @SerializedName("client_id") var clientId: Int,
    @SerializedName("user_id") var userId: Int,
    @SerializedName("last_login_at") var lastLoginAt: String? = null,
    @SerializedName("url") var url: String,
    @SerializedName("is_active") var is_active: Int,
)