package com.example.gptroom.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import java.util.*

object DeviceUtils {

    // Constants for permissions
    private const val PERMISSION_REQUEST_CODE = 1001

    // Get ANDROID_ID
    fun getAndroidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
    // Get UUID based on ANDROID_ID
    fun getUUID(context: Context): String {
        val androidId = getAndroidId(context)
        return UUID.nameUUIDFromBytes(androidId.toByteArray()).toString()
    }
    // Get IMEI (requires READ_PHONE_STATE permission)
    fun getIMEI(context: Context): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Starting from Android 10, IMEI is restricted
            return "Unavailable on Android 10 and above"
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not granted
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.READ_PHONE_STATE), PERMISSION_REQUEST_CODE)
            return null
        }

        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.deviceId
    }

    // Get Serial Number (deprecated in Android 10)
    fun getSerialNumber(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Build.getSerial()
            } catch (e: SecurityException) {
                "Permission required"
            }
        } else {
            Build.SERIAL
        }
    }

    // Generate a UUID
    fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }

    // Handle permission request result
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray): Boolean {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            return grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        return false
    }
}
