package com.xrtech.xrmirroring

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.xrtech.xrmirroring.listeners.CommonListener
import com.xrtech.xrmirroring.networking.ApiServices
import com.xrtech.xrmirroring.utils.Extensions.toDate
import com.xrtech.xrmirroring.utils.NetworkUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LoginActivity : AppCompatActivity(),CommonListener {

    private val TAG = "LoginActivity"
    private lateinit var activity: LoginActivity

    private lateinit var sharedPreferencesUtils: SharedPreferencesUtils

    private lateinit var tvDeviceId: TextView

    private lateinit var txtSignin: TextView

    val CAMERA_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        activity = this

        tvDeviceId = findViewById(R.id.txtDeviceId)
        txtSignin = findViewById(R.id.txtSignin)

        sharedPreferencesUtils = SharedPreferencesUtils(this)

        val deviceId = deviceId()
        tvDeviceId.text = "Device ID:"+deviceId

        Log.e(TAG,"device id:${deviceId}")

        txtSignin.setOnClickListener {
            if (NetworkUtils.isNetworkAvailable(applicationContext)) {
                startActivityForResult(Intent(this, QRCodeActivity::class.java), 55)
            }
        }

        if (!checkPermissionForCamera()) {
            requestPermissions(
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
       val lastUsedQRCodeLoginURL = sharedPreferencesUtils.getString(AppSettings.KEY_LOGIN_URL,"")
        if (lastUsedQRCodeLoginURL.isNotEmpty() && NetworkUtils.isNetworkAvailable(activity)){
            txtSignin.text = "Please wait ... checking your credentails"
            ApiServices.postLoginCode(lastUsedQRCodeLoginURL + "/",deviceId,this)
        }else{
            val qrValidTill = sharedPreferencesUtils.getString(AppSettings.KEY_QR_CODE_VALIDITY,"")
            checkQrCodeValidity(qrValidTill)
        }
    }

    fun checkQrCodeValidity(qrValidTill:String){
        if (qrValidTill.isNotEmpty()){
            val finalDate = qrValidTill.toDate("yyyy-MM-dd HH:mm:ss")
            val calendar: Calendar = Calendar.getInstance()
            val currentTime = calendar.timeInMillis
            var qrExpired = false
            if (finalDate?.time!!.toLong() < currentTime) {
                qrExpired = true
            }
            if (!qrExpired) {
                if (AppSettings.isHostApp){
                    val intent = Intent(activity, SinkActivity::class.java)
                    startActivity(intent)
                }else{
                    val intent = Intent(activity, SenderActivity::class.java)
                    startActivity(intent)
                }
                finish()
            }else{
                showCustomDialog(getString(R.string.qr_code_expiry),"QR code is expired. Please login again",false)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            var title: String = ""
            if (requestCode == 55) {
                if (resultCode == Activity.RESULT_OK) {
                    val exp = data?.getStringExtra("exp")
                    val qrExpired: Boolean? = data?.getBooleanExtra("qrExpired", true)
                    var bodyMsg: String
                    var strvalue = exp
                    title = getString(R.string.qr_code_expiry)
                    if (qrExpired!!){
                        bodyMsg ="QR code is already expired!"
                        showCustomDialog(title, bodyMsg,false)
                    }else{
                        sharedPreferencesUtils.saveString(AppSettings.KEY_QR_CODE_VALIDITY,strvalue!!)
                        var sdf1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                        var finaldate: Date = sdf1.parse(strvalue)
                        val sdf = SimpleDateFormat("E, dd MMM yyyy HH:mm:ss", Locale.ENGLISH)
                        var expdate = sdf.format(finaldate)
                        bodyMsg = getString(R.string.scanned_successfully, expdate)
                        showCustomDialog(title, bodyMsg,true)
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG,"Exception onResult:${ex}")
        }
    }


    private fun showCustomDialog(title: String, message: String,isNavigate:Boolean) {
        val dialog = Dialog(this, R.style.CustomDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog)

        val tvTitle = dialog.findViewById(R.id.tvTitle) as TextView
        tvTitle.text = title
        val body = dialog.findViewById(R.id.tvBody) as TextView
        body.text = message

        val okBtn = dialog.findViewById(R.id.btnOK) as TextView
        okBtn.setOnClickListener {
            dialog.dismiss()
            if (isNavigate){
                if (AppSettings.isHostApp){
                    val intent = Intent(activity, SinkActivity::class.java)
                    startActivity(intent)
                }else{
                    val intent = Intent(activity, SenderActivity::class.java)
                    startActivity(intent)
                }
                finish()
            }
        }
        dialog.show()

    }

    fun checkPermissionForCamera(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
        return result == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        } else {
            // The camera permission was denied.
           showToast("Camera permission required")
        }
    }

    private fun deviceId(): String {
        val dId = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        sharedPreferencesUtils.saveString(AppSettings.KEY_DEVICE_ID, dId)
        return dId
    }

    fun showToast(msg:String){
        Toast.makeText(
            this,
            msg,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun success(msg: String) {

    }

    override fun data(data: String, isExpired: Boolean) {
        txtSignin.text = "Sign In"
        if (isExpired){
            showToast("QR code is expired!")
        }else{
            checkQrCodeValidity(data)
        }
    }

    override fun error(msg: String) {
        txtSignin.text = "Sign In"
       showToast(msg)
    }


}