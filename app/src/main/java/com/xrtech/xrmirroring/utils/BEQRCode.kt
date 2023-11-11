package com.xrtech.xrmirroring.utils

import com.google.mlkit.vision.barcode.common.Barcode
import java.io.UnsupportedEncodingException
import java.net.URI

class BEQRCode(barcode: Barcode) {
    private lateinit var url: URI
    var isValid: Boolean = false
    var loginCodeURL = ""

    companion object {
        var TAG = "BEQRCode"
    }

    init {
        try {
            val valueType = barcode.valueType
            when (valueType) {
                Barcode.TYPE_TEXT -> {
                    url = URI(barcode.rawValue)
                    loginCodeURL = url.toString()
                }

                Barcode.TYPE_URL -> {
                    val barcodeUrl = barcode.url!!.url
                    loginCodeURL = barcodeUrl.toString()
                }
            }
            isValid = true
            //   throw Exception("QR code error to decode")
        } catch (ex: UnsupportedEncodingException) {
            isValid = false
        } catch (ex: Exception) {
            isValid = false
        }

    }

}
