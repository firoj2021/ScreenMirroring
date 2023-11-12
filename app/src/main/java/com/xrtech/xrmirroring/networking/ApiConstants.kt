package com.xrtech.xrmirroring.networking

import com.xrtech.xrmirroring.enums.ApiEnvironment

object ApiConstants {

    //Change during build apk
    val environment: ApiEnvironment = ApiEnvironment.DEVELOPMENT

    const val CLIENT_ID = "2"
    const val EMAIL = "api@xrsense.global"
    fun apiBaseURL(): String {
        return when (environment) {
            ApiEnvironment.DEVELOPMENT -> "https://dev.xrsense.global/api/1.0/"
            ApiEnvironment.STAGING -> "https://staging.xrsense.global/api/1.0/"
            ApiEnvironment.PRODUCTION -> "https://app.xrsense.global/api/1.0/"
            ApiEnvironment.STABLE -> "http://stable.demo.xrsense.global/api/1.0/"
        }
    }

    fun apiBaseXAuthToken(): String {
        return when (environment) {
            ApiEnvironment.DEVELOPMENT -> "IyL3CUM8bSCcjgJPkEHDPTz5CcUyKQFKL7gJi20kOnPrsNiBGJ3Y65ibzAzT6hE6"
            ApiEnvironment.STAGING -> "IyL3CUM8bSCcjgJPkEHDPTz5CcUyKQFKL7gJi20kOnPrsNiBGJ3Y65ibzAzT6hE6"
            ApiEnvironment.PRODUCTION -> "eBE4htTu9YX83n2jYZORkoSSeEb5rdBUvbrhFPZJoDIGtGjYgShGwiUoKz75qw8B"
            ApiEnvironment.STABLE -> "tQm6KFdPdUYm8FrNZunDQw8Py3BTQxzAiDtEaeX2ieAuTwLkUkxAL8beNHVBTavO"
        }
    }
}