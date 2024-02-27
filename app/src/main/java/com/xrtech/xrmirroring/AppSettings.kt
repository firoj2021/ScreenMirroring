package com.xrtech.xrmirroring

object AppSettings {

    //during run to the vuzix device::Host device
    //set isHostApp = true
    //var isHostApp = true
    //use SinkActivity to the manifest


    //during run to the mobile device::client device
    //set isHostApp = false
    var isHostApp = false
    //use SenderActivity to the manifest

    val KEY_DEVICE_ID = "dev_id"
    val KEY_QR_CODE_VALIDITY = "qrvalid_till"
    val KEY_LOGIN_URL = "loginCodeURL"


}