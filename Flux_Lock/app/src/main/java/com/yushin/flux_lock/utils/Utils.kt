package com.yushin.flux_lock.utils

import co.candyhouse.sesame.open.device.CHDevices
import co.candyhouse.sesame.open.device.CHProductModel
import com.yushin.flux_lock.R
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

object Utils {
     fun Disposable.addTo(compositeDisposable: CompositeDisposable): Disposable {
        compositeDisposable.add(this)
        return this
    }

     fun CHDevices.getFirZip(): Int {
        return when (productModel) {
            CHProductModel.SS2 -> R.raw.sesame_221_0_8c080c
            CHProductModel.SS4 -> R.raw.sesame_421_4_50ce5b
            CHProductModel.SS5 -> R.raw.sesame5_30_5_aaac8b
            CHProductModel.SS5PRO -> R.raw.sesame5pro_30_7_aaac8b
            CHProductModel.WM2 -> 0
            CHProductModel.SesameBot1 -> R.raw.sesamebot1_21_2_369eb9
            CHProductModel.BiKeLock -> R.raw.sesamebike1_21_3_d7162a
            CHProductModel.BiKeLock2 -> R.raw.sesamebike2_30_6_4835a6
            CHProductModel.SSMOpenSensor -> R.raw.opensensor1_30_8_dcd308
            CHProductModel.SSMTouchPro -> R.raw.sesametouch1pro_30_9_77a483
            CHProductModel.SSMTouch -> R.raw.sesametouch1_30_10_77a483
            CHProductModel.BLEConnector -> R.raw.bleconnector_30_11_3a61be
        }
    }
}