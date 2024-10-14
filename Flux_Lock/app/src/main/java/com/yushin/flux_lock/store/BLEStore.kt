package com.yushin.flux_lock.store

import android.util.Log
import co.candyhouse.sesame.open.device.CHDeviceStatus
import co.candyhouse.sesame.open.device.CHDevices
import com.yushin.flux_lock.action.BLEAction
import com.yushin.flux_lock.dispatcher.BLEDispatcher
import com.yushin.flux_lock.utils.Utils.addTo
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BLEStore @Inject constructor(
    private val bleDispatcher: BLEDispatcher
) {
    // 未登録デバイス
    private val unRegisteredDevices = mutableListOf<CHDevices>()
    private val unRegisteredSubject = BehaviorSubject.createDefault(unRegisteredDevices)

    // 登録デバイス
    private val registeredDevices = mutableListOf<CHDevices>()
    private val registeredSubject = BehaviorSubject.createDefault(registeredDevices)

    // 接続デバイス
    private val connectedSubject = BehaviorSubject.create<CHDevices?>()

    // デバイスステータス
    val bleStatusSubject = BehaviorSubject.create<CHDeviceStatus>()

    // ローディング中フラグ
    val loadingSubject = PublishSubject.create<Boolean>()

    private var disposables:CompositeDisposable = CompositeDisposable()

    init {
        bleDispatcher.onAction()
            .subscribe { action ->
                when (action) {
                    is BLEAction.StartLoading -> startLoading()
                    is BLEAction.FinishLoading -> finishLoading()
                    is BLEAction.LoadUnregisteredDevices -> loadUnregisteredDevices(action.devices)
                    is BLEAction.LoadRegisteredDevices -> loadRegisteredDevices(action.devices)
                    is BLEAction.ScanDevices -> scanDevices()
                    is BLEAction.StopScanDevices -> stopScanDevices()
                    is BLEAction.ConnectDevice -> connectDevice(action.device)
                    is BLEAction.SendUserConfig -> sendUserConfig()
                    is BLEAction.LockDevice -> lockDevice()
                    is BLEAction.UnlockDevice -> unlockDevice()
                    is BLEAction.CheckDeviceStatus -> checkDeviceStatus()
                    is BLEAction.RegisterDevice -> registerDevice()
                    is BLEAction.ChangeBleStatus -> changeBleStatus(action.status)
                    is BLEAction.Toggle -> toggle(action.device)
                }
            }.addTo(disposables)
    }

    private fun toggle(device: CHDevices) {
        Log.d("BLE", "toggle: $device")
    }

    private fun loadRegisteredDevices(devices:List<CHDevices>) {
        registeredDevices.clear()
        registeredDevices.addAll(devices)
        registeredSubject.onNext(registeredDevices)
    }

    private fun loadUnregisteredDevices(devices:List<CHDevices>) {
        unRegisteredDevices.clear()
        unRegisteredDevices.addAll(devices)
        unRegisteredSubject.onNext(unRegisteredDevices)
    }

    private fun registerDevice() {
        //登録成功したデバイスをストアに反映する
        connectedSubject.value?.let { registeredDevices.add(it) }
        registeredSubject.onNext(registeredDevices)
        Log.d("BLE", "registerDevice: $registeredDevices")

    }

    private fun checkDeviceStatus() {
        TODO("Not yet implemented")
    }

    private fun unlockDevice() {
        TODO("Not yet implemented")
    }

    private fun lockDevice() {
        TODO("Not yet implemented")
    }

    private fun sendUserConfig() {
        TODO("Not yet implemented")
    }

    private fun connectDevice(device: CHDevices) {
        connectedSubject.onNext(device)
        Log.d("BLE", "finished connectDevice")
    }

    private fun scanDevices() {
        Log.d("BLE", "finished scanDevices")
    }

    private fun stopScanDevices() {
    Log.d("BLE", "finished stopScanDevices")
    }

    fun onDestroy(){
        disposables.dispose()
    }

    private fun startLoading() {
        loadingSubject.onNext(true)
    }

    private fun finishLoading() {
        loadingSubject.onNext(false)
    }

    private fun changeBleStatus(status: CHDeviceStatus) {
        bleStatusSubject.onNext(status)
    }

    // 未登録デバイスのリストを取得する
    fun getUnregisteredDevices(): BehaviorSubject<MutableList<CHDevices>> = unRegisteredSubject

    // 登録デバイスのリストを取得する
    fun getRegisteredDevices(): BehaviorSubject<MutableList<CHDevices>> = registeredSubject

    // 接続デバイスを取得する
    fun getConnectedDevice() = connectedSubject

}