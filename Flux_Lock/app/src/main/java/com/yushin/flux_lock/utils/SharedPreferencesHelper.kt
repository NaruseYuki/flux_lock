package com.yushin.flux_lock.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

class SharedPreferencesHelper(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("saved_devices",Context.MODE_PRIVATE)
    // 名前リストを保存
     fun saveDeviceName(deviceId:UUID,name: String) {
        sharedPreferences.edit().putString(deviceId.toString(), name).apply()
    }

    // 名前リストを取得
    fun getDeviceName(deviceId:UUID): String {
        return sharedPreferences.getString(deviceId.toString(),"")?:""
    }
}