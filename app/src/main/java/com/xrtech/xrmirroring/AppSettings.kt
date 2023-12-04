package com.xrtech.xrmirroring

object AppSettings {


    val isHostApp = true //Please update also at the manifets file

    //during run to the vuzix device::Host device
    //set isHostApp = true
    //use SinkActivity to the manifest


    //during run to the mobile device::client device
    //set isHostApp = false
    //use SenderActivity to the manifest

    val KEY_DEVICE_ID = "dev_id"
    val KEY_QR_CODE_VALIDITY = "qrvalid_till"
    val KEY_LOGIN_URL = "loginCodeURL"


}