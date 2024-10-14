package com.yushin.flux_lock.application

import android.app.Application
import co.candyhouse.sesame.open.CHBleManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FluxLockApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CHBleManager(this)
    }
}