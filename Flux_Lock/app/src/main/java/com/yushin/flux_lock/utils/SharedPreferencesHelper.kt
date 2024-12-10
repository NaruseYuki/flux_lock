package com.yushin.flux_lock.utils

import android.content.Context
import android.content.SharedPreferences
import com.yushin.flux_lock.R
import java.util.UUID

class SharedPreferencesHelper(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("saved_devices",Context.MODE_PRIVATE)
    private val context = context
    // 名前リストを保存
     fun saveDeviceName(deviceId:UUID,name: String) {
        sharedPreferences.edit().putString(deviceId.toString(), name).apply()
    }

    // 名前リストを取得
    fun getDeviceName(deviceId:UUID): String {
        return sharedPreferences.getString(deviceId.toString(),"")?:""
    }

    fun saveConnectIndex(index: Int) {
        sharedPreferences.edit().putInt(
            context.getString(
                R.string.connect_index), index).apply()
    }

    fun getConnectIndex(): Int {
        return sharedPreferences.getInt(
            context.getString(R.string.connect_index), 0)
    }

    fun removeDevice(deviceId: UUID) {
        sharedPreferences.edit().remove(deviceId.toString()).apply()
    }

}