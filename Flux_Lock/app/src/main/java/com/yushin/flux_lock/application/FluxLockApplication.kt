package com.yushin.flux_lock.application

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import co.candyhouse.sesame.open.CHBleManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FluxLockApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // ダークモードを有効にする
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        CHBleManager(this)
    }
}