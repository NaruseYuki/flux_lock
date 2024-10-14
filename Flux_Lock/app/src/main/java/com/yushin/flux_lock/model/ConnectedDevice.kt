package com.yushin.flux_lock.model

import co.candyhouse.sesame.open.device.CHDevices
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectedDevice @Inject constructor() {
    private var device: CHDevices? = null

    fun setDevice(device: CHDevices) {
        this.device = device
    }
}