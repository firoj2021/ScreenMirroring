package com.xrtech.xrmirroring

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesUtils(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "MyPrefsXRMir",
        Context.MODE_PRIVATE
    )

    fun saveInt(key: String, value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue) ?: defaultValue
    }

    fun saveString(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }
}
