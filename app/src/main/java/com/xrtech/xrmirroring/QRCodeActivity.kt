package com.xrtech.xrmirroring

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.hardware.camera2.CaptureRequest
import android.os.Bundle
import android.util.Log
import android.util.Range
import android.util.Size
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.common.Barcode
import com.xrtech.xrmirroring.databinding.ActivityQrcodeBinding
import com.xrtech.xrmirroring.listeners.CommonListener
import com.xrtech.xrmirroring.networking.ApiServices
import com.xrtech.xrmirroring.utils.BEQRCode
import com.xrtech.xrmirroring.utils.NetworkUtils
import com.xrtech.xrmirroring.utils.QRCodeAnalyzer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

typealias QRCodeListener = (code: Barcode) -> Unit

class QRCodeActivity : AppCompatActivity(), SurfaceHolder.Callback, CommonListener {

    var TAG = "QRCodeActivity"
    private lateinit var binding: ActivityQrcodeBinding
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView

    private lateinit var sharedPreferencesUtils: SharedPreferencesUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferencesUtils = SharedPreferencesUtils(this)
        previewView = binding.previewView
        startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::imageAnalysis.isInitialized)
            imageAnalysis.clearAnalyzer()

        if (this::cameraExecutor.isInitialized) {
            if (!cameraExecutor.isShutdown) {
                cameraExecutor.shutdown()
                cameraExecutor.awaitTermination(500, TimeUnit.MILLISECONDS)
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        // After requesting a CameraProvider, verify that its initialization succeeded when the view is created.
        cameraProviderFuture.addListener(kotlinx.coroutines.Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).also {
                    Camera2Interop.Extender(it).apply {
                        setCaptureRequestOption(
                            CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(1, 5)
                        )
                    }
                }
                .build()

            var preview: Preview = Preview.Builder()
                .build()
            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )

                //orientationEventListener.enable()
                val cameraControl = camera.cameraControl
                cameraControl.setZoomRatio(2F)
                val zs = camera.cameraInfo.zoomState
                preview.setSurfaceProvider(previewView.surfaceProvider)
            } catch (ex: Exception) {
                Log.e(TAG, "Exception cameraProvider:${ex}")
            }
            cameraExecutor = Executors.newSingleThreadExecutor()
            imageAnalysis.setAnalyzer(cameraExecutor, QRCodeAnalyzer() { barCode ->
                if (barCode.url != null) {
                    if (barCode.url.url.contains("xrsense.global/api/1.0/login-code/")) {
                        try {
                            val beQRCode = BEQRCode(barCode)
                            if (beQRCode.isValid) {
                                Log.d(TAG, "QR code is valid:" + beQRCode.loginCodeURL)
                                sharedPreferencesUtils.saveString(
                                    AppSettings.KEY_LOGIN_URL,
                                    beQRCode.loginCodeURL
                                )
                                ApiServices.postLoginCode(
                                    beQRCode.loginCodeURL + "/",
                                    sharedPreferencesUtils.getString(AppSettings.KEY_DEVICE_ID),
                                    this
                                )
                            } else {
                                Log.d(TAG, "QR code is not valid")
                            }
                        } catch (ex: Exception) {
                            Log.e(TAG, "startCamera -> QRCode Exception: $ex ")
                        }
                    } else {
                        val intent = Intent()
                        intent.putExtra("exp", "2023-11-11 10:10:00")
                        intent.putExtra("qrExpired", true)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                } else {
                    val intent = Intent()
                    intent.putExtra("exp", "2023-11-11 10:10:00")
                    intent.putExtra("qrExpired", true)
                    setResult(Activity.RESULT_OK, intent)
                    finish()

                }
            })


        }, ContextCompat.getMainExecutor(this))

    }

    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        //drawFocusRect(Color.WHITE)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }

    override fun success(msg: String) {

    }

    override fun data(data: String, qrExpired: Boolean) {
        val intent = Intent()
        intent.putExtra("exp", data)
        intent.putExtra("qrExpired", qrExpired)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun error(msg: String) {
        showToast(msg)
    }

    fun showToast(msg: String) {
        Toast.makeText(
            this,
            msg,
            Toast.LENGTH_LONG
        ).show()
    }


}